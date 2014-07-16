import com.sun.source.tree.*;
import com.sun.source.util.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TryWithCheckPlugin implements com.sun.source.util.Plugin {

    @Override
    public String getName() {
        return "TryWithCheckPlugin";
    }

    @Override
    public void init(JavacTask javacTask, String... args) {
        System.err.println("Running TryWithCheckPlugin");
        javacTask.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent taskEvent) {
            }

            @Override
            public void finished(TaskEvent taskEvent) {
                if (taskEvent.getKind().equals(TaskEvent.Kind.ANALYZE)) {
                    CompilationUnitTree compilationUnit = taskEvent.getCompilationUnit();
                    OnMissingTryWithResourcesCallback callback = (allSuperTypes, isTestCode, type, trees, tree, cu) -> {
                        boolean isZipStream = allSuperTypes.contains(ZipInputStream.class.getCanonicalName()) || allSuperTypes.contains(ZipOutputStream.class.getCanonicalName());
                        if (isZipStream && !isTestCode) {
                            trees.printMessage(Diagnostic.Kind.WARNING, "Use try-with-resources, offending class was " + type.toString(), tree, cu);
                        }
                    };
                    new CodePatternTreeVisitor(javacTask.getTypes(), javacTask.getElements(), Trees.instance(javacTask), callback).scan(compilationUnit, null);
                }
            }
        });
    }

    public static interface OnMissingTryWithResourcesCallback {
        void call(List<String> allSuperTypes, boolean isTestCode, DeclaredType type, Trees trees, NewClassTree tree, CompilationUnitTree cu);
    }

    public static class CodePatternTreeVisitor extends TreePathScanner<Void, Void> {

        private final Types types;
        private final Elements elements;
        private final Trees trees;
        private final SourcePositions sourcePositions;
        private CompilationUnitTree currCompUnit;
        private String currMethodName;
        private final OnMissingTryWithResourcesCallback callback;

        public CodePatternTreeVisitor(Types types, Elements elements, Trees trees, OnMissingTryWithResourcesCallback callback) {
            this.types = types;
            this.elements = elements;
            this.trees = trees;
            this.callback = callback;
            this.sourcePositions = trees.getSourcePositions();
        }

        public Set<TypeMirror> getAllSuperTypes(TypeMirror fromType) {
            HashSet<TypeMirror> result = new HashSet<>();
            result.add(fromType);
            for (TypeMirror typeMirror : types.directSupertypes(fromType)) {
                result.addAll(getAllSuperTypes(typeMirror));
            }
            return result;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree compilationUnitTree, Void aVoid) {
            this.currCompUnit = compilationUnitTree;
            return super.visitCompilationUnit(compilationUnitTree, aVoid);
        }

        @Override
        public Void visitMethod(MethodTree methodTree, Void aVoid) {
            this.currMethodName = methodTree.getName().toString();
            return super.visitMethod(methodTree, aVoid);
        }

        boolean insideTryWith = false;

        @Override
        public Void visitTry(TryTree tryTree, Void aVoid) {
            insideTryWith = true;
            Void res = super.visitTry(tryTree, aVoid);
            insideTryWith = false;
            return res;
        }

        @Override
        public Void visitBlock(BlockTree blockTree, Void aVoid) {
            insideTryWith = false;
            return super.visitBlock(blockTree, aVoid);
        }

        @Override
        public Void visitCatch(CatchTree catchTree, Void aVoid) {
            insideTryWith = false;
            return super.visitCatch(catchTree, aVoid);
        }

        @Override
        public Void visitNewClass(NewClassTree newClassTree, Void aVoid) {
            ExpressionTree identifier = newClassTree.getIdentifier();
            if (identifier instanceof JCTree.JCIdent) {
                JCTree.JCIdent id = (JCTree.JCIdent) identifier;
                Symbol sym = id.sym;
                Type.ClassType type = (Type.ClassType) sym.asType();
                List<String> allSuperTypes = getAllSuperTypes(type).stream().map(x -> x.toString()).collect(Collectors.toList());

                boolean isAutoClosable = allSuperTypes.contains(AutoCloseable.class.getCanonicalName());
                boolean isTestCode = currCompUnit.getSourceFile().getName().contains(String.join(File.separator, "src", "main", "test", "java"));
                /*boolean isZipStream = allSuperTypes.contains(ZipInputStream.class.getCanonicalName()) || allSuperTypes.contains(ZipOutputStream.class.getCanonicalName());
                if (!insideTryWith && !isTestCode && isAutoClosable && isZipStream) {
                    trees.printMessage(diagnosticKind, "Use try-with-resources, offending class was " + type.toString(), newClassTree, currCompUnit);
                }*/
                if (isAutoClosable) {
                    callback.call(allSuperTypes, isTestCode, type, trees, newClassTree, currCompUnit);
                }
            }
            return super.visitNewClass(newClassTree, aVoid);
        }
    }
}

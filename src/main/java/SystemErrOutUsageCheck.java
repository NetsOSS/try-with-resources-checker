import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.*;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class SystemErrOutUsageCheck implements com.sun.source.util.Plugin {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void init(JavacTask javacTask, String... strings) {
        javacTask.addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent taskEvent) {
            }

            @Override
            public void finished(TaskEvent taskEvent) {
                if (taskEvent.getKind().equals(TaskEvent.Kind.ANALYZE)) {
                    CompilationUnitTree compilationUnit = taskEvent.getCompilationUnit();
                    new Visitor(javacTask.getTypes(), javacTask.getElements(), Trees.instance(javacTask), compilationUnit).scan(compilationUnit, null);
                }
            }
        });
    }

    private static class Visitor extends TreePathScanner<Void, Void> {

        private final Types types;
        private final Elements elements;
        private final Trees trees;
        private final SourcePositions sourcePositions;
        private CompilationUnitTree currCompUnit;

        private Visitor(Types types, Elements elements, Trees trees, CompilationUnitTree cu) {
            this.types = types;
            this.elements = elements;
            this.trees = trees;
            this.sourcePositions = trees.getSourcePositions();
            this.currCompUnit = cu;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree methodInvocationTree, Void aVoid) {
            ExpressionTree methodSelect = methodInvocationTree.getMethodSelect();
            String method = methodSelect.toString();
            if (method.startsWith("System.err") || method.startsWith("System.out")) {
                trees.printMessage(Diagnostic.Kind.ERROR, "Using System.(out|err)", methodInvocationTree, currCompUnit);
            }
            return super.visitMethodInvocation(methodInvocationTree, aVoid);
        }
    }

}

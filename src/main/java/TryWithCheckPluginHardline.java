import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;

import javax.tools.Diagnostic;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TryWithCheckPluginHardline implements com.sun.source.util.Plugin {
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
                    TryWithCheckPlugin.OnMissingTryWithResourcesCallback callback = (allSuperTypes, isTestCode, type, trees, tree, cu) -> {
                        boolean isZipStream = allSuperTypes.contains(ZipInputStream.class.getCanonicalName()) || allSuperTypes.contains(ZipOutputStream.class.getCanonicalName());
                        if (isZipStream && !isTestCode) {
                            trees.printMessage(Diagnostic.Kind.ERROR, "Use try-with-resources, offending class was " + type.toString(), tree, cu);
                        }
                    };
                    new TryWithCheckPlugin.CodePatternTreeVisitor(javacTask.getTypes(), javacTask.getElements(), Trees.instance(javacTask), callback).scan(compilationUnit, null);
                }
            }
        });
    }
}

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;

import javax.tools.Diagnostic;

public class ExtraHardline implements com.sun.source.util.Plugin {

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
                        if (!isTestCode) {
                            trees.printMessage(Diagnostic.Kind.ERROR, "Use try-with-resources, offending class was " + type.toString(), tree, cu);
                        }
                    };
                    new TryWithCheckPlugin.CodePatternTreeVisitor(javacTask.getTypes(), javacTask.getElements(), Trees.instance(javacTask), callback).scan(compilationUnit, null);
                }
            }
        });

    }
}

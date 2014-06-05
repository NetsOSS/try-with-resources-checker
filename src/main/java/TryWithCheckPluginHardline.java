import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;

import javax.tools.Diagnostic;

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
                    new TryWithCheckPlugin.CodePatternTreeVisitor(javacTask.getTypes(), javacTask.getElements(), Trees.instance(javacTask), Diagnostic.Kind.ERROR).scan(compilationUnit, null);
                }
            }
        });
    }
}

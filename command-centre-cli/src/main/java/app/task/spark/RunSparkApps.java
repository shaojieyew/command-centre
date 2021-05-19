package app.task.spark;

import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;

public class RunSparkApps extends Task {

    SparkCli cli;
    String appName;
    public RunSparkApps(SparkCli cli){
        super();
        this.cli = cli;
        appName = cli.getCliName();
    }

    @Override
    protected String getTaskName() {
        return "Run Spark Applications";
    }

    @Override
    protected void preTask() {
    }

    @Override
    protected void postTask() {

    }

    @Override
    protected void task() throws Exception {
        cli.getSpecFile().forEach(kind -> kind.getSpec().forEach(spec->{
            if(appName!=null &&  !((SparkDeploymentSpec) spec).getName().equals(appName)){
                return;
            }
            RunSparkApp runSparkApp = new RunSparkApp(cli,  (SparkDeploymentKind) kind, (SparkDeploymentSpec) spec);
            try {
                runSparkApp.startTask();
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        }));
    }
}

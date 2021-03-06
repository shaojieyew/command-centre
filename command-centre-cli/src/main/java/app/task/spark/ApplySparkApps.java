package app.task.spark;

import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;

public class ApplySparkApps extends Task {

    SparkCli cli;
    String appName;

    public ApplySparkApps(SparkCli cli){
        super();
        this.cli = cli;
        appName = cli.getCliName();
    }

    @Override
    protected String getTaskName() {
        return "Apply Spark Applications";
    }

    @Override
    protected void preTask() {
    }

    @Override
    protected void postTask() {

    }

    @Override
    protected void task() throws Exception {
        SparkCli.getSpecsFromKind( cli.getSpecFile()).forEach(spec->{
            if(appName!=null &&  !( spec).getName().equals(appName)){
                return;
            }
            ApplySparkApp runSparkApp = new ApplySparkApp(cli, spec);
            try {
                runSparkApp.startTask();
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        });
    }
}

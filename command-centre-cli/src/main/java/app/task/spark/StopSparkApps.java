package app.task.spark;

import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;

public class StopSparkApps extends Task {

    SparkCli cli;
    public StopSparkApps(SparkCli cli){
        super();
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Stop Spark Applications";
    }

    @Override
    protected void preTask() {
    }

    @Override
    protected void postTask() {

    }

    @Override
    protected void task() throws Exception {
        if(cli.getSpecFile().size()==0){
            new StopSparkApp(cli).startTask();
        }else{
            cli.getSpecFile().forEach(kind -> kind.getSpec().forEach(spec->{
                StopSparkApp stopSparkApp = new StopSparkApp(cli);
                try {
                    stopSparkApp.setAppName(((SparkDeploymentSpec)spec).getName());
                    stopSparkApp.startTask();
                } catch (Exception e) {
                    ConsoleHelper.console.display(e);
                }
            }));
        }
    }
}

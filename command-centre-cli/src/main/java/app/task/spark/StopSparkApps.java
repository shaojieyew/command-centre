package app.task.spark;

import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(StopSparkApps.class);
    @Override
    protected void task() throws Exception {
        if(cli.getCliRecursiveFilePath()==null && cli.getCliFilePath()==null){
            new StopSparkApp(cli).startTask();
        }else{
            cli.getSpecFile().stream().forEach(kind -> kind.getSpec().stream()
                    .filter(spec->{
                        if(cli.getCliName()==null){
                            return true;
                        }
                        if(cli.getCliName().equalsIgnoreCase(((SparkDeploymentSpec)spec).getName())){
                            return true;
                        }
                        return false;
                    })
                    .forEach(spec->{
                        logger.info("kill spark application name={}",((SparkDeploymentSpec)spec).getName());
                        StopSparkApp stopSparkApp = new StopSparkApp(cli,((SparkDeploymentSpec)spec).getName());
                        try {
                            stopSparkApp.startTask();
                        } catch (Exception e) {
                            ConsoleHelper.console.display(e);
                        }
                    }));
        }
    }
}

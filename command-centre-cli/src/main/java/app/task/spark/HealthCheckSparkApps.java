package app.task.spark;

import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckSparkApps extends Task {

    SparkCli cli;
    public static Logger logger = LoggerFactory.getLogger(HealthCheckSparkApps.class);

    public HealthCheckSparkApps(SparkCli cli) {
        super();
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Checking Spark Application Health";
    }

    @Override
    protected void preTask() throws Exception {

    }

    @Override
    protected void postTask() throws Exception {
    }

    @Override
    protected void task() throws Exception {
        SparkCli.getSpecsFromSparkKind(cli.getSubmittedAppSpec()).forEach(spec->{

        });

        cli.getSubmittedAppSpec().forEach(kind -> kind.getSpec().forEach(spec->{
            SparkDeploymentSpec sparkDeploymentSpec = (SparkDeploymentSpec) spec;
            if(sparkDeploymentSpec.getEnableHealthCheck()!=null
                    && sparkDeploymentSpec.getEnableHealthCheck().equalsIgnoreCase("true")){
                try {
                    RunSparkApp runSparkApp = new RunSparkApp(cli, sparkDeploymentSpec);
                    runSparkApp.setSaveSnapshot(false);
                    runSparkApp.startTask();
                } catch (Exception e) {
                    ConsoleHelper.console.display(e);
                }
            }
        }));
    }
}

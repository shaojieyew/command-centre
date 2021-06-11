package app.task.spark;

import app.cli.SparkCli;
import app.spec.SpecException;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HealthCheckSparkApps extends Task {

    SparkCli cli;

    public HealthCheckSparkApps(SparkCli cli) throws IOException, SpecException {
        super();
        for (File sparkSubmitDir : new File(cli.getSparkSubmitDir()).listFiles()) {
            long max = Long.MIN_VALUE;
            for (File file : sparkSubmitDir.listFiles()) {
                try{
                    long date = Long.parseLong(file.getName());
                    if(date>max){
                        max = date;
                    }
                }catch (Exception e){

                }
            }
            String dir = String.format("%s\\%s", sparkSubmitDir.getAbsolutePath(),  max);
            cli.loadFile(dir, false);
        }
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
        cli.getSpecFile().forEach(kind -> kind.getSpec().forEach(spec->{
            SparkDeploymentSpec sparkDeploymentSpec = (SparkDeploymentSpec) spec;
            if(sparkDeploymentSpec.getEnableHealthCheck()!=null
                    && sparkDeploymentSpec.getEnableHealthCheck().equalsIgnoreCase("true")){
                RunSparkApp runSparkApp = new RunSparkApp(cli,  (SparkDeploymentKind) kind, (SparkDeploymentSpec) sparkDeploymentSpec);
                runSparkApp.setSaveSnapshot(false);
                try {
                    runSparkApp.startTask();
                } catch (Exception e) {
                    ConsoleHelper.console.display(e);
                }
            }
        }));
    }
}

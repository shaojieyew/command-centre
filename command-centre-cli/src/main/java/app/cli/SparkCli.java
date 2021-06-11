package app.cli;

import app.cli.type.Action;
import app.spec.Kind;
import app.task.spark.HealthCheckSparkApps;
import app.task.spark.ListSparkApp;
import app.task.spark.RunSparkApps;
import app.task.spark.StopSparkApps;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

public class SparkCli extends Cli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(SparkCli.class);

    public List<Kind> getSpecFile() {
        return super.getSpecFile().stream()
                .filter(k->k.getKind().toUpperCase().equalsIgnoreCase(Cli.KIND_APP_DEPLOYMENT.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Integer task() throws Exception {
        if(getCliAction().equalsIgnoreCase(Action.ls.toString())){
            new ListSparkApp(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.run.toString()) || getCliAction().equalsIgnoreCase(Action.start.toString())){
            new RunSparkApps(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.stop.toString())){
            new StopSparkApps(this).startTask();
        }
//        else if(getCliAction().equalsIgnoreCase(Action.healthCheck.toString())){
//            new HealthCheckSparkApps(this).startTask();
//        }
        return 0;
    }
    private static String SPARK_SUBMIT_DIR = "spark-submit";
    public String getSparkSubmitDir(){
        return  getC2CliProperties().getSparkSnapshotDirectory()+"/"+SPARK_SUBMIT_DIR;
    }
    private static String SPARK_SUBMIT_JAR_DIR = "repository";
    public String getSparkSubmitJarDir(){
        return  getC2CliProperties().getSparkSnapshotDirectory()+"/"+SPARK_SUBMIT_JAR_DIR;
    }
}



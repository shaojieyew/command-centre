package app.cli;

import app.cli.type.Action;
import app.spec.Kind;
import app.spec.SpecException;
import app.task.spark.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
        } else if(getCliAction().equalsIgnoreCase(Action.apply.toString())){
            new ApplySparkApps(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.stop.toString())){
            new StopSparkApps(this).startTask();
        }
        return 0;
    }
    public static Logger logger = LoggerFactory.getLogger(SparkCli.class);
    public void loadSubmittedApp(){
        for (File sparkSubmitDir : new File(getSparkSubmitDir()).listFiles()) {
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

            if(max>Long.MIN_VALUE){
                String dir = String.format("%s\\%s", sparkSubmitDir.getAbsolutePath(),  max);
                try {
                    loadFile(dir, false);
                } catch (IOException | SpecException e) {
                    logger.warn("cannot load files from = {}, reason = {}",dir ,e.getMessage());
                }
            }
        }
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



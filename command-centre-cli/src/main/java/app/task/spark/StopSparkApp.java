package app.task.spark;
import app.c2.service.yarn.YarnSvcFactory;
import app.c2.service.yarn.model.YarnApp;
import app.cli.SparkCli;
import app.task.Task;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class StopSparkApp extends Task {

    public static final Logger logger = LoggerFactory.getLogger(StopSparkApp.class);

    SparkCli cli;
    String appName;
    String appId;

    public StopSparkApp(SparkCli cli){
        super();
        this.cli = cli;
        appId = cli.getCliId();
        appName = cli.getCliName();
    }

    public StopSparkApp(SparkCli cli, String appName){
        super();
        this.cli = cli;
        appId = cli.getCliId();
        this.appName = appName;
    }

    @Override
    protected String getTaskName() {
        if(appName!=null){
            return "Stop Spark Application - "+appName;
        }else if(appId!=null){
            return "Stop Spark Application - "+appId;
        }
        return "Stop Spark Application";
    }

    @Override
    protected void preTask() throws Exception {
        if(appName==null && appId==null){
            throw new Exception("Invalid application name or applicationId");
        }
    }

    @Override
    protected void postTask() throws Exception {
    }

    @Override
    protected void task() throws Exception {
        if(appName!=null) {
            YarnSvcFactory.create(cli.getC2CliProperties()).setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get().stream()
                    .filter(f->f.getName().equals(appName))
                    .forEach(s->{
                        logger.info("kill applicationid={}",s.getId());
                        YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(s.getId())
                                .kill();
                    });
            if(YarnSvcFactory.create(cli.getC2CliProperties()).setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get().stream()
                    .noneMatch(f->f.getName().equals(appName))){
                deleteSnapshot(appName);
            }
        } else if(appId!=null) {
            YarnSvcFactory.create(cli.getC2CliProperties()).setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get().stream()
                    .filter(f->f.getId().equals(appId))
                    .forEach(s->{
                        logger.info("kill applicationid={}",s.getId());
                        YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(s.getId())
                        .kill();
            });
            if(YarnSvcFactory.create(cli.getC2CliProperties()).setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get().stream()
                    .noneMatch(f->f.getId().equals(appId))){
                deleteSnapshot(appName);
            }
        }
    }

    private void deleteSnapshot(String appName) throws IOException {
        File f = new File(String.format("%s/%s",cli.getSparkSubmitDir() , appName));
        if(f.exists() && f.isDirectory()){
            FileUtils.deleteDirectory(f);
        }
    }
}

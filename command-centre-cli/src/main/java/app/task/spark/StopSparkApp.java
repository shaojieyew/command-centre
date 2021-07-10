package app.task.spark;
import app.c2.service.yarn.YarnSvcFactory;
import app.c2.service.yarn.model.YarnApp;
import app.cli.SparkCli;
import app.task.Task;
import app.util.ConsoleHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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

    public static String YARN_RUNNING_STATE ="NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING";

    @Override
    protected void task() throws Exception {
        if(appName!=null) {
            YarnSvcFactory.create(cli.getC2CliProperties()).setStates(YARN_RUNNING_STATE)
                    .get().stream()
                    .filter(f->f.getName().equals(appName))
                    .forEach(s->{
                        logger.info("kill applicationid={}",s.getId());
                        try {
                            YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(s.getId())
                                    .kill();
                        } catch (Exception e) {
                            ConsoleHelper.console.display(e);
                        }
                    });
            int count = 0;
            long runningCount = 0;

            do{
                runningCount = YarnSvcFactory.create(cli.getC2CliProperties()).setStates(YARN_RUNNING_STATE)
                        .get().stream()
                        .filter(f->f.getName().equals(appName))
                        .count();
                count++;
            } while(count<3 && runningCount >0);

            if(runningCount==0){
                deleteSnapshot(appName);
            }else{
                throw new RuntimeException("Failed to stop application appName = "+appName);
            }
        } else if(appId!=null) {
            Optional<YarnApp> app =  YarnSvcFactory.create(cli.getC2CliProperties()).setStates(YARN_RUNNING_STATE)
                    .get().stream()
                    .filter(f->f.getId().equals(appId)).findAny();
            if(app.isPresent()){
                YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(app.get().getId())
                        .kill();
                int count = 0;
                long runningCount = 0;
                do{
                    runningCount = YarnSvcFactory.create(cli.getC2CliProperties()).setStates(YARN_RUNNING_STATE)
                            .get().stream()
                            .filter(f->f.getName().equals(app.get().getName()))
                            .count();
                    count++;
                } while(count<3 && runningCount >0);

                if(runningCount==0){
                    deleteSnapshot(app.get().getName());
                }else{
                    throw new RuntimeException("Failed to stop application appId = "+appId);
                }
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

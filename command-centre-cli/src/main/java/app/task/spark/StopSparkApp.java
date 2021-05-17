package app.task.spark;
import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import app.cli.SparkCli;
import app.task.Task;

public class StopSparkApp extends Task {

    SparkCli cli;
    String appName;
    String appId;

    public StopSparkApp(SparkCli cli){
        super();
        this.cli = cli;
        appId = cli.getCliId();
        appName = cli.getCliName();
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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
        YarnSvc yarnSvc =  YarnSvcFactory.create(cli.getC2CliProperties());
        if(appName!=null){
            yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get().stream()
                    .filter(f->f.getName().equals(appName))
                    .forEach(s->{
                        YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(s.getId())
                                .kill();
                    });
        }else if(appId!=null){
            YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(appId)
                    .kill();
        }
    }
}

package app.task.spark;

import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import app.c2.service.yarn.model.YarnApp;
import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;
import app.util.PrintableTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
public class ListSparkApp extends Task {

    SparkCli cli;
    public ListSparkApp(SparkCli cli){
        super();
        this.cli = cli;
    }
    @Override
    protected String getTaskName() {
        return "List Spark Application";
    }


    @Override
    protected void preTask() {

    }

    @Override
    protected void postTask() {

    }

    class AppStatus extends SparkDeploymentSpec{
        String name;
        String yarnAppId;
        String yarnStatus;
        String startedTime;

        public AppStatus(String name, String yarnAppId, String yarnStatus, String startedTime) {
            this.name = name;
            this.yarnAppId = yarnAppId;
            this.yarnStatus = yarnStatus;
            this.startedTime = startedTime;
        }
    }
    DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss");

    @Override
    protected void task() throws Exception {
        String inputName = cli.getCliName();

        YarnSvc yarnSvc = YarnSvcFactory.create(cli.getC2CliProperties());
        List<YarnApp> apps = null;
        try{
            apps = yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get();
        }catch (Exception ex){
            ConsoleHelper.console.display(ex);
        }
        List<YarnApp> finalApps = apps;
        List<AppStatus> appStatuses = new ArrayList<>();
        if(inputName==null){
            List<SparkDeploymentSpec> specs =
                    (List<SparkDeploymentSpec>) cli.getSpecFile().stream().flatMap(k->k.getSpec().stream())
                            .collect(Collectors.toList());
            appStatuses = specs
                    .stream()
                    .map(s->{
                        String appId = "-";
                        String yarnStatus = "-";
                        String startedTime = "-";
                        if(finalApps == null){
                            appId = "UNKNOWN";
                            yarnStatus = "UNKNOWN";
                            startedTime = "UNKNOWN";
                        }else{
                            Optional<YarnApp> optionalYarnApp = finalApps.stream().filter(a->a.getName().equalsIgnoreCase(s.getName())).findFirst();
                            if(optionalYarnApp.isPresent()){
                                appId = optionalYarnApp.get().getId();
                                yarnStatus = optionalYarnApp.get().getState();
                                startedTime = df.format(new Date(optionalYarnApp.get().getStartedTime()));
                            }
                        }
                        return new AppStatus(s.getName(),appId,yarnStatus,startedTime);
                    }).collect(Collectors.toList());

        }else{
            if(finalApps==null){
                appStatuses.add(new AppStatus(inputName,"UNKNOWN","UNKNOWN","UNKNOWN"));
            }else{
                Optional<YarnApp> yarnApp = apps.stream().filter(a->a.getName().equalsIgnoreCase(inputName)).findFirst();
                if(yarnApp.isPresent()){
                    appStatuses.add(new AppStatus(yarnApp.get().getName(),yarnApp.get().getId(),yarnApp.get().getState(),df.format(new Date(yarnApp.get().getStartedTime()))));
                }else{
                    ConsoleHelper.console.display("Input name '"+inputName+"' not found");
                }
            }
        }

        ArrayList<String> header = new ArrayList<>();
        header.add("name");
        header.add("yarnAppId");
        header.add("yarnStatus");
        header.add("startedTime");
        new PrintableTable(appStatuses, header,"Spark Applications").show();
    }
}

package app.task.spark;

import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import app.c2.service.yarn.model.YarnApp;
import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.ConsoleHelper;
import app.util.PrintableTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
public class ListSparkApp extends Task {

    public static Logger logger = LoggerFactory.getLogger(ListSparkApp.class);

    SparkCli cli;
    List<SparkDeploymentSpec> specs;
    List<SparkDeploymentSpec> submittedAppSpec;
    public ListSparkApp(SparkCli cli){
        super();
        this.cli = cli;
    }
    @Override
    protected String getTaskName() {
        return "List Spark Application";
    }


    @Override
    protected void preTask() throws IOException {
        submittedAppSpec = SparkCli.getSpecsFromSparkKind(cli.getSubmittedAppSpec());
        if(cli.getCliFilePath() == null && cli.getCliRecursiveFilePath()==null){
            specs = submittedAppSpec;
        }else{
            specs = SparkCli.getSpecsFromSparkKind(cli.getSpecFile().stream().filter(k->k instanceof SparkDeploymentKind)
                    .map(k->(SparkDeploymentKind)k)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    protected void postTask() {

    }

    class AppStatus extends SparkDeploymentSpec{
        String name;
        String yarnAppId;
        String yarnStatus;
        String startedTime;
        boolean healthCheck;

        public AppStatus(String name, String yarnAppId, String yarnStatus, String startedTime, boolean healthCheck) {
            this.name = name;
            this.yarnAppId = yarnAppId;
            this.yarnStatus = yarnStatus;
            this.startedTime = startedTime;
            this.healthCheck= healthCheck;
        }
    }
    DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss");

    @Override
    protected void task() throws Exception {
        String inputName = cli.getCliName();

        YarnSvc yarnSvc = YarnSvcFactory.create(cli.getC2CliProperties());
        List<YarnApp> remoteApps = null;
        try{
            remoteApps = yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                    .get();
        }catch (Exception ex){
            ConsoleHelper.console.display(ex);
        }
        List<YarnApp> finalRemoteApp = remoteApps;
        List<AppStatus> appStatuses = new ArrayList<>();
        if(inputName==null){
            appStatuses = specs
                    .stream()
                    .map(spec->{
                        String appId = "-";
                        String yarnStatus = "-";
                        String startedTime = "-";
                        if(finalRemoteApp == null){
                            appId = "UNKNOWN";
                            yarnStatus = "UNKNOWN";
                            startedTime = "UNKNOWN";
                        }else{
                            Optional<YarnApp> optionalYarnApp = finalRemoteApp.stream().filter(a->a.getName().equalsIgnoreCase(spec.getName())).findFirst();
                            if(optionalYarnApp.isPresent()){
                                appId = optionalYarnApp.get().getId();
                                yarnStatus = optionalYarnApp.get().getState();
                                startedTime = df.format(new Date(optionalYarnApp.get().getStartedTime()));
                            }
                        }


                        boolean isHealthCheckEnable = submittedAppSpec.stream()
                                .filter(submittedApp->submittedApp.getName().equalsIgnoreCase(spec.getName()))
                                .anyMatch(appSpec->appSpec.getEnableHealthCheck().equalsIgnoreCase("true"));

                        return new AppStatus(spec.getName(),
                                appId,
                                yarnStatus,
                                startedTime,
                                isHealthCheckEnable);
                    }).collect(Collectors.toList());
        }else{
            boolean isHealthCheckEnable = submittedAppSpec.stream()
                    .filter(submittedApp->submittedApp.getName().equalsIgnoreCase(inputName))
                    .anyMatch(appSpec->appSpec.getEnableHealthCheck().equalsIgnoreCase("true"));

            if(finalRemoteApp==null){
                appStatuses.add(new AppStatus(inputName,"UNKNOWN","UNKNOWN","UNKNOWN", isHealthCheckEnable));
            }else{
                Optional<YarnApp> yarnApp = remoteApps.stream().filter(a->a.getName().equalsIgnoreCase(inputName)).findFirst();
                if(yarnApp.isPresent()){
                    appStatuses.add(new AppStatus(yarnApp.get().getName(),
                            yarnApp.get().getId(),
                            yarnApp.get().getState(),
                            df.format(new Date(yarnApp.get().getStartedTime())),
                            isHealthCheckEnable));
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
        header.add("healthCheck");
        new PrintableTable(appStatuses, header,"Spark Applications").show();
    }
}

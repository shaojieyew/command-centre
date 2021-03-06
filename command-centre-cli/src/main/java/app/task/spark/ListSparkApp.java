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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    protected void preTask() throws IOException, GitAPIException {
        submittedAppSpec = SparkCli.getSpecsFromSparkKind(cli.getSubmittedAppSpec());
        if(cli.getCliFilePath() == null && cli.getCliRecursiveFilePath()==null && cli.getCliGitPath()==null){
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
        String dist;
        boolean healthCheck;

        public AppStatus(String name, String yarnAppId, String yarnStatus, String startedTime, String dist, boolean healthCheck) {
            this.name = name;
            this.yarnAppId = yarnAppId;
            this.yarnStatus = yarnStatus;
            this.startedTime = startedTime;
            this.healthCheck= healthCheck;
            this.dist= dist;
        }
    }
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

                        Optional<SparkDeploymentSpec> optionalSparkDeploymentSpec = submittedAppSpec.stream()
                                .filter(submittedApp->submittedApp.getName().equalsIgnoreCase(spec.getName())).findFirst();

                        boolean isHealthCheckEnable = false;
                        String dist = "UNKNOWN";
                        if(optionalSparkDeploymentSpec.isPresent()){
                            isHealthCheckEnable = optionalSparkDeploymentSpec.get().getEnableHealthCheck().equalsIgnoreCase("true");
                            dist = optionalSparkDeploymentSpec.get().getArtifact();

                            if(!dist.contains(":")){
                                Path p = Paths.get(dist);
                                dist = p.getFileName().toString();
                            }

                        }

                        return new AppStatus(spec.getName(),
                                appId,
                                yarnStatus,
                                startedTime,
                                dist,
                                isHealthCheckEnable);
                    }).collect(Collectors.toList());
        }else{
            Optional<SparkDeploymentSpec> submittedSpec = submittedAppSpec.stream()
                    .filter(submittedApp->submittedApp.getName().equalsIgnoreCase(inputName)).findFirst();
            boolean isHealthCheckEnable = false;
            String dist = "UNKNOWN";
            if(submittedSpec.isPresent()){
                isHealthCheckEnable = submittedAppSpec.stream()
                        .filter(submittedApp->submittedApp.getName().equalsIgnoreCase(inputName))
                        .anyMatch(appSpec->"true".equalsIgnoreCase(appSpec.getEnableHealthCheck()));
                        dist = submittedSpec.get().getArtifact();

                        if(!dist.contains(":")){
                            Path p = Paths.get(dist);
                            dist = p.getFileName().toString();
                        }
            }

            if(finalRemoteApp==null){
                appStatuses.add(new AppStatus(inputName,"UNKNOWN","UNKNOWN","UNKNOWN",dist, isHealthCheckEnable));
            }else{
                Optional<YarnApp> yarnApp = remoteApps.stream().filter(a->a.getName().equalsIgnoreCase(inputName)).findFirst();
                if(yarnApp.isPresent()){
                    appStatuses.add(new AppStatus(yarnApp.get().getName(),
                            yarnApp.get().getId(),
                            yarnApp.get().getState(),
                            "",
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
        header.add("dist");
        header.add("healthCheck");
        new PrintableTable(appStatuses, header,"Spark Applications").show();
    }
}

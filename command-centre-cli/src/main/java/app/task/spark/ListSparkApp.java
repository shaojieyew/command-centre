package app.task.spark;

import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import app.c2.service.yarn.model.YarnApp;
import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.PrintableTable;

import java.util.*;
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

        public AppStatus(String name, String yarnAppId, String yarnStatus) {
            this.name = name;
            this.yarnAppId = yarnAppId;
            this.yarnStatus = yarnStatus;
        }
    }

    @Override
    protected void task() throws Exception {
        YarnSvc yarnSvc = YarnSvcFactory.create(cli.getC2CliProperties());
        List<YarnApp> apps = yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                .get();
        List<SparkDeploymentSpec> specs = (List<SparkDeploymentSpec>) cli.getSpecFile().stream().flatMap(k->k.getSpec().stream()).collect(Collectors.toList());
        List<AppStatus> appStatuses = specs.stream().map(s->{
            String appId = "-";
            String yarnStatus = "-";
            Optional<YarnApp> optionalYarnApp = apps.stream().filter(a->a.getName().equalsIgnoreCase(s.getName())).findFirst();
            if(optionalYarnApp.isPresent()){
                appId = optionalYarnApp.get().getId();
                yarnStatus = optionalYarnApp.get().getState();
            }
            return new AppStatus(s.getName(),appId,yarnStatus);
        }).collect(Collectors.toList());

        ArrayList<String> header = new ArrayList<>();
        header.add("name");
        header.add("yarnAppId");
        header.add("yarnStatus");
        new PrintableTable(appStatuses, header,"Spark Applications").show();
    }
}

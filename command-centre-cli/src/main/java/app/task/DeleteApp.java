package app.task;

import app.cli.Cli;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import app.c2.service.SparkService;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
import app.util.ConsoleHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeleteApp extends Task {

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;
    @Autowired
    SparkService sparkService;

    Cli cli;
    String appName;

    @Override
    protected String getTaskName() {
        return DeleteApp.class.getSimpleName() +" "+appName;
    }

    public void startTask(Cli cli, String appName) throws Exception {
        this.cli = cli;
        this.appName = appName;
        super.startTask(cli);
    }

    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        this.appName = cli.getCliName();
        super.startTask();
    }

    public void startTask(Cli cli, List<AppDeploymentKind> kinds) throws Exception {
        kinds.forEach(s-> {
            try {
                startTask(cli, s);
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        });
    }

    public void startTask(Cli cli, AppDeploymentKind kind) throws Exception {
        kind.getSpec().forEach(s-> {
            try {
                startTask(cli, s);
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        });
    }

    public void startTask(Cli cli, AppDeploymentSpec spec) throws Exception {
        this.cli = cli;
        this.appName = spec.getName();
        if(appName==null || appName.length()==0){
            throw new Exception("Invalid app name");
        }
        super.startTask();
    }

    @Override
    protected void task() throws Exception {
        appService.delete(appName,cli.getProject().getId());
    }
}

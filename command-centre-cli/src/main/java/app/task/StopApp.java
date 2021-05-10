package app.task;

import app.cli.Cli;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import app.spec.spark.AppDeploymentKind;
import app.util.ConsoleHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StopApp extends Task{

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;
    @Autowired
    StopApp stopApp;

    Cli cli;
    public void startTask(Cli cli, List<AppDeploymentKind> kinds) throws Exception {
        kinds.forEach(k->{
            k.getSpec().forEach(s-> {
                try {
                    stopApp.startTask(cli, s.getName());
                } catch (Exception e) {
                    ConsoleHelper.console.display(e);
                }
            });
        });
    }

    @Override
    protected String getTaskName() {
        return StopApp.class.getSimpleName()+" "+appName;
    }

    @Override
    public void startTask(Cli cli) throws Exception {
        List<App> apps = appService.findAllAppStatus(cli.getProject().getId());
        apps.stream().filter(a->a.getName()!=null).forEach(a->
        {
            try {
                stopApp.startTask(cli, a.getName());
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        });
    }

    String appName;
    public void startTask(Cli cli, String appName) throws Exception {
        this.appName = appName;
        this.cli = cli;
        super.startTask(cli);
    }

    @Override
    protected void task() throws Exception {
        appService.kill(cli.getProject().getId(),appName);
    }
}

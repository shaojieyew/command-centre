package app.task;

import app.cli.Cli;
import app.c2.service.AppService;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
import app.util.ConsoleHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunApp extends Task{


    @Autowired
    RunApp runApp;
    Cli cli = null;
    String appName = null;


    public void startTask(Cli cli, List<AppDeploymentKind> kinds) throws Exception {
        kinds.forEach(k->{
            k.getSpec().forEach(s-> {
                try {
                    runApp.startTask(cli, s.getName());
                } catch (Exception e) {
                    ConsoleHelper.console.display(e);
                }
            });
        });
    }

    public void startTask(Cli cli) throws Exception {
        this.cli=cli;
        this.appName = cli.getCliName();
        startTask();
    }

    public void startTask(Cli cli, String appName) throws Exception {
        this.cli=cli;
        this.appName = appName;
        startTask();
    }

    @Override
    protected String getTaskName() {
        return RunApp.class.getSimpleName() +" "+appName;
    }

    @Autowired
    AppService appService;
    @Override
    protected void task() throws Exception {
        appService.submitApp(cli.getProject().getId(), appName, false, false);
    }
}

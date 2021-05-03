package app.task;

import app.Cli;
import app.c2.service.AppService;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RunApp extends Task{

    Cli cli = null;
    String appName = null;
@Autowired
RunApp runApp;
    public void startTask(Cli cli) throws Exception {
        this.cli=cli;
        this.appName = cli.get_name();
        if(appName!=null && appName.length()>0){
            startTask();
        }else{
            if(cli.getSpecFile().size()==0){
                throw new Exception("app doesn't exist and no spec file provided");
            }else{
                cli.getSpecFile().stream().filter(f->f instanceof AppDeploymentKind)
                        .forEach(a-> {
                            ((List<AppDeploymentSpec>)a.getSpec()).forEach(s->{
                                try {
                                    runApp.startTask(cli, s.getName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        });
            }
        }
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
        appService.submitApp(cli.getProject().getId(), appName, false);
    }
}

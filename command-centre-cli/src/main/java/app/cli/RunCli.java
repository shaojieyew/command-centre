package app.cli;

import app.cli.type.Component;
import app.spec.nifi.NifiQueryKind;
import app.spec.spark.AppDeploymentKind;
import app.task.CreateApp;
import app.task.CreateNifiQuery;
import app.task.RunApp;
import app.task.RunNifi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.stream.Collectors;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class RunCli extends Cli {

    @Autowired
    RunApp runApp;
    @Autowired
    CreateApp createApp;
    @Autowired
    CreateNifiQuery createNifiQuery;
    @Autowired
    RunNifi runNifi;

    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            if(getCliName()!=null){
                if(getCliName().length()==0){
                    throw new Exception("Invalid name");
                }
                runApp.startTask(this);
            }else{
                List<AppDeploymentKind> appDeployments = getSpecFile().stream()
                        .filter(s->s instanceof AppDeploymentKind)
                        .map(s->(AppDeploymentKind)s)
                        .collect(Collectors.toList());
                createApp.startTask(this, appDeployments);
                runApp.startTask(this, appDeployments);
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.nifi.toString())){
            if(getCliQuery()!=null || getCliName()!=null || getCliId()!=null){
                runNifi.startTask(this);
            }else{
                List<NifiQueryKind> nifiQueryKinds = getSpecFile().stream()
                        .filter(s->s instanceof NifiQueryKind)
                        .map(s->(NifiQueryKind)s)
                        .collect(Collectors.toList());
                createNifiQuery.startTask(this, nifiQueryKinds);
                runNifi.startTask(this, nifiQueryKinds );
            }
        }
        return 0;
    }
}



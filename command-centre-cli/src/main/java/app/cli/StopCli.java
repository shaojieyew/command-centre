package app.cli;

import app.cli.type.Component;
import app.spec.nifi.NifiQueryKind;
import app.spec.spark.SparkDeploymentKind;
import app.task.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.stream.Collectors;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class StopCli extends Cli {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(StopCli.class);
    @Autowired
    StopApp stopApp;
    @Autowired
    StopNifi stopNifi;
    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            if(getCliName()!=null){
                if(getCliName().length()==0){
                    throw new Exception("Invalid name");
                }
                stopApp.startTask(this);
            }else{
                List<SparkDeploymentKind> appDeployments = getSpecFile().stream()
                        .filter(s->s instanceof SparkDeploymentKind)
                        .map(s->(SparkDeploymentKind)s)
                        .collect(Collectors.toList());
                stopApp.startTask(this, appDeployments);
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.nifi.toString())){
            if(getCliQuery()!=null || getCliName()!=null || getCliId()!=null){
                stopNifi.startTask(this);
            }else{
                List<NifiQueryKind> nifiQueryKinds = getSpecFile().stream()
                        .filter(s->s instanceof NifiQueryKind)
                        .map(s->(NifiQueryKind)s)
                        .collect(Collectors.toList());
                stopNifi.startTask(this, nifiQueryKinds );
            }
        }else{
            List<NifiQueryKind> nifiQueryKinds = getSpecFile().stream()
                    .filter(s->s instanceof NifiQueryKind)
                    .map(s->(NifiQueryKind)s)
                    .collect(Collectors.toList());
            stopNifi.startTask(this, nifiQueryKinds );
            List<SparkDeploymentKind> appDeployments = getSpecFile().stream()
                    .filter(s->s instanceof SparkDeploymentKind)
                    .map(s->(SparkDeploymentKind)s)
                    .collect(Collectors.toList());
            stopApp.startTask(this, appDeployments);
        }
        return 0;
    }


}



package app.cli;

import app.cli.type.Component;
import app.spec.nifi.NifiQueryKind;
import app.spec.resource.GroupResourceKind;
import app.spec.spark.SparkDeploymentKind;
import app.task.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

import java.util.stream.Collectors;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class CreateCli extends Cli {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(CreateCli.class);
    @Autowired
    CreateSpec createSpec;
    @Autowired
    CreateNifiQuery createNifiQuery;
    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            createSpec.startTask(this, getSpecFile().stream().filter(s->s instanceof SparkDeploymentKind).collect(Collectors.toList()));
        }else if(getCliComponent().equalsIgnoreCase(Component.nifi.toString())){
            if(getCliName()!=null
            && getCliQuery()!=null){
                if(getCliName().length()==0){
                    throw new Exception("Invalid name");
                }
                createNifiQuery.startTask(this);
            }else{
                createSpec.startTask(this, getSpecFile().stream().filter(s->s instanceof NifiQueryKind).collect(Collectors.toList()));
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.file.toString())){
            createSpec.startTask(this, getSpecFile().stream().filter(s->s instanceof GroupResourceKind).collect(Collectors.toList()));
        }else{
            createSpec.startTask(this);
        }
        return 0;
    }


}



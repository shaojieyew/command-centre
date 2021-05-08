package app.cli;

import app.C2CliProperties;
import app.c2.model.Project;
import app.c2.service.ProjectService;
import app.cli.type.Component;
import app.spec.Kind;
import app.spec.MetadataKind;
import app.spec.nifi.NifiQueryKind;
import app.spec.resource.GroupResourceKind;
import app.spec.spark.AppDeploymentKind;
import app.task.*;
import app.util.YamlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class CreateCli extends Cli {
    @Autowired
    CreateSpec createSpec;
    @Autowired
    CreateNifiQuery createNifiQuery;
    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            createSpec.startTask(this, getSpecFile().stream().filter(s->s instanceof AppDeploymentKind).collect(Collectors.toList()));
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



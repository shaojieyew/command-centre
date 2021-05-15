package app.cli;

import app.cli.type.Component;
import app.spec.nifi.NifiQueryKind;
import app.spec.resource.GroupResourceKind;
import app.spec.spark.SparkDeploymentKind;
import app.task.DeleteApp;
import app.task.DeleteFile;
import app.task.DeleteNifiQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.stream.Collectors;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class DeleteCli extends Cli {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(DeleteCli.class);

    @Autowired
    DeleteApp deleteApp;
    @Autowired
    DeleteNifiQuery deleteNifiQuery;
    @Autowired
    DeleteFile deleteFile;

    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            if(getCliName()!=null){
                if(getCliName().length()==0){
                    throw new Exception("Invalid name");
                }
                deleteApp.startTask(this);
            }else{
                List<SparkDeploymentKind> appDeployments = getSpecFile().stream()
                        .filter(s->s instanceof SparkDeploymentKind)
                        .map(s->(SparkDeploymentKind)s)
                        .collect(Collectors.toList());
                deleteApp.startTask(this, appDeployments);
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.nifi.toString())){
            if(getCliName()!=null){
                if(getCliName().length()==0){
                    throw new Exception("Invalid query name");
                }
                deleteNifiQuery.startTask(this);
            }else{
                List<NifiQueryKind> nifiQueryKinds = getSpecFile().stream()
                        .filter(s->s instanceof NifiQueryKind)
                        .map(s->(NifiQueryKind)s)
                        .collect(Collectors.toList());
                deleteNifiQuery.startTask(this, nifiQueryKinds);
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.file.toString())){
            if(getCliName()!=null){
                if(getCliName().length()==0){
                    throw new Exception("Invalid file namespace and filename");
                }
                deleteFile.startTask(this);
            }else{
                List<GroupResourceKind> groupResourceKindList = getSpecFile().stream()
                        .filter(s->s instanceof GroupResourceKind)
                        .map(s->(GroupResourceKind)s)
                        .collect(Collectors.toList());
                deleteFile.startTask(this, groupResourceKindList);
            }
        }else{

            List<SparkDeploymentKind> appDeployments = getSpecFile().stream()
                    .filter(s->s instanceof SparkDeploymentKind)
                    .map(s->(SparkDeploymentKind)s)
                    .collect(Collectors.toList());
            deleteApp.startTask(this, appDeployments);

            List<NifiQueryKind> nifiQueryKinds = getSpecFile().stream()
                    .filter(s->s instanceof NifiQueryKind)
                    .map(s->(NifiQueryKind)s)
                    .collect(Collectors.toList());
            deleteNifiQuery.startTask(this, nifiQueryKinds);

            List<GroupResourceKind> groupResourceKindList = getSpecFile().stream()
                    .filter(s->s instanceof GroupResourceKind)
                    .map(s->(GroupResourceKind)s)
                    .collect(Collectors.toList());
            deleteFile.startTask(this, groupResourceKindList);
        }
        return 0;
    }


}



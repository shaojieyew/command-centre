package app.cli;

import app.cli.type.Component;
import app.spec.nifi.NifiQueryKind;
import app.spec.resource.GroupResourceKind;
import app.spec.spark.AppDeploymentKind;
import app.task.DeleteApp;
import app.task.DeleteFile;
import app.task.DeleteNifiQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.stream.Collectors;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class DeleteCli extends Cli {

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
                List<AppDeploymentKind> appDeployments = getSpecFile().stream()
                        .filter(s->s instanceof AppDeploymentKind)
                        .map(s->(AppDeploymentKind)s)
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
            deleteFile.startTask(this);
        }
        return 0;
    }


}



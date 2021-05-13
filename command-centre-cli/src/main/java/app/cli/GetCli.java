package app.cli;

import app.cli.type.Component;
import app.task.GetApp;
import app.task.GetCheckpoint;
import app.task.GetFile;
import app.task.GetNifiQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class GetCli extends Cli {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(GetCli.class);

    @Autowired
    GetApp getApp;
    @Autowired
    GetFile getFile;
    @Autowired
    GetNifiQuery getNifiQuery;
    @Autowired
    GetCheckpoint getCheckpoint;
    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            if(getCliName()!=null && getCliName().length()>0) {
                getApp.startTask(this);
            }else{
                throw new Exception("Invalid name");
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.nifi.toString())){
            if(getCliName()!=null && getCliName().length()>0) {
                getNifiQuery.startTask(this);
            }else{
                throw new Exception("Invalid name");
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.file.toString())){
            if(getCliId()!=null){
                getFile.startTask(this);
            }else{
                throw new Exception("Invalid file Id");
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.checkpoint.toString())){
            if(getCliQuery()!=null){
                getCheckpoint.startTask(this);
            }else{
                throw new Exception("Missing query");
            }
        }
        return 0;
    }


}



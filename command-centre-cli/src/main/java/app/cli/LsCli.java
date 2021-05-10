package app.cli;

import app.cli.type.Component;
import app.task.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class LsCli extends Cli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(LsCli.class);
    @Autowired
    ListTask listTask;
    @Autowired
    ListNifi listNifi;
    @Autowired
    ListApp listApp;
    @Autowired
    ListFile listFile;
    @Autowired
    ListNifiQuery listNifiQuery;

    @CommandLine.Option(names = {"--nifi-query"})
    private boolean list_nifi_query = false;
    @CommandLine.Option(names = {"--query-name"})
    private String query_name;

    @Override
    public Integer task() throws Exception {
        if(getCliComponent().equalsIgnoreCase(Component.spark.toString())){
            listApp.startTask(this);
        }else if(getCliComponent().equalsIgnoreCase(Component.nifi.toString())){
            if(list_nifi_query==true){
                listNifiQuery.startTask(this);
            }else{
                if(query_name!=null){
                    listNifi.startTask(this, query_name);
                }else{
                    listNifi.startTask(this);
                }
            }
        }else if(getCliComponent().equalsIgnoreCase(Component.file.toString())){
            listFile.startTask(this);
        }else{
            listTask.startTask(this);
        }
        return 0;
    }


}



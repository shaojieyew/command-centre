package app.task;

import app.cli.Cli;
import app.c2.model.NifiQuery;
import app.c2.service.AppService;
import app.c2.service.NifiQueryService;
import app.c2.services.nifi.NifiSvc;
import app.cli.StopCli;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StopNifi extends Task{

    @Override
    protected String getTaskName() {
        return StopNifi.class.getSimpleName() +((query ==null)? " id="+id : "query="+query);
    }

    @Autowired
    AppService appService;
    @Autowired
    StopNifi stopNifi;

    public void startTask(Cli cli, List<NifiQueryKind> kinds) throws Exception {
        kinds.forEach(k->k.getSpec().forEach(s-> {
            try {
                stopNifi.startTask(cli, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
    public void startTask(Cli cli, NifiQuerySpec spec) throws Exception {
        this.cli = cli;
        query = spec.getQuery();
        processType = spec.getType();
        startTask();
    }
    String query;
    String processType ;
    String id ;
    @Override
    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        id = cli.getCliId();
        query = cli.getCliQuery();
        processType = cli.getCliNifiProcessType();

        if(cli.getCliId()!=null) {
            startTask();
        }else{
            if(cli.getCliQuery()!=null){
                startTask();
            }else if (cli.getCliName()!=null){
                Optional<NifiQuery> nifiQuery = nifiQueryService.findByProjectIdAndName(cli.getProject().getId(),cli.getCliName());
                if(nifiQuery.isPresent()){
                    query = nifiQuery.get().getQuery();
                    processType = nifiQuery.get().getType();
                    startTask();
                }else{
                    throw new Exception("Invalid queryName");
                }
            }
        }
    }

    @Autowired
    NifiQueryService nifiQueryService;
    @Override
    protected void task() throws Exception {

        if(processType!=null && processType.equalsIgnoreCase(NifiQueryService.ProcessorType.Group.toString())){
            if(id!=null){
                nifiQueryService.stopProcessGroupById(cli.getProject().getId(), id, false);
            }else{
                nifiQueryService.stopProcessorGroup(cli.getProject().getId(),query,false);
            }
        }else{
            if(id!=null){
                nifiQueryService.stopProcessById(cli.getProject().getId(), id);
            }else{
                nifiQueryService.stopProcessor(cli.getProject().getId(), query, processType);
            }
        }
    }
}

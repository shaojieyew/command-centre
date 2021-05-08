package app.task;

import app.cli.Cli;
import app.c2.model.NifiQuery;
import app.c2.service.AppService;
import app.c2.service.NifiQueryService;
import app.c2.services.nifi.NifiSvc;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RunNifi extends Task{

    @Override
    protected String getTaskName() {
        return RunNifi.class.getSimpleName() +((query ==null)? " id="+id : "query="+query);
    }

    @Autowired
    AppService appService;
    @Autowired
    RunNifi RunNifi;

    NifiQuerySpec spec;
    String query;
    String processType ;
    String id ;

    public void startTask(Cli cli, List<NifiQueryKind> kinds) throws Exception {
        kinds.forEach(k->k.getSpec().forEach(s-> {
            try {
                RunNifi.startTask(cli, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public void startTask(Cli cli, NifiQuerySpec spec) throws Exception {
        this.cli = cli;
        this.spec = spec;
        query = spec.getQuery();
        processType = spec.getType();
        startTask();
    }

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
                query = cli.getCliQuery();
                processType = cli.getCliNifiProcessType();
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
                nifiQueryService.startProcessGroupById(cli.getProject().getId(), id, false);
            }else{
                nifiQueryService.startProcessorGroup(cli.getProject().getId(),query,false);
            }
        }else{
            if(id!=null){
                nifiQueryService.startProcessById(cli.getProject().getId(), id);
            }else{
                nifiQueryService.startProcessor(cli.getProject().getId(), query, processType);
            }
        }
    }
}

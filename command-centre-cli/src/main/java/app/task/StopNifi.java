package app.task;

import app.Cli;
import app.c2.service.AppService;
import app.c2.service.NifiQueryService;
import app.c2.services.nifi.NifiSvc;
import app.spec.nifi.NifiQuerySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StopNifi extends Task{

    @Override
    protected String getTaskName() {
        return StopNifi.class.getSimpleName() + " query="+query+ " processType="+processType;
    }

    @Autowired
    AppService appService;
    @Autowired
    StopNifi stopNifi;

    public void startTask(Cli cli, NifiQuerySpec spec) throws Exception {
        this.cli = cli;
        query = spec.getQuery();
        processType = spec.getType();
        onlyLeadingProcessor = spec.getScope() !=null && spec.getScope().equalsIgnoreCase(NifiQueryService.ProcessorScope.leadingProcessor.toString());
        startTask();
    }
    String query;
    String processType ;
    boolean onlyLeadingProcessor;
    @Override
    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        if(cli.get_query()!=null){
            query = cli.get_query();
            processType = cli.get_nifi_process_type();
            onlyLeadingProcessor = cli.is_onlyLeaderProcessor();
            startTask();
        }else{
            if(cli.getSpecFile().size()>0){
                cli.getSpecFile().forEach(c->c.getSpec().forEach(s->{
                    if(s instanceof NifiQuerySpec){
                        try {
                            startTask(cli, (NifiQuerySpec) s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }));
            }
        }
    }

    @Autowired
    NifiQueryService nifiQueryService;
    @Override
    protected void task() throws Exception {
        if(processType!=null && processType.equalsIgnoreCase(NifiQueryService.ProcessorType.Group.toString())){
            nifiQueryService.updateProcessGroup(cli.getProject().getId(),query,onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_STOPPED);
        }else{
            nifiQueryService.updateProcessor(cli.getProject().getId(), query, processType, NifiSvc.NIFI_RUN_STATUS_STOPPED);
        }
    }
}

package app.task;

import app.c2.model.NifiQuery;
import app.cli.Cli;
import app.util.PrintTable;
import app.c2.service.NifiQueryService;
import app.c2.service.nifi.NifiSvc;
import app.c2.service.nifi.NifiSvcFactory;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.davis.client.model.ProcessorStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ListNifi extends Task{

    Cli cli;

    @Override
    protected String getTaskName() {
        return RunApp.class.getSimpleName();
    }

    class NifiInfo{
        private String id;
        private String path;
        private String status;
        private String type;

        public NifiInfo(String id, String path, String status, String type) {
            this.id = id;
            this.path = path;
            this.status = status;
            this.type = type;
        }
    }
    @Autowired
    NifiQueryService nifiQueryService;


    public void startTask(Cli cli, String queryName) throws Exception {
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("path");
        columns.add("status");
        columns.add("type");
        this.cli = cli;
        List<NifiInfo> list = new ArrayList<>();
        Optional<NifiQuery> nifiQueryOptional = nifiQueryService.findByProjectIdAndName(cli.getProject().getId(), queryName);

        String type = nifiQueryOptional.get().getType();
        if(!nifiQueryOptional.isPresent()){
            throw new Exception("Invalid query name");
        }
        if(type!=null && type.equalsIgnoreCase(NifiQueryService.ProcessorType.Group.toString())){
            List<ProcessGroupStatusDTO> processGroup= nifiQueryService.findProcessGroup(cli.getProject().getId(),nifiQueryOptional.get().getQuery());
            for(ProcessGroupStatusDTO group: processGroup){
                NifiInfo info = new NifiInfo(group.getId(),group.getName(), null,NifiQueryService.ProcessorType.Group.toString());
                list.add(info);
            }
        }else{
            List<ProcessorStatusDTO> processors= nifiQueryService.findProcessor(cli.getProject().getId(),nifiQueryOptional.get().getQuery(), type);
            for(ProcessorStatusDTO processor: processors) {
                NifiInfo info = new NifiInfo(processor.getId(),processor.getName(),processor.getRunStatus(),processor.getAggregateSnapshot().getType());
                list.add(info);
            }
        }
        new PrintTable<NifiInfo>(list,columns);
    }

    public void startTask(Cli cli) throws Exception {
        String type = cli.getCliNifiProcessType();
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("path");
        columns.add("status");
        columns.add("type");
        this.cli = cli;
        List<NifiInfo> list = new ArrayList<>();
        NifiSvc svc= NifiSvcFactory.create(cli.getC2CliProperties());
        if(type!=null && type.equalsIgnoreCase(NifiQueryService.ProcessorType.Group.toString())){
            List<ProcessGroupStatusDTO> processGroup= nifiQueryService.findProcessGroup(cli.getProject().getId(),cli.getCliQuery());
            for(ProcessGroupStatusDTO group: processGroup){
                NifiInfo info = new NifiInfo(group.getId(),group.getName(), null,NifiQueryService.ProcessorType.Group.toString());
                list.add(info);
            }
        }else{
            List<ProcessorStatusDTO> processors= nifiQueryService.findProcessor(cli.getProject().getId(),cli.getCliQuery(), cli.getCliNifiProcessType());
            for(ProcessorStatusDTO processor: processors) {
                NifiInfo info = new NifiInfo(processor.getId(),processor.getName(),processor.getRunStatus(),processor.getAggregateSnapshot().getType());
                list.add(info);
            }
        }
        new PrintTable<NifiInfo>(list,columns,"Nifi Processor");
    }

    @Override
    protected void task() throws Exception {

    }
}

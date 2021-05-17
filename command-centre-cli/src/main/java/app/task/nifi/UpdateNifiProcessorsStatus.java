package app.task.nifi;

import app.c2.service.nifi.NifiSvc;
import app.c2.service.nifi.NifiSvcFactory;
import app.c2.service.nifi.model.NifiComponent;
import app.cli.NifiCli;
import app.spec.Kind;
import app.spec.nifi.NifiQuerySpec;
import app.task.Task;
import app.task.nifi.model.NifiInfo;
import com.davis.client.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class UpdateNifiProcessorsStatus extends Task {

    NifiCli cli;
    String queryName;
    String query;
    String id;
    String filterProcessType;
    boolean rootProcessorOnly;

    public UpdateNifiProcessorsStatus(NifiCli cli){
        super();
        this.cli = cli;
        this.queryName = cli.getCliName();
        this.id = cli.getCliId();
        this.query = cli.getCliQuery();
        this.filterProcessType = cli.getCliNifiProcessType();
        this.rootProcessorOnly = cli.isCliRootProcessor();
    }

    List<NifiInfo> getNifiInfoWithNifiSvc(String processType, String queryName, String query, String id) throws Exception {
        List<NifiInfo> list = new ArrayList<>();
        Set<NifiComponent> nifiComponents = nifiSvc.findNifiComponent(query, processType,id);
        for(NifiComponent processor: nifiComponents) {
            if(id == null || processor.getId().equalsIgnoreCase(id)|| processor.getFlowPathId().endsWith(id)) {
                NifiInfo info = new NifiInfo(processor,
                        queryName,
                        query);
                list.add(info);
            }
        }
        return list;
    }

    NifiSvc nifiSvc = null;
    @Override
    protected void task() throws Exception {
        nifiSvc = NifiSvcFactory.create(cli.getC2CliProperties());
        if(cli.getSpecFile().size()==0){
            List<NifiInfo> result = new ArrayList<>();
            String id = this.id;
            String queryName = this.queryName;
            String query = this.query;
            String filterProcessType = this.filterProcessType;
            boolean rootProcessorOnly = this.rootProcessorOnly;

            if(query != null){
                result.addAll(getNifiInfoWithNifiSvc(filterProcessType, queryName, query, id));
            }

            for (NifiInfo nifiInfo : result) {
                if(rootProcessorOnly){
                    nifiSvc.updateRunStatusById(nifiInfo.getId(), getStatus(),NifiSvc.Scope.Root.toString());
                }else{
                    nifiSvc.updateRunStatusById(nifiInfo.getId(), getStatus(),null);
                }
            }
        }else{
            for (Kind kind : cli.getSpecFile()) {
                for (Object s : kind.getSpec()) {
                    List<NifiInfo> result = new ArrayList<>();
                    NifiQuerySpec spec = ((NifiQuerySpec) s);
                    if(queryName!=null && !queryName.equals(((NifiQuerySpec) s).getName())){
                        break;
                    }
                    String queryName = spec.getName();
                    String id = spec.getId();
                    String query = spec.getQuery();
                    String filterProcessType = spec.getType();
                    String groupScope = spec.getScope();
                    result.addAll(getNifiInfoWithNifiSvc(filterProcessType, queryName, query, id));

                    for (NifiInfo nifiInfo : result) {
                            nifiSvc.updateRunStatusById(nifiInfo.getId(), getStatus(),groupScope);
                    }
                }
            }
        }
    }

    public abstract String getStatus();

    @Override
    protected void preTask() throws Exception {
    }

    @Override
    protected void postTask() throws Exception {
    }

    @Override
    protected String getTaskName() {
        return "Run Nifi Processors";
    }

}

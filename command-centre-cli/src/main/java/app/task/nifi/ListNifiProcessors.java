package app.task.nifi;

import app.c2.service.nifi.NifiSvc;
import app.c2.service.nifi.NifiSvcFactory;
import app.c2.service.nifi.model.NifiComponent;
import app.cli.NifiCli;
import app.spec.Kind;
import app.spec.nifi.NifiQuerySpec;
import app.task.Task;
import app.task.nifi.model.NifiInfo;
import app.util.PrintableTable;
import com.davis.client.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListNifiProcessors extends Task {

    NifiCli cli;
    String queryName;
    String query;
    String id;
    String filterProcessType;

    public ListNifiProcessors(NifiCli cli){
        super();
        this.cli = cli;
        this.queryName = cli.getCliName();
        this.id = cli.getCliId();
        this.query = cli.getCliQuery();
        this.filterProcessType = cli.getCliNifiProcessType();
    }

    private List<NifiInfo> getNifiInfoWithNifiSvc(String processType, String queryName, String query, String id) throws Exception {
        String keyword;
        keyword = (id!=null && id.length()>0)?id:query;
        List<NifiInfo> list = new ArrayList<>();
        NifiSvc nifiSvc = NifiSvcFactory.create(cli.getC2CliProperties());
        Set<NifiComponent> nifiComponents = nifiSvc.findNifiComponent(keyword, processType);
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


    List<NifiInfo> result = new ArrayList<>();

    @Override
    protected void task() throws Exception {
        result = new ArrayList<>();
        if(cli.getSpecFile().size()==0){
            String id = this.id;
            String queryName = this.queryName;
            String query = this.query;
            String filterProcessType = this.filterProcessType;

            if(query != null || id !=null){
                result.addAll(getNifiInfoWithNifiSvc(filterProcessType, queryName, query, id));
            }
        }else{
            for (Kind kind : cli.getSpecFile()) {
                for (Object s : kind.getSpec()) {
                    NifiQuerySpec spec = ((NifiQuerySpec) s);
                    if(queryName!=null && !queryName.equals(((NifiQuerySpec) s).getName())){
                        break;
                    }
                    String queryName = spec.getName();
                    String id = spec.getId();
                    String query = spec.getQuery();
                    String filterProcessType = spec.getType();
                    result.addAll(getNifiInfoWithNifiSvc(filterProcessType, queryName, query, id));
                }
            }
        }
    }

    @Override
    protected void preTask() throws Exception {
    }

    @Override
    protected void postTask() throws Exception {
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("name");
        columns.add("flowPath");
        columns.add("status");
        columns.add("type");
        columns.add("query");
        columns.add("queryName");
        new PrintableTable<NifiInfo>(result,columns).show();
    }

    @Override
    protected String getTaskName() {
        return "List Nifi Processors";
    }


}

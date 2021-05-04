package app.task;

import app.Cli;
import app.c2.model.NifiQuery;
import app.c2.service.NifiQueryService;
import app.spec.Kind;
import app.spec.Spec;
import app.spec.nifi.NifiQuerySpec;
import app.spec.resource.GroupResourceKind;
import app.spec.resource.GroupResourceSpec;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateNifiQuery extends Task{
    @Autowired
    CreateFileGroup createFileGroup;
    @Autowired
    CreateNifiQuery nifiQuery;

    public void startTask(Cli cli, NifiQuerySpec spec) throws Exception {
        this.cli = cli;
        this.queryName = spec.getName();
        this.query =spec.getQuery();
        this.processType = spec.getType();
        this.scope = spec.getScope();
        super.startTask(cli);
    }

    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        this.queryName = cli.get_name();
        this.query = cli.get_query();
        this.processType = cli.get_nifi_process_type();
        boolean leadingProcess = cli.is_onlyLeaderProcessor();
        this.scope = null;
        if(leadingProcess){
            scope = NifiQueryService.ProcessorScope.leadingProcessor.toString();
        }
        super.startTask(cli);
    }

    @Override
    protected String getTaskName() {
        return CreateNifiQuery.class.getSimpleName()+" "+queryName;
    }

    String queryName;
    String query;
    String processType;
    String scope;

    @Autowired
    NifiQueryService nifiQueryService;

    @Override
    protected void task() throws Exception {
        if(queryName==null ){
            throw new Exception("name of query not specified");
        }
        if(query==null){
            throw new Exception("query not specified");
        }

        NifiQuery nifiQuery = new NifiQuery();
        nifiQuery.setProjectId(cli.getProject().getId());
        nifiQuery.setName(queryName);
        nifiQuery.setQuery(query);
        nifiQuery.setType(processType);
        nifiQuery.setScope(scope);
        nifiQueryService.save(nifiQuery);
    }

}

package app.task;

import app.cli.Cli;
import app.c2.model.NifiQuery;
import app.c2.service.NifiQueryService;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import app.util.ConsoleHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateNifiQuery extends Task{
    @Autowired
    CreateNifiQuery createNifiQuery;

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
        this.queryName = cli.getCliName();
        this.query = cli.getCliQuery();
        this.processType = cli.getCliNifiProcessType();
        this.scope = null;
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
            throw  new Exception("name of query not specified, -n or --name");
        }
        if(query==null){
            throw new Exception("query not specified, -q or --query");
        }

        NifiQuery nifiQuery = new NifiQuery();
        nifiQuery.setProjectId(cli.getProject().getId());
        nifiQuery.setName(queryName);
        nifiQuery.setQuery(query);
        nifiQuery.setType(processType);
        nifiQuery.setScope(scope);
        nifiQueryService.save(nifiQuery);
    }

    public void startTask(Cli cli, List<NifiQueryKind> kinds) throws Exception {
        kinds.forEach(k->k.getSpec().forEach(s-> {
            try {
                createNifiQuery.startTask(cli, s);
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        }));
    }
}

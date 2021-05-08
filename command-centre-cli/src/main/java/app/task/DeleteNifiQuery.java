package app.task;

import app.cli.Cli;
import app.c2.service.*;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteNifiQuery extends Task {

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;
    @Autowired
    SparkService sparkService;

    String queryName;
    @Override
    protected String getTaskName() {
        return DeleteNifiQuery.class.getSimpleName() +" "+queryName;
    }

    public void startTask(Cli cli, String queryName) throws Exception {
        this.cli = cli;
        this.queryName = queryName;
        super.startTask(cli);
    }

    @Autowired DeleteNifiQuery deleteNifiQuery;

    public void startTask(Cli cli, List<NifiQueryKind> kinds) throws Exception {
        kinds.forEach(s->{
                try {
                    deleteNifiQuery.startTask(cli,s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }
    public void startTask(Cli cli, NifiQueryKind kind) throws Exception {
        kind.getSpec().forEach(s->{
            if(s instanceof NifiQuerySpec){
                try {
                    deleteNifiQuery.startTask(cli,s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startTask(Cli cli, NifiQuerySpec spec) throws Exception {
        this.cli = cli;
        this.queryName = spec.getName();
        super.startTask(cli);
    }

    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        this.queryName = cli.getCliName();
        super.startTask(cli);
    }

    @Autowired
    NifiQueryService nifiQueryService;

    @Override
    protected void task() throws Exception {
        nifiQueryService.delete(cli.getProject().getId(),queryName);
    }
}

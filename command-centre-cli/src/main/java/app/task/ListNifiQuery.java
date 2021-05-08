package app.task;

import app.cli.Cli;
import app.c2.model.NifiQuery;
import app.c2.service.NifiQueryService;
import app.util.PrintTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListNifiQuery extends Task{

    Cli cli;

    @Override
    protected String getTaskName() {
        return RunApp.class.getSimpleName();
    }

    @Autowired
    NifiQueryService nifiQueryService;

    public void startTask(Cli cli) throws Exception {
        List<String> columns = new ArrayList<>();
        columns.add("name");
        columns.add("query");
        columns.add("type");
        columns.add("scope");
        columns.add("updatedTimestamp");
        List<NifiQuery> nifiQueries = nifiQueryService.findAllNifiQuery();
        new PrintTable<NifiQuery>(nifiQueries, columns);
    }

    @Override
    protected void task() throws Exception {

    }
}

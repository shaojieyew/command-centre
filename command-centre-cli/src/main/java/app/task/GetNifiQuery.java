package app.task;

import app.c2.model.App;
import app.c2.model.NifiQuery;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.NifiQueryService;
import app.c2.service.ProjectService;
import app.cli.Cli;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import app.spec.resource.Resource;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
import app.util.ConsoleHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GetNifiQuery extends Task {

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;
    public NifiQueryKind convertQueryToSpec(NifiQuery query) throws IOException {
        NifiQuerySpec spec = new NifiQuerySpec();
        spec.setName(query.getName());
        spec.setQuery(query.getQuery());
        spec.setScope(query.getScope());
        spec.setType(query.getType());

        NifiQueryKind nifiQueryKind = new NifiQueryKind();
        List<NifiQuerySpec> specs = new ArrayList<>();
        specs.add(spec);
        nifiQueryKind.setSpec(specs);
        nifiQueryKind.setKind("NifiQuery");
        return nifiQueryKind;
    }

    @Autowired
    NifiQueryService nifiQueryService;
    @Override
    public void startTask(Cli cli) throws Exception {
        Optional<NifiQuery> optionalNifiQuery = nifiQueryService.findByProjectIdAndName(cli.getProject().getId(), cli.getCliName());
        if(optionalNifiQuery.isPresent()){
            NifiQuery query = optionalNifiQuery.get();
            NifiQueryKind kind = convertQueryToSpec(query);
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String specStr = mapper.writeValueAsString(kind);
            ConsoleHelper.console.display(specStr);
        }else{
            throw new Exception("Invalid app name");
        }
    }

    @Override
    protected String getTaskName() {
        return null;
    }

    @Override
    protected void task() throws Exception {

    }
}

package app.task;

import app.cli.Cli;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import app.spec.resource.Resource;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
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
public class GetApp extends Task {

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;
    public AppDeploymentKind convertAppToSpec(App app) throws IOException {
        AppDeploymentSpec spec = new AppDeploymentSpec();
        spec.setName(app.getName());
        spec.setJarGroupId(app.getJarGroupId());
        spec.setJarArtifactId(app.getJarArtifactId());
        spec.setJarVersion(app.getJarVersion());
        spec.setMainClass(app.getJarMainClass());
        spec.setJarArgs(app.getJarArgs());
        spec.setSparkArgs(app.getSparkArgs());
        spec.setNamespace(spec.getNamespace());

        List<Resource> resources= app.getFileIds().stream().map(f->fileStorageService.getFile(f).get())
                .map(f->{
                    Resource r = new Resource();
                    r.setName(f.getName());
                    r.setSource(f.getSource());
                    r.setType(f.getSourceType());
                    return r;
                }).collect(Collectors.toList());
        spec.setResources(resources);

        AppDeploymentKind appDeploymentKind = new AppDeploymentKind();
        List<AppDeploymentSpec> specs = new ArrayList<>();
        specs.add(spec);
        appDeploymentKind.setSpec(specs);
        appDeploymentKind.setKind("AppDeployment");
        return appDeploymentKind;
    }

    @Override
    public void startTask(Cli cli) throws Exception {
            Optional<App> optionalApp = appService.findApp(cli.getProject().getId(), cli.getCliName());
            if(optionalApp.isPresent()){
                App app = optionalApp.get();
                AppDeploymentKind appDeploymentKind = convertAppToSpec(app);
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                String appSpec = mapper.writeValueAsString(appDeploymentKind);
                System.out.println(appSpec);

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

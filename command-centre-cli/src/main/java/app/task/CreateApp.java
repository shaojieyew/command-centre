package app.task;

import app.cli.Cli;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.spec.Kind;
import app.spec.Spec;
import app.spec.resource.Resource;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CreateApp extends Task{

    AppDeploymentSpec spec = null;
    Cli cli = null;
    @Autowired CreateApp createApp;
    public void startTask(Cli cli, List<AppDeploymentKind> kinds) throws Exception {
        for(Kind k : kinds){
            for(Object s: k.getSpec()){
                createApp.startTask(cli, (AppDeploymentSpec) s);
            }
        }
    }
    public void startTask(Cli cli, AppDeploymentSpec spec) throws Exception {
        this.cli=cli;
        this.spec = spec;
        startTask();
    }

    @Override
    protected String getTaskName() {
        return CreateApp.class.getSimpleName() +" "+spec.getName();
    }

    @Override
    protected void task() throws IOException, GitAPIException {
        createApp(cli, spec);
    }

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    AppService appService;
    private void createApp(Cli cli, AppDeploymentSpec spec) throws IOException, GitAPIException {
        Optional<App> appOptional = appService.findApp(cli.getProject().getId(), spec.getName());
        App app = new App();
        if(appOptional.isPresent()){
            app = appOptional.get();
        }
        Set<Long> fileIds = new HashSet<>();
        if(spec.getResources()!=null) {
            for (Resource resource : spec.getResources()) {
                long fileId = 0;
                switch (resource.getType().toUpperCase()) {
                    case "GIT":
                        String[] sourceArr = resource.getSource().split("/-/");
                        String remoteUrl = sourceArr[0];
                        String branch = sourceArr[1];
                        String path = sourceArr[2];
                        fileId =  fileStorageService.saveFile(remoteUrl, branch, path, cli.getProject().getId(), resource.getName()).getId();
                        break;
                    case "LOCAL":
                        fileId =  fileStorageService.saveFile(new File(resource.getSource()), resource.getName(), cli.getProject().getId()).getId();
                        break;
                    case "STRING":
                        fileId =  fileStorageService.saveFile(resource.getSource(), resource.getName(), cli.getProject().getId()).getId();
                        break;
                    default:
                }
                if(fileId!=0){
                    fileIds.add(fileId);
                }
            }
        }

        app.setName(spec.getName());
        app.setProjectId(cli.getProject().getId());
        app.setJarGroupId(spec.getJarGroupId());
        app.setJarArtifactId(spec.getJarArtifactId());
        app.setJarVersion(spec.getJarVersion());
        app.setJarMainClass(spec.getMainClass());
        app.setJarArgs(spec.getJarArgs());
        app.setSparkArgs(spec.getSparkArgs());
        app.setFileIds(fileIds);
        app.setNamespace(spec.getNamespace());

        appService.save(app);
    }
}

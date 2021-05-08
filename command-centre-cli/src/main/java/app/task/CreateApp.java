package app.task;

import app.cli.Cli;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
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
        kinds.forEach(k->{
            k.getSpec().forEach(s-> {
                try {
                    createApp.startTask(cli, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
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
    protected void task() throws IOException {
        createApp(cli, spec);
    }

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    AppService appService;
    private void createApp(Cli cli, AppDeploymentSpec spec) throws IOException {
        Optional<App> appOptional = appService.findApp(cli.getProject().getId(), spec.getName());
        App app = new App();
        if(appOptional.isPresent()){
            app = appOptional.get();
        }
        Set<Long> fileIds = new HashSet<>();
        if(spec.getResources()!=null) {

            fileIds = spec.getResources().stream().map(resource -> {
                long fileId = 0;
                switch (resource.getType().toUpperCase()) {
                    case "GIT":
                        String[] sourceArr = resource.getSource().split("/-/");
                        String remoteUrl = sourceArr[0];
                        String branch = sourceArr[1];
                        String path = sourceArr[2];
                        try {
                            app.c2.model.File file = fileStorageService.saveFile(remoteUrl, branch, path, cli.getProject().getId());
                            fileId = file.getId();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GitAPIException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "LOCAL":
                        try {
                            app.c2.model.File file = fileStorageService.saveFile(new File(resource.getSource()), cli.getProject().getId());
                            fileId = file.getId();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "STRING":
                        try {
                            app.c2.model.File file = fileStorageService.saveFile(resource.getSource(), resource.getName(), cli.getProject().getId());
                            fileId = file.getId();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                }
                return fileId;
            }).collect(Collectors.toSet());
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

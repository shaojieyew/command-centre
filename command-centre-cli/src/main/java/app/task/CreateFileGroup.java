package app.task;

import app.cli.Cli;
import app.c2.service.FileStorageService;
import app.spec.resource.GroupResourceSpec;
import app.spec.resource.Resource;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class CreateFileGroup extends Task{

    GroupResourceSpec spec = null;
    Cli cli = null;

    public void startTask(Cli cli, GroupResourceSpec spec) throws Exception {
        this.cli=cli;
        this.spec = spec;
        startTask();
    }

    @Override
    protected String getTaskName() {
        return CreateFileGroup.class.getSimpleName() +" "+spec.getName();
    }

    @Override
    protected void task() throws IOException, GitAPIException {
        for(Resource resource: spec.getResources()){
            uploadResource(resource, cli.getProject().getId(), resource.getName());
        }
    }

    @Autowired
    FileStorageService fileStorageService;
    private void uploadResource(Resource resource, long projectId, String namespace) throws IOException, GitAPIException {
        switch (resource.getType().toUpperCase()){
            case "GIT":
                String[] sourceArr = resource.getSource().split("/-/");
                String remoteUrl = sourceArr[0];
                String branch = sourceArr[1];
                String path = sourceArr[2];
                fileStorageService.addGitFileToProject(remoteUrl,branch,path, projectId, namespace);
                break;
            case "LOCAL":
                fileStorageService.addUploadedFileToProject(new File(resource.getSource()), projectId, namespace);
                break;
            case "STRING":
                fileStorageService.addStringContentToProject(resource.getSource(), resource.getName(), projectId, namespace);
                break;
            default:
        }
    }
}

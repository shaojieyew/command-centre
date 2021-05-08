package app.task;

import app.cli.Cli;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import app.spec.resource.GroupResourceKind;
import app.spec.resource.GroupResourceSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteFile extends Task {

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;


    @Override
    protected String getTaskName() {
        return DeleteFile.class.getSimpleName() +" "+path;
    }

    public void startTask(Cli cli, List<GroupResourceKind> kinds) throws Exception {
        kinds.forEach(s-> {
            try {
                startTask(cli, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void startTask(Cli cli, GroupResourceKind kind) throws Exception {
        kind.getSpec().forEach(s-> {
            try {
                startTask(cli, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    String path = null;
    public void startTask(Cli cli, GroupResourceSpec spec) throws Exception {
        this.cli = cli;
        spec.getResources().forEach(r-> {
            try {
                startTask(cli, spec.getName()+"/"+r.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void startTask(Cli cli, String path) throws Exception {
        this.cli = cli;
        this.path = path;
        if (path == null || path.length() == 0) {
            throw new Exception("Invalid resource path");
        }
        startTask(cli);
    }

    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        this.path = cli.getCliName();
        if (path == null || path.length() == 0) {
            throw new Exception("Invalid resource path");
        }
        startTask(cli);
    }

    @Override
    protected void task() throws Exception {
        if(path!=null){
            String path = cli.getCliName();
            String filename = null;
            String namespace = null;
            if(path.contains("/")){
                namespace = path.split("/")[0];
                filename = path.split("/")[1];
                projectService.deleteFile(cli.getProject().getId(),filename, namespace);
            }else{
                namespace = path;
                projectService.deleteFiles(cli.getProject().getId(),namespace);
            }
        }else{
            throw new Exception("Invalid resource path");
        }
    }
}

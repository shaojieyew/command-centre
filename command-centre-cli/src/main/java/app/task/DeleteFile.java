package app.task;

import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return DeleteFile.class.getSimpleName() +" "+cli.get_name();
    }

    @Override
    protected void task() throws Exception {
        if(cli.get_name()!=null){
            String path = cli.get_name();
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
            throw new Exception("Invalid name");
        }
    }
}

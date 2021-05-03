package app.task;

import app.Cli;
import app.util.PrintTable;
import app.c2.model.File;
import app.c2.model.Project;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GetFile extends Task {

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;

    @Override
    public void startTask(Cli cli) throws Exception {
        if(cli.get_id()!=null){
            long fileId = Long.parseLong(cli.get_id());
            Optional<File> optionalFile = fileStorageService.getFile(fileId);
            if(optionalFile.isPresent()){
                String s = new String(optionalFile.get().getFileBlob());
                ArrayList<String> fileHeader = new ArrayList<>();
                fileHeader.add("id");
                fileHeader.add("name");
                fileHeader.add("sourceType");
                fileHeader.add("source");
                Optional<Project> optionalProject = projectService.findById(cli.getProject().getId());
                List<File> f = new ArrayList<>();
                f.add(optionalFile.get());
                new PrintTable(f,fileHeader);
                System.out.println(s);
            }else{
                throw new Exception("Invalid fileId");
            }
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

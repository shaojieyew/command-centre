package app.task;

import app.Cli;
import app.util.PrintTable;
import app.c2.model.Project;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListFile extends Task{

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;

    Cli cli;

    @Override
    protected String getTaskName() {
        return RunApp.class.getSimpleName();
    }

    public void startTask(Cli cli) throws Exception {
        this.cli = cli;

        ArrayList<String> fileHeader = new ArrayList<>();
        fileHeader.add("id");
        fileHeader.add("name");
        fileHeader.add("sourceType");
        fileHeader.add("source");
        Optional<Project> optionalProject = projectService.findById(cli.getProject().getId());
        if(optionalProject.isPresent()){
            Map<String, Set<Long>> fileMapping = optionalProject.get().getFileIds();
            for(String namespace: fileMapping.keySet()){
                System.out.println("Files in "+namespace+" namespace: ");
                new PrintTable(fileMapping.get(namespace)
                        .stream().map(id->fileStorageService.getFile(id).orElseGet(null))
                        .collect(Collectors.toList()),
                        fileHeader);
            }
        }
    }

    @Override
    protected void task() throws Exception {

    }
}

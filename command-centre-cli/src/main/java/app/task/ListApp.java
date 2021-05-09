package app.task;

import app.cli.Cli;
import app.util.PrintTable;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListApp extends Task{

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
        List<App> apps = appService.findAllAppStatus(cli.getProject().getId()).stream().filter(s->{
            if(cli.getCliName()==null){
                return true;
            }
            return s.getName().toLowerCase().contains(cli.getCliName().toLowerCase());
        }).collect(Collectors.toList());
        ArrayList<String> header = new ArrayList<>();
        header.add("updatedTimestamp");
        header.add("name");
        header.add("jarMainClass");
        header.add("jarVersion");
        header.add("jarArgs");
        header.add("namespace");
        header.add("yarnAppId");
        header.add("yarnStatus");
        new PrintTable(apps, header,"Spark Applications");
    }

    @Override
    protected void task() throws Exception {

    }
}

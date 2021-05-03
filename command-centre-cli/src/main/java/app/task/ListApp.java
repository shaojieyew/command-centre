package app.task;

import app.Cli;
import app.util.PrintTable;
import app.c2.model.App;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

        System.out.println("Spark Application: ");
        List<App> apps = appService.findAllAppStatus(cli.getProject().getId());
        ArrayList<String> header = new ArrayList<>();
        header.add("updatedTimestamp");
        header.add("name");
        header.add("jarMainClass");
        header.add("jarArgs");
        header.add("jarVersion");
        header.add("namespace");
        header.add("yarnAppId");
        header.add("yarnStatus");
        new PrintTable(apps, header);

    }

    @Override
    protected void task() throws Exception {

    }
}

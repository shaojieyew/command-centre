package app.task;

import app.cli.Cli;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListTask  extends Task{

    @Autowired
    AppService appService;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ProjectService projectService;
    @Autowired
    ListApp listApp;
    @Autowired
    ListFile listFile;

    Cli cli;

    @Override
    protected String getTaskName() {
        return RunApp.class.getSimpleName();
    }

    public void startTask(Cli cli) throws Exception {
        this.cli = cli;
        listApp.startTask(cli);
        listFile.startTask(cli);
    }

    @Override
    protected void task() throws Exception {

    }
}

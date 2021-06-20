package app.task.hdfs;

import app.c2.service.hdfs.FileBackupSvc;
import app.c2.service.hdfs.model.Backup;
import app.c2.service.kafka.CheckpointBackupSvcFactory;
import app.cli.CheckpointCli;
import app.task.Task;
import app.util.PrintableTable;

import java.util.*;

public class ListBackups extends Task {

    CheckpointCli cli;
    public ListBackups(CheckpointCli cli){
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Listing Checkpoint backups";
    }

    @Override
    protected void preTask() throws Exception {

    }


    List<Backup> backups;
    @Override
    protected void task() throws Exception {
        FileBackupSvc fileBackupSvc = CheckpointBackupSvcFactory.create(cli.getC2CliProperties());
        backups =fileBackupSvc.getAllBackup();
    }

    @Override
    public void postTask(){
        new PrintableTable(backups, null, "Checkpoint Backups").show();
    }
}

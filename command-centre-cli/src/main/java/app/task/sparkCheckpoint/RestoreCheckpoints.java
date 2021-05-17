package app.task.sparkCheckpoint;

import app.c2.service.hdfs.FileBackupSvc;
import app.c2.service.kafka.CheckpointBackupSvcFactory;
import app.c2.service.kafka.SparkCheckpointSvc;
import app.c2.service.kafka.SparkCheckpointSvcFactory;
import app.cli.CheckpointCli;
import app.task.Task;
import app.util.ConsoleHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RestoreCheckpoints extends Task {

    CheckpointCli cli;
    public RestoreCheckpoints(CheckpointCli cli){
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Restoring Backup-ed Checkpoints";
    }

    @Override
    protected void preTask() throws Exception {
        if(cli.getCliId()==null){
            throw new Exception("Missing id");
        }
    }

    @Override
    protected void task() throws Exception {
        String id = cli.getCliId();
        FileBackupSvc fileBackupSvc = CheckpointBackupSvcFactory.create(cli.getC2CliProperties());
        fileBackupSvc.restoreBackup(id);
    }

    @Override
    public void postTask(){

    }
}

package app.task.hdfs;

import app.c2.service.hdfs.FileBackupSvc;
import app.c2.service.kafka.CheckpointBackupSvcFactory;
import app.cli.CheckpointCli;
import app.task.Task;

public class RestoreBackups extends Task {

    CheckpointCli cli;
    public RestoreBackups(CheckpointCli cli){
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

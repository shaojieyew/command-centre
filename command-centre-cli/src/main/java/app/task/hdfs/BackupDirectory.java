package app.task.hdfs;

import app.c2.service.hdfs.FileBackupSvc;
import app.c2.service.kafka.CheckpointBackupSvcFactory;
import app.cli.CheckpointCli;
import app.task.Task;

public class BackupDirectory extends Task {

    CheckpointCli cli;
    public BackupDirectory(CheckpointCli cli){
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Moving Directory to Backup folder";
    }

    @Override
    protected void preTask() throws Exception {

    }

    @Override
    protected void task() throws Exception {
        String directoryToBackup = cli.getCliQuery();
        String[] directories ={directoryToBackup};
        String backupName = BackupDirectory.class.getName()+"_"+System.currentTimeMillis();
        if(cli.getCliName()!=null){
            backupName = cli.getCliName();
        }
        FileBackupSvc fileBackupSvc = CheckpointBackupSvcFactory.create(cli.getC2CliProperties());
        fileBackupSvc.backup(backupName, directories);
    }

    @Override
    public void postTask(){

    }
}

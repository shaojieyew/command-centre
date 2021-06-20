package app.cli;

import app.cli.type.Action;
import app.task.hdfs.BackupDirectory;
import app.task.hdfs.ListBackups;
import app.task.hdfs.RestoreBackups;
import app.task.sparkCheckpoint.*;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class CheckpointCli extends Cli {
    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(CheckpointCli.class);
    @CommandLine.Option(names = {"--show-backlog"}, description = "")
    private boolean cliShowBacklogOnly = false;
    @CommandLine.Option(names = {"--backup"}, description = "")
    private boolean cliBackupMode = false;

    public boolean isCliShowBacklogOnly() {
        return cliShowBacklogOnly;
    }

    @Override
    public Integer task() throws Exception {
        if(!cliBackupMode){
            if(getCliAction().equalsIgnoreCase(Action.ls.toString())){
                new ListCheckpoints(this).startTask();
            }
            if(getCliAction().equalsIgnoreCase(Action.get.toString())){
                new GetCheckpoints(this).startTask();
            }
        }else{
            if(getCliAction().equalsIgnoreCase(Action.ls.toString())){
                new ListBackups(this).startTask();
            }
            if(getCliAction().equalsIgnoreCase(Action.mv.toString())){
                new BackupDirectory(this).startTask();
            }
            if(getCliAction().equalsIgnoreCase(Action.restore.toString())){
                new RestoreBackups(this).startTask();
            }
        }
        return 0;
    }

}



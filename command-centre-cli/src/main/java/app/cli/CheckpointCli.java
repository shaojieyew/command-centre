package app.cli;

import app.cli.type.Action;
import app.task.sparkCheckpoint.BackupCheckpoints;
import app.task.sparkCheckpoint.ListBackups;
import app.task.sparkCheckpoint.ListCheckpoints;
import app.task.sparkCheckpoint.RestoreCheckpoints;
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
        }else{
            if(getCliAction().equalsIgnoreCase(Action.ls.toString())){
                new ListBackups(this).startTask();
            }
            if(getCliAction().equalsIgnoreCase(Action.mv.toString())){
                new BackupCheckpoints(this).startTask();
            }
            if(getCliAction().equalsIgnoreCase(Action.restore.toString())){
                new RestoreCheckpoints(this).startTask();
            }
        }
        return 0;
    }

}



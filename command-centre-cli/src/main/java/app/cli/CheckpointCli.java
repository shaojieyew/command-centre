package app.cli;

import app.cli.type.Action;
import app.task.hdfs.BackupDirectory;
import app.task.hdfs.ListBackups;
import app.task.hdfs.RestoreBackups;
import app.task.spark_checkpoint.*;
import app.util.ConsoleHelper;
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
    public void printHelp() {
        ConsoleHelper.console.display("Command Actions:");
        ConsoleHelper.console.display("ls\t\t\t\t\tlist all checkpoint of given hdfs directory specify in --query. \n\t\t\t\t\tWhen --show-backlog is specified, only offsets of topic/partitions that have backlog will be listed. eg. 'c2 checkpoint ls -q \"\\user\\all_checkpoints\"' --show-backlog \n\t\t\t\t\tWhen --backup is specified, it will list all the backup-ed checkpoint. 'eg. c2 checkpoint ls --backup'.");
        ConsoleHelper.console.display("get\t\t\t\t\tPrint out the checkpoint file in the queried directory eg. 'c2 checkpoint get -q \"\\user\\all_checkpoints\"'.");
        ConsoleHelper.console.display("mv\t\t\t\t\tBackup checkpoint. This action will move all the checkpoints in the directory to a backup directory; specified in setting.yml. It does not create a copy. eg. 'c2.sh checkpoint mv --backup -q \"\\user\\all_checkpoints\"'.");
        ConsoleHelper.console.display("restore \t\t\tRestore the checkpoints to its original location. It does not create a copy. eg. c2 checkpoint restore --backup --id 3034194245");

        ConsoleHelper.console.display("");
        ConsoleHelper.console.display("Command arguments:");
        ConsoleHelper.console.display("-i\t--id\t\t\tSpecify backup id assigned by backup manager");
        ConsoleHelper.console.display("-q\t--query\t\t\tSpecify path of hdfs directory");
        ConsoleHelper.console.display("\t--backup\t\tswitch to backup manager mode");
        ConsoleHelper.console.display("\t--show-backlog\tswitch to backup manager mode");
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



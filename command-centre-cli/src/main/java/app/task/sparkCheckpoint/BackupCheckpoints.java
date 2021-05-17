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

public class BackupCheckpoints extends Task {

    CheckpointCli cli;
    public BackupCheckpoints(CheckpointCli cli){
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Moving Checkpoints to Backup folder";
    }

    @Override
    protected void preTask() throws Exception {

    }

    @Override
    protected void task() throws Exception {
        Set<String> checkpointLocations = new HashSet<>();
        checkpointLocations.add(cli.getCliQuery());

        SparkCheckpointSvc sparkCheckpointSvc
                = SparkCheckpointSvcFactory.create(cli.getC2CliProperties(), cli.getC2CliProperties().getTmpDirectory());
        Map<String, Map<String, Long>> listResult = sparkCheckpointSvc.getKafkaBacklogWithCheckpoints(checkpointLocations);
        List<String> checkpointPaths = listResult.entrySet().stream().flatMap(e->{
            return e.getValue().entrySet().stream().map(topicPartitionLongEntry -> {
                return new SparkCheckpoint(e.getKey(), topicPartitionLongEntry.getKey(), topicPartitionLongEntry.getValue());
            });
        }).filter(r->{
            if(cli.isCliShowBacklogOnly()){
                return r.offsetBacklog>0;
            }else{
                return true;
            }
        }).map(f->f.checkpointPath).distinct().collect(Collectors.toList());

        if(checkpointPaths.size()==0){
            ConsoleHelper.console.display(new Exception("No checkpoint directory found"));
        }else{
            String backupName = "CheckpointBackup"+System.currentTimeMillis();
            if(cli.getCliName()!=null){
                backupName = cli.getCliName();
            }

            FileBackupSvc fileBackupSvc = CheckpointBackupSvcFactory.create(cli.getC2CliProperties());
            fileBackupSvc.backup(backupName,checkpointPaths);
        }
    }

    @Override
    public void postTask(){

    }
}

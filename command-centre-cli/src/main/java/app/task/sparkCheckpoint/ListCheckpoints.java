package app.task.sparkCheckpoint;

import app.c2.service.kafka.SparkCheckpointSvc;
import app.c2.service.kafka.SparkCheckpointSvcFactory;
import app.cli.CheckpointCli;
import app.task.Task;
import app.util.PrintableTable;

import java.util.*;
import java.util.stream.Collectors;

public class ListCheckpoints extends Task {

    CheckpointCli cli;
    public ListCheckpoints(CheckpointCli cli){
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Listing Spark Streaming Checkpoint";
    }



    @Override
    protected void preTask() throws Exception {
        if(cli.getCliQuery()==null){
            throw new Exception("Missing checkpoint -q <path>");
        }
    }
    List<SparkCheckpoint> result= new ArrayList<>();

    @Override
    protected void task() throws Exception {
        Set<String> checkpointLocations = new HashSet<>();
        checkpointLocations.add(cli.getCliQuery());

        SparkCheckpointSvc sparkCheckpointSvc
                = SparkCheckpointSvcFactory.create(cli.getC2CliProperties(), cli.getC2CliProperties().getTmpDirectory());
        Map<String, Map<String, Long>> listResult = sparkCheckpointSvc.getKafkaBacklogWithCheckpoints(checkpointLocations);
        result = listResult.entrySet().stream().flatMap(e->{
            return e.getValue().entrySet().stream().map(topicPartitionLongEntry -> {
                return new SparkCheckpoint(e.getKey(), topicPartitionLongEntry.getKey(), topicPartitionLongEntry.getValue());
            });
        }).filter(r->{
            if(cli.isCliShowBacklogOnly()){
                return r.offsetBacklog>0;
            }else{
                return true;
            }
        }).collect(Collectors.toList());
    }


    @Override
    public void postTask(){
        List<String> columns = new ArrayList<>();
        columns.add("checkpointPath");
        columns.add("topic");
        columns.add("offsetBacklog");
        new PrintableTable<SparkCheckpoint>(result, columns).show();
    }
}

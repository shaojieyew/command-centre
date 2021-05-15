package app.task;

import app.c2.service.kafka.SparkCheckpointSvc;
import app.c2.service.kafka.SparkCheckpointSvcFactory;
import app.cli.LsCli;
import app.util.PrintTable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListCheckpoint extends Task{


    @Override
    protected String getTaskName() {
        return ListCheckpoint.class.getSimpleName();
    }



    @Override
    protected void task() throws Exception {
        SparkCheckpointSvc sparkCheckpointSvc
                = SparkCheckpointSvcFactory.create(cli.getC2CliProperties());
        Set<String> checkpointLocations = new HashSet<>();
        checkpointLocations.add(cli.getCliQuery());
        listResult = sparkCheckpointSvc.getKafkaBacklogWithCheckpoints(checkpointLocations);
    }

    class SparkCheckpoint{
        String path;
        String topic;
        long offsetBacklog;

        public SparkCheckpoint(String path, String topic, long offsetBacklog, String checkpoint) {
            this.path = path;
            this.topic = topic;
            this.offsetBacklog = offsetBacklog;
        }
    }

    Map<String, Map<String, Long>> listResult;
    @Override
    public void postTask(){
        List<SparkCheckpoint> list = listResult.entrySet().stream().flatMap(e->{
            return e.getValue().entrySet().stream().map(topicPartitionLongEntry -> {
                return new SparkCheckpoint(e.getKey(), topicPartitionLongEntry.getKey(), topicPartitionLongEntry.getValue(), "");
            });
        }).filter(r->{
            if(cli instanceof LsCli && ((LsCli)cli).isShow_backlog_only()){
                return r.offsetBacklog>0;
            }else{
                return true;
            }
        }).collect(Collectors.toList());

        List<String> columns = new ArrayList<>();
        columns.add("path");
        columns.add("topic");
        columns.add("offsetBacklog");
        columns.add("checkpoint");
        new PrintTable<SparkCheckpoint>(list, columns);
    }
}

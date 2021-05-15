package app.task;

import app.c2.service.kafka.SparkCheckpointSvc;
import app.c2.service.kafka.SparkCheckpointSvcFactory;
import app.util.ConsoleHelper;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GetCheckpoint extends Task{

    @Override
    protected String getTaskName() {
        return GetCheckpoint.class.getSimpleName();
    }

    Map<String, Map<String, Long>> listResult;
    String result;
    SparkCheckpointSvc sparkCheckpointSvc;
    @Override
    protected void task() throws Exception {
        sparkCheckpointSvc
                = SparkCheckpointSvcFactory.create(cli.getC2CliProperties());
        Set<String> checkpointLocations = new HashSet<>();
        checkpointLocations.add(cli.getCliQuery());
        listResult = sparkCheckpointSvc.getKafkaBacklogWithCheckpoints(checkpointLocations);
    }

    @Override
    public void postTask(){
        listResult.keySet().forEach(r->{
            try {
                result = sparkCheckpointSvc.getCheckpointFile(r);
                ConsoleHelper.console.display("checkpoint directory: "+r);
                ConsoleHelper.console.display(result+"\n");
            } catch (Exception e) {
                ConsoleHelper.console.display(e);
            }
        });
    }
}

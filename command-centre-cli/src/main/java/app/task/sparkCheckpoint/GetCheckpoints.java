package app.task.sparkCheckpoint;

import app.c2.service.kafka.SparkCheckpointSvc;
import app.c2.service.kafka.SparkCheckpointSvcFactory;
import app.cli.CheckpointCli;
import app.task.Task;
import app.util.ConsoleHelper;
import app.util.PrintableTable;

import java.util.*;
import java.util.stream.Collectors;

public class GetCheckpoints extends Task {

    CheckpointCli cli;
    public GetCheckpoints(CheckpointCli cli){
        this.cli = cli;
    }

    @Override
    protected String getTaskName() {
        return "Getting Spark Streaming Checkpoint";
    }



    @Override
    protected void preTask() throws Exception {
        if(cli.getCliQuery()==null){
            throw new Exception("Missing checkpoint -q <path>");
        }
    }

    Set<String> checkpointsFolder;
    SparkCheckpointSvc sparkCheckpointSvc;
    @Override
    protected void task() throws Exception {
        Set<String> checkpointLocations = new HashSet<>();
        checkpointLocations.add(cli.getCliQuery());
        sparkCheckpointSvc = SparkCheckpointSvcFactory.create(cli.getC2CliProperties(), cli.getC2CliProperties().getTmpDirectory());
        checkpointsFolder = sparkCheckpointSvc.getCheckpoints(checkpointLocations).keySet();
    }


    @Override
    public void postTask() throws Exception {
        for (String checkpointDir : checkpointsFolder) {
            ConsoleHelper.console.display("Checkpoint: "+checkpointDir);
            ConsoleHelper.console.display(sparkCheckpointSvc.getCheckpointFile(checkpointDir));
            ConsoleHelper.console.display(" ");
            ConsoleHelper.console.display(" ");
        }
    }
}

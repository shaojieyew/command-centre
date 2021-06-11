package app.cli;

import app.cli.type.Action;
import app.spec.Kind;
import app.task.spark.HealthCheckSparkApps;
import app.task.spark.ListSparkApp;
import app.task.spark.RunSparkApps;
import app.task.spark.StopSparkApps;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SparkHealthCli extends SparkCli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(SparkHealthCli.class);

    @Override
    public Integer task() throws Exception {
        if(getCliAction().equalsIgnoreCase(Action.check.toString()) ||getCliAction().equalsIgnoreCase(Action.run.toString()) || getCliAction().equalsIgnoreCase(Action.start.toString())){
            new HealthCheckSparkApps(this).startTask();
        }
        return 0;
    }
}



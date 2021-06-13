package app.cli;

import app.cli.type.Action;
import app.task.spark.HealthCheckSparkApps;
import org.slf4j.LoggerFactory;

public class SparkHealthCli extends SparkCli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(SparkHealthCli.class);

    @Override
    public Integer task() throws Exception {
        if(getCliAction().equalsIgnoreCase(Action.check.toString())
                ||getCliAction().equalsIgnoreCase(Action.run.toString())
                || getCliAction().equalsIgnoreCase(Action.start.toString())){
            new HealthCheckSparkApps(this).startTask();
        }
        return 0;
    }
}



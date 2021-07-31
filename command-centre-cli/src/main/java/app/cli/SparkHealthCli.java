package app.cli;

import app.cli.type.Action;
import app.task.spark.HealthCheckSparkApps;
import app.util.ConsoleHelper;
import org.slf4j.LoggerFactory;

public class SparkHealthCli extends SparkCli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(SparkHealthCli.class);

    @Override
    public void printHelp() {

        ConsoleHelper.console.display("Command Actions:");
        ConsoleHelper.console.display("check\t\t\t\t\tCheck and launch all the stopped application that has enableHealthCheck=true");

    }
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



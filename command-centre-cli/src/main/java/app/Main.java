package app;

import app.cli.*;
import app.cli.type.Component;
import app.util.ConsoleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
public class Main  {
    private static final Logger logger  = LoggerFactory
            .getLogger(Main.class);

    public static void printHelp(){
        ConsoleHelper.console.display("Command actions:");
        ConsoleHelper.console.display("spark\t\t\tmanage spark applications on yarn");
        ConsoleHelper.console.display("nifi\t\t\tmanage nifi processors");
        ConsoleHelper.console.display("sparkhealth\t\tstart all stopped spark application with that has health check enabled");
        ConsoleHelper.console.display("checkpoint\t\tmanage spark streaming checkpoints on hdfs");
    }

    public static void main(String[] args) {
        if(args.length<1){
            ConsoleHelper.console.display(new Exception("Invalid argument"));
            System.exit(0);
        }
        String option = args[0].toLowerCase();

        if(Arrays.stream(Component.values()).anyMatch(c->c.toString().equalsIgnoreCase(option))){
            logger.info("starting component="+option);
            String [] cliArgs = new String[args.length-1];
            System.arraycopy(args,1,cliArgs,0,args.length-1);

            if(option.equalsIgnoreCase(Component.spark.toString())){
                new SparkCli().execute(cliArgs);
            }
            if(option.equalsIgnoreCase(Component.nifi.toString())){
                new NifiCli().execute(cliArgs);
            }
            if(option.equalsIgnoreCase(Component.checkpoint.toString())){
                new CheckpointCli().execute(cliArgs);
            }
            if(option.equalsIgnoreCase(Component.sparkhealth.toString())){
                new SparkHealthCli().execute(cliArgs);
            }
        }else{
            printHelp();
        }

    }

}
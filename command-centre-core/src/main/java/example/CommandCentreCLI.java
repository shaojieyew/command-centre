package example;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
@Command(name = "command-centre", mixinStandardHelpOptions = true, version = "command-centre 1.0",
        description = "command-centre")
public class CommandCentreCLI implements Callable<Integer> {

    @Parameters(index = "0", description = "action for command centre")
    private String action;

    @Option(names = {"-c", "--config"}, description = "path to the yaml config file")
    private String configFile = "command-centre-config.yml";

    @Override
    public Integer call() {
        System.out.println("load config: "+configFile);
        System.out.println("perform: "+action);
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new CommandCentreCLI()).execute(args);
        System.exit(exitCode);
    }
}
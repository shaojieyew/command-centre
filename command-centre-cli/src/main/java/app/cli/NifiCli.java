package app.cli;

import app.cli.type.Action;
import app.spec.Kind;
import app.task.nifi.ListNifiProcessors;
import app.task.nifi.RunNifiProcessors;
import app.task.nifi.StopNifiProcessors;
import app.task.nifi.UpdateNifiProcessorsStatus;
import app.util.ConsoleHelper;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

public class NifiCli extends Cli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(NifiCli.class);

    @Override
    public void printHelp() {

        ConsoleHelper.console.display("Command Actions:");
        ConsoleHelper.console.display("ls\t\t\t\t\tList all the NiFi component that matches the query declared inline or in NifiQuery spec file");
        ConsoleHelper.console.display("run\t\t\t\t\tRun all the NiFi component that matches the query declared inline or in NifiQuery spec file");
        ConsoleHelper.console.display("stop\t\t\t\t\tStop all the NiFi component that matches the query declared inline or in NifiQuery spec file");

        ConsoleHelper.console.display("");
        super.printHelp();
        ConsoleHelper.console.display("-i\t--id\t\t\tSpecify nifi component id");
        ConsoleHelper.console.display("-q\t--query\t\t\tSpecify inline query, support regex, eg. \"^/Test.*123/*/ListHDFS$\"");
        ConsoleHelper.console.display("-n\t--name\t\t\tSpecify query name declared in NifiQuery spec file");
    }

    @CommandLine.Option(names = {"--process-type"}, description = "processor-type")
    private String cliNifiProcessType = null;
    @CommandLine.Option(names = {"--root-processor"}, description = "root-processor")
    private boolean cliRootProcessor = false;

    public boolean isCliRootProcessor() {
        return cliRootProcessor;
    }

    public void setCliRootProcessor(boolean cliRootProcessor) {
        this.cliRootProcessor = cliRootProcessor;
    }

    public void setCliNifiProcessType(String cliNifiProcessType) {
        this.cliNifiProcessType = cliNifiProcessType;
    }

    public String getCliNifiProcessType() {
        return cliNifiProcessType;
    }
    public List<Kind> getSpecFile() {
        return super.getSpecFile().stream()
                .filter(k->k.getKind().toUpperCase().equalsIgnoreCase(Cli.KIND_NIFI_QUERY.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Integer task() throws Exception {
        if(getCliAction().equalsIgnoreCase(Action.ls.toString())){
            new ListNifiProcessors(this).startTask();
        } else {
            if(getCliAction().equalsIgnoreCase(Action.run.toString())){
                new RunNifiProcessors(this).startTask();
            } else {
                if(getCliAction().equalsIgnoreCase(Action.stop.toString())){
                    new StopNifiProcessors(this).startTask();
                }
            }
        }
        return 0;
    }

}



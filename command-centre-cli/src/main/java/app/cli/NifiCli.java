package app.cli;

import app.cli.type.Action;
import app.spec.Kind;
import app.task.nifi.ListNifiProcessors;
import app.task.nifi.RunNifiProcessors;
import app.task.nifi.StopNifiProcessors;
import app.task.nifi.UpdateNifiProcessorsStatus;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

public class NifiCli extends Cli {

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(NifiCli.class);

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
        } else if(getCliAction().equalsIgnoreCase(Action.run.toString())){
            new RunNifiProcessors(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.stop.toString())){
            new StopNifiProcessors(this).startTask();
        }
        return 0;
    }

}



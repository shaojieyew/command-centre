package app.cli;

import app.C2CliProperties;
import app.spec.Kind;
import app.spec.MetadataKind;
import app.spec.SpecException;
import app.spec.nifi.NifiQueryKind;
import app.spec.spark.SparkDeploymentKind;
import app.util.ConsoleHelper;
import app.util.YamlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class Cli  implements Callable<Integer> {

    private static Logger LOG = LoggerFactory
            .getLogger(Cli.class);

    @CommandLine.Parameters(defaultValue="", index = "0")
    private String cliAction;

    public String getCliAction() {
        return cliAction;
    }

    @CommandLine.Option(names = {"-c", "--config"}, description = "config location")
    private String cliConfig = System.getenv("C2_HOME")==null?null:System.getenv("C2_HOME")+"\\config\\setting.yml";

    @CommandLine.Option(names = {"-f", "--file"}, description = "file location")
    private String cliFilePath = null;

    @CommandLine.Option(names = {"-n", "--name"}, description = "name")
    private String cliName = null;

    @CommandLine.Option(names = {"-i", "--id"}, description = "id")
    private String cliId = null;

    @CommandLine.Option(names = {"-q", "--query"}, description = "query")
    private String cliQuery = null;

    public String getCliId() {
        return cliId;
    }

    public String getCliQuery() {
        return cliQuery;
    }

    public String getCliName() {
        return cliName;
    }

    private List<Kind> specFile = new ArrayList<>();

    public List<Kind> getSpecFile() {
        return specFile;
    }
    public C2CliProperties getC2CliProperties() {
        return c2CliProperties;
    }
    private C2CliProperties c2CliProperties = null;

    private String []args;
    public int execute(String[] args){
        this.args = args;
        return new CommandLine(this).execute(args);
    }
    @Override
    public Integer call() throws Exception {
        if(cliConfig==null){
            ConsoleHelper.console.display(new Exception("Env variable C2_HOME not set "));
            return 0;
        }
        File config = new File(cliConfig);
        if(!config.exists()){
            ConsoleHelper.console.display(new Exception(cliConfig+" file not found"));
            return 0;
        }

        if(cliFilePath != null) {
            File file = new File(cliFilePath);
            if (!file.exists()) {
                ConsoleHelper.console.display(new Exception(cliFilePath+" file not found"));
                return 0;
            }

            if(file.isDirectory()){
                LOG.info("loading files from directory="+file.getAbsolutePath());
                for(File subFile: file.listFiles()){
                    try {
                        Kind kind = parseKind(subFile);
                        if(kind != null){
                            specFile.add(kind);
                        }
                    } catch (SpecException e) {
                        ConsoleHelper.console.display(e);
                        return 0;
                    }catch (Exception e) {
                        ConsoleHelper.console.display(e);
                        return 0;
                    }
                }
            }else{
                LOG.info("loading file="+file.getAbsolutePath());
                Kind kind = parseKind(file);
                if(kind != null){
                    specFile.add(kind);
                }
            }
        }

        LOG.info("total file loaded="+specFile.size());
        LOG.info("loading config from="+config.getAbsolutePath());
        c2CliProperties = (C2CliProperties) new YamlLoader(C2CliProperties.class).load(config.getAbsolutePath());

        LOG.info("init project env properties");
        LOG.info(c2CliProperties.toString());

        try{
            return task();
        }catch (Exception e){
            ConsoleHelper.console.display(e);
            return 0;
        }
    }
    abstract public Integer task() throws Exception;

    final public static String KIND_APP_DEPLOYMENT="SparkDeployment";
    final public static String KIND_NIFI_QUERY="NifiQuery";


    private Kind parseKind(File file) throws IOException, SpecException {
        LOG.info("loading file="+file.getAbsolutePath());
        Kind k = null;
        MetadataKind metadata =  new YamlLoader<>(MetadataKind.class).load(file.getAbsolutePath());
        if(metadata.getKind().toUpperCase().equalsIgnoreCase(KIND_APP_DEPLOYMENT.toUpperCase())){
            LOG.info("file is SparkDeploymentKind="+file.getAbsolutePath());
            k = new YamlLoader<>(SparkDeploymentKind.class).load(file.getAbsolutePath());
            k.setFileOrigin(file);
            k.validate();
            return k;
        }
        if(metadata.getKind().toUpperCase().equalsIgnoreCase(KIND_NIFI_QUERY.toUpperCase())){
            LOG.info("file is NifiQueryKind="+file.getAbsolutePath());
            k = new YamlLoader<>(NifiQueryKind.class).load(file.getAbsolutePath());
            k.setFileOrigin(file);
            k.validate();
            return k;
        }
        return null;
    }
}

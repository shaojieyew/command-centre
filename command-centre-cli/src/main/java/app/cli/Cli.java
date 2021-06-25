package app.cli;

import app.C2CliProperties;
import app.spec.Kind;
import app.spec.MetadataKind;
import app.spec.SpecException;
import app.spec.nifi.NifiQueryKind;
import app.spec.spark.SparkDeploymentKind;
import app.task.Housekeeper;
import app.util.ConsoleHelper;
import app.util.FileHelper;
import app.util.YamlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public abstract class Cli  implements Callable<Integer> {

    private static Logger LOG = LoggerFactory
            .getLogger(Cli.class);

    @CommandLine.Parameters(defaultValue="", index = "0")
    private String cliAction;

    public String getCliAction() {
        return cliAction;
    }

    @CommandLine.Option(names = {"-c", "--config"}, description = "config location")
    private String cliConfig = System.getenv("C2_HOME")==null?null:System.getenv("C2_HOME")+"//config//setting.yml";

    @CommandLine.Option(names = {"-f", "--file"}, description = "file location")
    private String cliFilePath = null;
    @CommandLine.Option(names = {"-rf", "--recurse-file"}, description = "file location")
    private String cliRecursiveFilePath = null;

    public String getCliFilePath() {
        return cliFilePath;
    }
    public String getCliRecursiveFilePath() {
        return cliRecursiveFilePath;
    }

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

    private C2CliProperties c2CliProperties = null;

    public C2CliProperties getC2CliProperties() {
        return c2CliProperties;
    }


    public int execute(String[] args){
        return new CommandLine(this).execute(args);
    }


    public List<Kind> loadFile(String dir, boolean recursive) throws IOException, SpecException {
        List<Kind> specsKind = new ArrayList<>();
        if(dir != null) {
            File file = new File(dir);
            Set<File> files = new HashSet<>();

            if(file.isDirectory()){
                if(recursive){
                    files.addAll(FileHelper.listFileTree(file));
                }else{
                    files.addAll(Arrays.stream(file.listFiles()).collect(Collectors.toSet()));
                }
            }else{
                files.add(file);
            }
            if (!file.getAbsoluteFile().exists()) {
                ConsoleHelper.console.display(new Exception(dir+" file not found"));
            }

            if(file.isDirectory()){
                LOG.info("loading files from directory="+file.getAbsolutePath());
                for(File subFile: files){
                    try {
                        Kind kind = parseKind(subFile);
                        if(kind != null){
                            specsKind.add(kind);
                        }
                    } catch (SpecException e) {
                        ConsoleHelper.console.display(e);
                    } catch (Exception e) {
                        StringWriter errors = new StringWriter();
                        e.printStackTrace(new PrintWriter(errors));
                        LOG.warn(errors.toString());
                    }
                }
            }else{
                LOG.info("loading file="+file.getAbsolutePath());
                Kind kind = parseKind(file);
                if(kind != null){
                    specsKind.add(kind);
                }
            }
        }
        return specsKind;
    }

    @Override
    public Integer call() throws Exception {
        if(cliConfig==null){
            ConsoleHelper.console.display(new Exception("Env variable C2_HOME not set "));
            return 0;
        }
        File config = new File(cliConfig);
        if(!config.getAbsoluteFile().exists()){
            ConsoleHelper.console.display(new Exception(config.getAbsolutePath()+" file not found"));
            return 0;
        }

        String dir = cliFilePath==null?cliRecursiveFilePath:cliFilePath;
        boolean recursive = cliRecursiveFilePath!=null;
        specFile.addAll(loadFile(dir, recursive));

        LOG.info("total file loaded="+specFile.size());
        LOG.info("loading config from="+config.getAbsolutePath());
        c2CliProperties = (C2CliProperties) new YamlLoader(C2CliProperties.class).load(config.getAbsolutePath());

        LOG.info("init project env properties");
        LOG.info(c2CliProperties.toString());

        Housekeeper.houseKeep(c2CliProperties.getTmpDirectory());

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

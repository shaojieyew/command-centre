package app.cli;

import app.C2CliProperties;
import app.c2.model.Project;
import app.c2.service.ProjectService;
import app.cli.type.Component;
import app.spec.Kind;
import app.spec.MetadataKind;
import app.spec.nifi.NifiQueryKind;
import app.spec.resource.GroupResourceKind;
import app.spec.spark.AppDeploymentKind;
import app.task.CreateSpec;
import app.util.YamlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class Cli  implements Callable<Integer> {

    @CommandLine.Parameters(defaultValue="", index = "0")
    private String cliComponent;

    @CommandLine.Option(names = {"-c", "--config"}, description = "config location")
    private String cliConfig = System.getProperty("user.home")+"/.c2/setting.yml";

    @CommandLine.Option(names = {"-f", "--file"}, description = "file location")
    private String cliFilePath = null;

    @CommandLine.Option(names = {"-n", "--name"}, description = "name")
    private String cliName = null;

    @CommandLine.Option(names = {"-i", "--id"}, description = "id")
    private String cliId = null;

    @CommandLine.Option(names = {"-q", "--query"}, description = "query")
    private String cliQuery = null;

    @CommandLine.Option(names = {"--process-type"}, description = "processor-type")
    private String cliNifiProcessType = null;

    public String getCliComponent() {
        return cliComponent;
    }

    public String getCliId() {
        return cliId;
    }

    public void setCliId(String cliId) {
        this.cliId = cliId;
    }

    public void setCliComponent(String cliComponent) {
        this.cliComponent = cliComponent;
    }

    public String getCliConfig() {
        return cliConfig;
    }

    public void setCliConfig(String cliConfig) {
        this.cliConfig = cliConfig;
    }

    public void setCliFilePath(String cliFilePath) {
        this.cliFilePath = cliFilePath;
    }

    public void setCliName(String cliName) {
        this.cliName = cliName;
    }

    public void setCliQuery(String cliQuery) {
        this.cliQuery = cliQuery;
    }

    public void setCliNifiProcessType(String cliNifiProcessType) {
        this.cliNifiProcessType = cliNifiProcessType;
    }

    public String getCliQuery() {
        return cliQuery;
    }
    public String getCliNifiProcessType() {
        return cliNifiProcessType;
    }

    public String getCliFilePath() {
        return cliFilePath;
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

    private Project project;
    public Project getProject() {
        return project;
    }
    private String []args;
    public int execute(String[] args){
        this.args = args;
        return new CommandLine(this).execute(args);
    }

    @Autowired
    private ProjectService projectService;

    public int initProject() throws Exception {
        File config = new File(cliConfig);
        if(! config.exists()){
            throw new Exception("file not found: "+ cliConfig);
        }
        c2CliProperties = (C2CliProperties) new YamlLoader(C2CliProperties.class).load(config.getAbsolutePath());
        Optional<Project> optionalProject =  projectService.findAll().stream().filter(p->p.getName().equals(c2CliProperties.getProjectName())).findFirst();

        project = new Project();
        if(optionalProject.isPresent()){
            project = optionalProject.get();
        }else{
            project.setName(c2CliProperties.getProjectName());
        }
        project.setEnv(c2CliProperties);
        project = projectService.save(project);
        return 0;
    }

    @Override
    public Integer call() throws Exception {
        initProject();
        initFiles();
        return task();
    }
    abstract public Integer task() throws Exception;

    final private static String KIND_APP_DEPLOYMENT="AppDeployment";
    final private static String KIND_GROUP_RESOURCE="GroupResource";
    final private static String KIND_NIFI_QUERY="NifiQuery";

    private Kind parseKind(File file) throws IOException {
        MetadataKind metadata =  new YamlLoader<>(MetadataKind.class).load(file.getAbsolutePath());
        if(metadata.getKind().toUpperCase().equalsIgnoreCase(KIND_APP_DEPLOYMENT.toUpperCase())){
            return new YamlLoader<>(AppDeploymentKind.class).load(file.getAbsolutePath());
        }
        if(metadata.getKind().toUpperCase().equalsIgnoreCase(KIND_GROUP_RESOURCE.toUpperCase())){
            return new YamlLoader<>(GroupResourceKind.class).load(file.getAbsolutePath());
        }
        if(metadata.getKind().toUpperCase().equalsIgnoreCase(KIND_NIFI_QUERY.toUpperCase())){
            return new YamlLoader<>(NifiQueryKind.class).load(file.getAbsolutePath());
        }
        return null;
    }

    private void initFiles() throws IOException {
        if(cliFilePath !=null){
            File file = new File(cliFilePath);
            if(!file.exists()){
                throw new FileNotFoundException("file not found: "+ cliFilePath);
            }
            if(file.isDirectory()){
                for(File subFile: file.listFiles()){
                    try {
                        Kind kind = parseKind(subFile);
                        if(kind != null){
                            specFile.add(kind);
                        }
                    } catch (Exception e) {
                    }
                }
            }else{
                Kind kind = parseKind(file);
                if(kind != null){
                    specFile.add(kind);
                }
            }
        }
    }
}

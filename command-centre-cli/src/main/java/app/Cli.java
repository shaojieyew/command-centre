package app;

import app.task.*;
import app.spec.Kind;
import app.spec.MetadataKind;
import app.c2.model.Project;
import app.c2.service.ProjectService;
import app.spec.nifi.NifiQueryKind;
import app.spec.resource.GroupResourceKind;
import app.spec.spark.AppDeploymentKind;
import app.util.YamlLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "", mixinStandardHelpOptions = true, version = "1.0",
        description = "")
@Service
public class Cli implements Callable<Integer> {

    private final static String ACTION_CREATE = "create";
    private final static String ACTION_DELETE = "delete";
    private final static String ACTION_RUN = "run";
    private final static String ACTION_STOP = "stop";
    private final static String ACTION_GET = "get";
    private final static String ACTION_LS= "ls";
    private final static String TYPE_NIFI= "n";
    private final static String TYPE_FILE= "f";
    private final static String TYPE_APP= "a";
    
    public String[] args;

    public static List<String> ACTIONS =  new ArrayList<String>(
            Arrays.asList(ACTION_CREATE,ACTION_DELETE,ACTION_RUN,ACTION_STOP,ACTION_LS,ACTION_GET));

    @Parameters(index = "0", description = "action (ls, get, create, run, stop, delete)")
    private String _action = "";

    @Option(names = {"-c", "--conf"}, description = "config location")
    private String _config = System.getProperty("user.home")+"/.c2/setting.yml";

    @Option(names = {"-f", "--file"}, description = "file location")
    private String _file = null;

    @Option(names = {"-n", "--name"}, description = "name")
    private String _name = null;

    @Option(names = {"-t", "--type"}, description = "file f / app a / nifi n")
    private String _type = null;

    @Option(names = "-a", description = "")
    private boolean _all = false;

    @Option(names = {"-i", "--id"}, description = "id")
    private String _id = null;

    @Option(names = {"-q", "--query"}, description = "query")
    private String _query = null;

    @Option(names = {"--process-type"}, description = "processor-type")
    private String _nifi_process_type = null;

    @Option(names = {"--leading-processor"}, description = "only start/stop leading processor")
    private boolean _onlyLeaderProcessor = false;

    public int execute(String[] args){
        this.args = args;
        return new CommandLine(this).execute(args);
    }

    public boolean is_onlyLeaderProcessor() {
        return _onlyLeaderProcessor;
    }

    public String get_query() {
        return _query;
    }
    public String get_nifi_process_type() {
        return _nifi_process_type;
    }

    public String get_file() {
        return _file;
    }

    public String get_type() {
        return _type;
    }

    public String get_id() {
        return _id;
    }

    public String get_name() {
        return _name;
    }

    public String get_action() {
        return _action;
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

    @Autowired
    private ProjectService projectService;
    @Autowired
    CreateSpec createSpec;
    @Autowired
    RunApp runApp;
    @Autowired
    ListTask listTask;
    @Autowired
    ListNifi listNifi;
    @Autowired
    ListApp listApp;
    @Autowired
    ListFile listFile;
    @Autowired
    DeleteFile deleteFile;
    @Autowired
    DeleteApp deleteApp;
    @Autowired
    GetFile getFile;
    @Autowired
    GetApp getApp;
    @Autowired
    StopApp stopApp;
    @Autowired
    StopNifi stopNifi;
    @Autowired
    RunNifi runNifi;

    public int initProject() throws Exception {
        File config = new File(_config);
        if(! config.exists()){
            throw new Exception("file not found: "+ _config);
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

        if(!ACTIONS.contains(_action.toUpperCase())){
            System.out.println("Invalid action");
            return 0;
        }

        switch ((get_action()).toUpperCase()){
            case ACTION_CREATE:
                if(getSpecFile().size()==0){
                    throw new Exception("No service or spec file provided");
                }
                createSpec.startTask(this);
                break;

            case ACTION_RUN:
                if(get_type()!=null){
                    switch (get_type()){
                        case TYPE_APP:
                            if(getSpecFile().size()>0){
                                createSpec.startTask(this);
                            }
                            runApp.startTask(this);
                            break;
                        case TYPE_NIFI:
                            runNifi.startTask(this);
                            break;
                    }
                }else{
                    if(getSpecFile().size()>0){
                        runNifi.startTask(this);
                        createSpec.startTask(this);
                        runApp.startTask(this);
                    }
                }
                break;

            case ACTION_LS:
                if(get_type()==null){
                    listTask.startTask(this);
                }else{
                    switch (get_type()){
                        case TYPE_APP:
                            listApp.startTask(this);
                            break;
                        case TYPE_FILE:
                            listFile.startTask(this);
                            break;
                        case TYPE_NIFI:
                            listNifi.startTask(this);
                            break;
                        default:
                    }
                }
                break;

            case ACTION_GET:
                if(get_type()==null){
                    throw new Exception("require --type");
                }
                switch (get_type()){
                    case TYPE_APP:
                        getApp.startTask(this);
                        break;
                    case TYPE_FILE:
                        getFile.startTask(this);
                        break;
                }
                break;

            case ACTION_STOP:
                if(get_type()!=null){
                    switch (get_type()){
                        case TYPE_APP:
                            if(_all){
                                stopApp.startTask(this);
                            }else{
                                if(get_name()!=null){
                                    stopApp.startTask(this, get_name());
                                }
                            }
                            break;
                        case TYPE_NIFI:
                            stopNifi.startTask(this);
                            break;
                    }
                }else{
                    stopApp.startTask(this);
                    stopNifi.startTask(this);
                }

                break;

            case ACTION_DELETE:
                if(get_type()==null){
                    throw new Exception("require --type");
                }
                switch (get_type()){
                    case TYPE_APP:
                        deleteApp.startTask(this);
                        break;
                    case TYPE_FILE:
                        deleteFile.startTask(this);
                        break;
                }
                break;
        }
        return 0;
    }

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
        if(_file!=null){
            File file = new File(_file);
            if(!file.exists()){
                throw new FileNotFoundException("file not found: "+ _file);
            }
            if(file.isDirectory()){
                System.out.println(_file+" is a directory");
                for(File subFile: file.listFiles()){
                    try {
                        Kind kind = parseKind(subFile);
                        if(kind != null){
                            specFile.add(kind);
                            System.out.println(subFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                    }
                }
            }else{
                Kind kind = parseKind(file);
                if(kind != null){
                    specFile.add(kind);
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }

}


//TODO clean up hardcoded Strnig
//TODO organise cli parameter,
//TODO standardise config
//TODO delete useless codes
//TODO kerberos sparksubmit and nifi


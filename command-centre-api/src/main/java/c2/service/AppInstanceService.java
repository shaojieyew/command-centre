package c2.service;

import c2.C2PlatformProperties;
import c2.dao.AppInstanceDao;
import c2.model.App;
import c2.model.AppInstance;
import c2.model.Project;
import c2.properties.C2Properties;
import c2.services.mvnRegistry.AbstractRegistrySvc;
import c2.services.mvnRegistry.RegistrySvcFactory;
import c2.services.mvnRegistry.model.Package;
import c2.services.yarn.YarnSvc;
import c2.services.yarn.model.YarnApp;
import org.apache.commons.io.FileUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AppInstanceService {

    @Autowired
    AppInstanceDao appInstanceDao;
    @Autowired
    AppService appService;
    @Autowired
    ProjectService projectService;
    @Autowired
    C2PlatformProperties sparkProperties;
    @Autowired
    FileStorageService fileStorageService;


    private String getYarnAppState(String applicationId){
        Optional<YarnApp> yarnApp = new YarnSvc(sparkProperties.getYarn()).setApplicationId(applicationId).get().stream().findFirst();
        return yarnApp.map(a->a.getState().toUpperCase()).orElse("UNKNOWN");
    }

    public Optional<AppInstance> findById(String appid){
        Optional<AppInstance> appInstanceOptional = appInstanceDao.findById( appid).map(i->{
            i.setLastState(getYarnAppState(i.getAppId()));
            return i;
        });

        return appInstanceOptional;
    }

    public List<AppInstance> findByProjectId(long projectId){
        return appInstanceDao.findByProjectId( projectId);
    }

    public List<AppInstance> findByProjectAppId(long projectId, String appName){

        List<AppInstance> instances = appInstanceDao.findByProjectAppId(appService.createAppId(projectId, appName)).stream().map(i->{
            i.setLastState(getYarnAppState(i.getAppId()));
            return i;
        }).collect(Collectors.toList());

        return instances;
    }


    public Optional<AppInstance> findByProjectAppIdLastLaunched(long projectId, String appName){

        Optional<AppInstance> instances = findByProjectAppId(projectId,appName).stream().max(new Comparator<AppInstance>() {
            @Override
            public int compare(AppInstance o1, AppInstance o2) {
                return o1.getUpdatedTimestamp().compareTo(o2.getUpdatedTimestamp());
            }
        }).map(i->{
            i.setLastState(getYarnAppState(i.getAppId()));
            return i;
        });

        return instances;
    }


    public AppInstance save(App app, String appId, List<Long> fileIds, String lastState) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String fileIdsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileIds);
        AppInstance instance = new AppInstance();
        instance.setAppId(appId);
        instance.setProjectAppId(appService.createAppId(app.getProjectId(), app.getName()));
        instance.setFileIds(fileIds);
        instance.setJarGroupId(app.getJarGroupId());
        instance.setJarArtifactId(app.getJarArtifactId());
        instance.setJarVersion(app.getJarVersion());
        instance.setJarMainClass(app.getJarMainClass());
        instance.setJarArgs(app.getJarArgs());
        instance.setSparkArgs(app.getSparkArgs());
        instance.setLastState(lastState);
        instance.setProjectId(app.getProjectId());
        return appInstanceDao.save(instance);
    }

    public AppInstance save(AppInstance app, String appId, String lastState) throws IOException {

        AppInstance instance = new AppInstance();
        instance.setAppId(appId);
        instance.setProjectAppId(app.getProjectAppId());
        instance.setFileIds(app.getFileIds());
        instance.setJarGroupId(app.getJarGroupId());
        instance.setJarArtifactId(app.getJarArtifactId());
        instance.setJarVersion(app.getJarVersion());
        instance.setJarMainClass(app.getJarMainClass());
        instance.setJarArgs(app.getJarArgs());
        instance.setSparkArgs(app.getSparkArgs());
        instance.setLastState(lastState);
        instance.setProjectId(app.getProjectId());
        instance.setAppId(appId);
        instance.setLastState(lastState);
        return appInstanceDao.save(instance);
    }


    public AppInstance submitApp(String appId) throws Exception {
        // launch app
        long launchTime = System.currentTimeMillis();
        Optional<AppInstance> appInstanceOpt = appInstanceDao.findById(appId);
        if(!appInstanceOpt.isPresent()){
            throw new Exception("Invalid AppId");
        }
        AppInstance appInstance = appInstanceOpt.get();

        Optional<App> appOpt = appService.findById(appInstance.getProjectAppId());
        if(!appOpt.isPresent()){
            throw new Exception("Invalid AppId");
        }
        String appName = appOpt.get().getName();
        long projectId = appOpt.get().getProjectId();
        Project project = projectService.findById(projectId).orElseGet(null);
        if (project == null) {
            throw new Exception("Project not found.");
        }
        C2Properties properties = (project.getEnv());

        if(new YarnSvc(sparkProperties.getYarn()).setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING").get().stream().filter(f->f.getName().equalsIgnoreCase(appName))
                .count()>0){
            throw new Exception("App name, "+appName+" has been submitted");
        }

        File jar = null;
        for (AbstractRegistrySvc reg : RegistrySvcFactory.create(properties)) {
            Optional<Package> optionalPackage =
                    reg.getPackage(appInstance.getJarGroupId(), appInstance.getJarArtifactId(), appInstance.getJarVersion());
            if (optionalPackage.isPresent()) {
                jar = reg.download(optionalPackage.get());
                break;
            }
        }

        if (jar == null) {
            throw new Exception("Application artifact not found.");
        }
        String sparkMaster = "yarn";
        String deployMode = "cluster";

    SparkLauncher launcher =
        new SparkLauncher()
            .setSparkHome(sparkProperties.getSpark())
            .setMaster(sparkMaster)
            .setDeployMode(deployMode)
            .setAppResource(jar.getAbsolutePath())
            .setMainClass(appInstance.getJarMainClass())
            .setAppName(appName);

        // add app args
        for (String arg : appInstance.getJarArgs()) {
            launcher = launcher.addAppArgs(arg);
        }

        // add spark args
        for (String key : appInstance.getSparkArgs().keySet()) {
            launcher = launcher.addSparkArg(key, appInstance.getSparkArgs().get(key));
        }

        // add files
        String tmpDir =
                sparkProperties.getTmp()
                        + "/command-center/"
                        + projectId
                        + "/"
                        + appName
                        + "/"
                        + launchTime;
        Files.createDirectories(Paths.get(tmpDir));

        List<Long> fileIds = appInstance.getFileIds();

        Iterator<c2.model.File> filesItr = fileStorageService.getFiles(fileIds).iterator();
        while (filesItr.hasNext()) {
            c2.model.File f = filesItr.next();
            Files.write(Paths.get(tmpDir).resolve(f.getName()), f.getFileBlob());
            launcher =
                    launcher.addFile(Paths.get(tmpDir).resolve(f.getName()).toAbsolutePath().toString());
        }

        SparkLauncher finalLauncher = launcher;
        SparkAppHandle handler = null;
        String applicationId = null;
        String state = null;
        try {
            handler = finalLauncher.startApplication();
            while (!handler.getState().isFinal()) {
                applicationId = handler.getAppId();
                state = handler.getState().name();
                if (applicationId != null) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (applicationId == null) {
            applicationId = "failed_application_" + launchTime;
        }

        AppInstance newAppInstance = null;
        try {
            newAppInstance = save(appInstance, applicationId, state);
            FileUtils.deleteDirectory(new File(tmpDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newAppInstance;
    }

    public boolean kill(long projectId, String appId) throws Exception {
        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            throw new Exception("Invalid ProjectId");
        }
        C2Properties prop = (projectOpt.get().getEnv());
        return new YarnSvc(sparkProperties.getYarn()).setApplicationId(appId).kill();
    }
}

package c2.service;

import c2.C2PlatformProperties;
import c2.common.SystemUtil;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Optional<YarnApp> yarnApp = new YarnSvc(sparkProperties.getYarnHost()).setApplicationId(applicationId).get().stream().findFirst();
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

    public AppInstance convertAppToInstance(App app) throws IOException{
        AppInstance instance = new AppInstance();
        instance.setProjectAppId(appService.createAppId(app.getProjectId(), app.getName()));
        instance.setFileIds(app.getFileIds());
        instance.setJarGroupId(app.getJarGroupId());
        instance.setJarArtifactId(app.getJarArtifactId());
        instance.setJarVersion(app.getJarVersion());
        instance.setJarMainClass(app.getJarMainClass());
        instance.setJarArgs(app.getJarArgs());
        instance.setSparkArgs(app.getSparkArgs());
        instance.setProjectId(app.getProjectId());
        instance.setName(app.getName());
        return instance;
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

    @Transactional
    public AppInstance save(AppInstance app, String appId, String lastState) throws IOException {
        app.setAppId(appId);
        app.setLastState(lastState);
        return appInstanceDao.save(app);
    }


    public boolean kill(long projectId, String appId) throws Exception {
        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            throw new Exception("Invalid ProjectId");
        }
        C2Properties prop = (projectOpt.get().getEnv());
        return new YarnSvc(sparkProperties.getYarnHost()).setApplicationId(appId).kill();
    }


}

package app.c2.service;

import app.c2.C2PlatformProperties;
import app.c2.dao.AppInstanceDao;
import app.c2.model.App;
import app.c2.model.AppInstance;
import app.c2.model.Project;
import app.c2.properties.C2Properties;
import app.c2.services.yarn.YarnSvc;
import app.c2.services.yarn.model.YarnApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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


    private String getYarnAppState(long projectId, String applicationId) throws JsonProcessingException {
        Optional<Project> optionalProject = projectService.findById(projectId);
        if(optionalProject.isPresent()){
            Optional<YarnApp> yarnApp = new YarnSvc(optionalProject.get().getEnv().getHadoopProperties().getYarnHost()).setApplicationId(applicationId).get().stream().findFirst();
            return yarnApp.map(a->a.getState().toUpperCase()).orElse("UNKNOWN");
        }else{
            return "UNKNOWN";
        }
    }


    public Optional<AppInstance> findById(String appid){
        Optional<AppInstance> appInstanceOptional = appInstanceDao.findById( appid).map(i->{
            try {
                i.setLastState(getYarnAppState(i.getProjectId(),i.getAppId()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return i;
        });

        return appInstanceOptional;
    }

    public List<AppInstance> findByProjectId(long projectId){
        return appInstanceDao.findByProjectId( projectId);
    }

    public List<AppInstance> findByProjectAppId(long projectId, String appName){

        List<AppInstance> instances = appInstanceDao.findByProjectIdAndName(projectId, appName).stream().map(i->{
            try {
                i.setLastState(getYarnAppState(projectId,i.getAppId()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
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
            try {
                i.setLastState(getYarnAppState(i.getProjectId(),i.getAppId()));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return i;
        });
        return instances;
    }

    public AppInstance convertAppToInstance(App app) throws Exception {
        AppInstance instance = new AppInstance();

        Optional<Project> projectOpt = projectService.findById(app.getProjectId());
        if(!projectOpt.isPresent()){
            throw new Exception("Invalid ProjectId");
        }
        Set<Long> fileIds = projectOpt.get().getFileIds(app.getNamespace());
        Set <String> fileNames = app.getFileIds().stream().map(fId->
            fileStorageService.getFile(fId).get().getName().toUpperCase()).collect(Collectors.toSet());
        fileIds = fileIds.stream().filter(fId->
            !fileNames.contains(fileStorageService.getFile(fId).get().getName().toUpperCase())
        ).collect(Collectors.toSet());
        fileIds.addAll(app.getFileIds());
        instance.setFileIds(fileIds);
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
    public App convertAppInstanceToApp(AppInstance appInstance) throws IOException{
        App app = new App();
        app.setFileIds(appInstance.getFileIds());
        app.setJarGroupId(appInstance.getJarGroupId());
        app.setJarArtifactId(appInstance.getJarArtifactId());
        app.setJarVersion(appInstance.getJarVersion());
        app.setJarMainClass(appInstance.getJarMainClass());
        app.setJarArgs(appInstance.getJarArgs());
        app.setSparkArgs(appInstance.getSparkArgs());
        app.setProjectId(appInstance.getProjectId());
        app.setName(appInstance.getName());
        return app;
    }


    public AppInstance save(App app, String appId, Set<Long> fileIds, String lastState) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String fileIdsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileIds);
        AppInstance instance = new AppInstance();
        instance.setAppId(appId);
        instance.setFileIds(fileIds);
        instance.setJarGroupId(app.getJarGroupId());
        instance.setJarArtifactId(app.getJarArtifactId());
        instance.setJarVersion(app.getJarVersion());
        instance.setJarMainClass(app.getJarMainClass());
        instance.setJarArgs(app.getJarArgs());
        instance.setSparkArgs(app.getSparkArgs());
        instance.setLastState(lastState);
        instance.setProjectId(app.getProjectId());
        instance.setName(app.getName());
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
        return new YarnSvc(projectOpt.get().getEnv().getHadoopProperties().getYarnHost()).setApplicationId(appId).kill();
    }


}

package app.c2.service;

import app.c2.C2PlatformProperties;
import app.c2.dao.AppDao;
import app.c2.model.App;
import app.c2.model.AppInstance;
import app.c2.model.File;
import app.c2.model.Project;
import app.c2.model.compositKey.AppId;
import app.c2.services.yarn.YarnSvc;
import app.c2.services.yarn.YarnSvcFactory;
import app.c2.services.yarn.model.YarnApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AppService {

    @Autowired
    AppDao appDao;
    @Autowired
    C2PlatformProperties sparkProperties;
    @Autowired ProjectService projectService;
    @Autowired AppService appService;
    @Autowired FileStorageService fileStorageService;
    @Autowired AppInstanceService appInstanceService;
    @Autowired SparkService sparkService;

    public String createAppId(long projectId, String appName){
        return projectId+"/"+appName;
    }

    public List<App> findByProjectId(long projectId){
        return appDao.findByProjectId(projectId);
    }
    public Optional<App> findById(long projectId, String appName){
        return appDao.findById(new AppId(projectId,appName));
    }

    public App save(App app){
        return appDao.save(app);
    }

    public App save(String name, long projectId, String jarGroupId, String jarArtifactId, String jarVersion, String jarMainClass, List<String> jarArgs, Map<String,String> sparkArgs) throws IOException {
        App app = new App();
        app.setName(name);
        app.setProjectId(projectId);
        app.setJarGroupId(jarGroupId);
        app.setJarArtifactId(jarArtifactId);
        app.setJarVersion(jarVersion);
        app.setJarMainClass(jarMainClass);
        app.setJarArgs(jarArgs);
        app.setSparkArgs(sparkArgs);
        app.setNamespace(null);
        return appDao.save(app);
    }

    public void delete(String appName, long projectId) {
        appDao.deleteById(new AppId(projectId,appName));
    }

    public AppInstance submitApp(long projectId, String appName, boolean saveSnapshot) throws Exception {
        return  submitApp( projectId,  appName,  saveSnapshot,true);
    }
    public AppInstance submitApp(long projectId, String appName, boolean saveSnapshot, boolean async) throws Exception {
        Optional<App> appOptional = appService.findById(projectId,appName);
        App app = null;
        if(!appOptional.isPresent()){
            throw new Exception("Invalid AppName");
        }
        app = appOptional.get();
        AppInstance appInstance = appInstanceService.convertAppToInstance(app);
        return sparkService.submitApp(appInstance, true, saveSnapshot, async);
    }

    public AppInstance submitApp(App app, boolean saveSnapshot) throws Exception {
        return submitApp( app,  saveSnapshot);
    }

    public AppInstance submitApp(App app, boolean saveSnapshot, boolean async) throws Exception {
        AppInstance appInstance = appInstanceService.convertAppToInstance(app);
        return sparkService.submitApp(appInstance, true, saveSnapshot, async);
    }

    public Optional<App> findApp(long projectId, String appName) {
        return appDao.findById(new AppId(projectId,appName));
    }

    public ArrayList<File> getFiles(long projectId, String appName) {

        Optional<App> appOptional = appDao.findById(new AppId(projectId,appName));
        if(appOptional.isPresent()){
            Iterable<File> filesIter=   fileStorageService.getFiles(appOptional.get().getFileIds());
            ArrayList<File> actualList = Lists.newArrayList(filesIter);
            actualList.forEach(f->f.setFileBlob(null));
            return actualList;
        }
        return new ArrayList<>();

    }

    public void deleteFile(long projectId, String appName, String filename) throws JsonProcessingException {

        Optional<App> appOptional = appDao.findById(new AppId(projectId,appName));
        if(appOptional.isPresent()){
            List<File> files = getFiles(projectId,appName);
            App app = appOptional.get();
            app.setFileIds(files.stream().filter(f->!f.getName().equals(filename)).map(f->f.getId()).collect(Collectors.toSet()));
            save(app);
        }
    }

    public List<App> findAllAppStatus(long projectId) throws Exception {
        Instant aMonthBack = Instant.now().minusSeconds(60*60*24*45);
        long millis = aMonthBack.toEpochMilli();
        List<App> apps = appDao.findByProjectId(projectId);

        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            throw new Exception("Invalid ProjectId");
        }
        return apps.stream().map( app->{
            String sparkAppNameToSubmit = SparkService.getSparkAppName(projectOpt.get().getName(),projectId, app.getName());
            YarnSvc yarnSvc = null;
            try {

                yarnSvc = YarnSvcFactory.create(projectOpt.get().getEnv());

                Optional<YarnApp> yarnApp = yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                        .setStartedTimeBegin(millis)
                        .get().stream()
                        .filter(f->f.getName().equalsIgnoreCase(sparkAppNameToSubmit))
                        .max(new Comparator<YarnApp>() {
                            @Override
                            public int compare(YarnApp o1, YarnApp o2) {
                                return o1.getStartedTime().compareTo(o2.getStartedTime());
                            }
                        });
                if(yarnApp.isPresent()){
                    app.setYarnStatus(yarnApp.get().getState());
                    app.setYarnAppId(yarnApp.get().getId());
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return app;
        }).collect(Collectors.toList());

    }

    public boolean kill(long projectId) throws Exception {
        return kill(projectId, null);
    }

    public boolean kill(long projectId, String appName) throws Exception {
        Instant aMonthBack = Instant.now().minusSeconds(60*60*24*45);
        long millis = aMonthBack.toEpochMilli();
        List<App> apps = appDao.findByProjectId(projectId);

        if(appName!=null && appName.length()>0){
            apps = apps.stream().filter(app -> app.getName().equalsIgnoreCase(appName)).collect(Collectors.toList());
        }

        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            throw new Exception("Invalid ProjectId");
        }
        apps.stream().forEach( app->{
            String sparkAppNameToKill = SparkService.getSparkAppName(projectOpt.get().getName(),projectId, app.getName());
            YarnSvc yarnSvc = null;
            try {
                yarnSvc =  YarnSvcFactory.create(projectOpt.get().getEnv());
                yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                        .setStartedTimeBegin(millis)
                        .get().stream()
                        .filter(f->f.getName().equalsIgnoreCase(sparkAppNameToKill))
                        .forEach(s->{
                            try {
                                YarnSvcFactory.create(projectOpt.get().getEnv()).setApplicationId(s.getId())
                                        .kill();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        });
        return true;
    }


}

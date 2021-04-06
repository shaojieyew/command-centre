package c2.service;

import c2.C2PlatformProperties;
import c2.common.SystemUtil;
import c2.dao.AppDao;
import c2.model.App;
import c2.model.AppInstance;
import c2.model.Project;
import c2.properties.C2Properties;
import c2.services.mvnRegistry.AbstractRegistrySvc;
import c2.services.mvnRegistry.RegistrySvcFactory;
import c2.services.mvnRegistry.model.Package;
import c2.services.yarn.YarnSvc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AppService {

    @Autowired AppDao appDao;
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

    public Optional<App> findById(long projectId, String appName){
        return appDao.findById(createAppId(projectId,appName));
    }
    public Optional<App> findById(String id){
        return appDao.findById(id);
    }

    public App save(App app){
        app.setId(createAppId(app.getProjectId(),app.getName()));
        return appDao.save(app);
    }

    public App save(String name, long projectId, String jarGroupId, String jarArtifactId, String jarVersion, String jarMainClass, List<String> jarArgs, Map<String,String> sparkArgs) throws IOException {
        App app = new App( name,  projectId,  jarGroupId,  jarArtifactId,  jarVersion,  jarMainClass,  jarArgs,  sparkArgs);
        return appDao.save(app);
    }

    public void delete(String name, long projectId) {
        appDao.deleteById(createAppId(projectId,name));
    }

    public AppInstance submitApp(long projectId, String appName) throws Exception {
        Optional<App> appOptional = appService.findById(projectId,appName);
        App app = null;
        if(!appOptional.isPresent()){
            throw new Exception("Invalid AppName");
        }
        app = appOptional.get();
        AppInstance appInstance = appInstanceService.convertAppToInstance(app);
        return sparkService.submitApp(appInstance, true);
    }

    public Optional<App> findApp(long projectId, String appName) {
        return appDao.findById(createAppId(projectId,appName));
    }

    public ArrayList<c2.model.File> getFiles(long projectId, String appName) {

        Optional<App> appOptional = appDao.findById(createAppId(projectId,appName));
        if(appOptional.isPresent()){
            Iterable<c2.model.File> filesIter=   fileStorageService.getFiles(appOptional.get().getFileIds());
            ArrayList<c2.model.File> actualList = Lists.newArrayList(filesIter);
            actualList.forEach(f->f.setFileBlob(null));
            return actualList;
        }
        return new ArrayList<>();

    }

    public void deleteFile(long projectId, String appName, String filename) throws JsonProcessingException {

        Optional<App> appOptional = appDao.findById(createAppId(projectId,appName));
        if(appOptional.isPresent()){
            List<c2.model.File> files = getFiles(projectId,appName);
            App app = appOptional.get();
            app.setFileIds(files.stream().filter(f->!f.getName().equals(filename)).map(f->f.getId()).collect(Collectors.toList()));
            save(app);
        }
    }
}

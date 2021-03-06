package app.c2.service;

import app.c2.C2PlatformProperties;
import app.c2.model.AppInstance;
import app.c2.model.Project;
import app.c2.service.spark.SparkSvc;
import app.c2.service.spark.model.SparkArgKeyValuePair;
import app.c2.properties.C2Properties;
import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.MavenSvcFactory;
import app.c2.service.maven.model.Package;
import app.c2.service.util.FileManager;
import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SparkService {

    @Autowired
    AppInstanceService appInstanceService;
    @Autowired
    C2PlatformProperties c2PlatformProperties;
    @Autowired ProjectService projectService;
    @Autowired FileStorageService fileStorageService;

    public static String getSparkAppName(String projectName, long projectId, String AppName){
        return projectName.replaceAll(" ","")+projectId+"_"+AppName;
    }


    public void startSparkLauncher(SparkLauncher finalLauncher, AppInstance app, boolean saveSnapshot) throws Exception {
        SparkAppHandle handler = null;
        String applicationId = null;
        String state = null;
        try {
            handler = finalLauncher.startApplication();
            while(!handler.getState().isFinal()){
                applicationId = handler.getAppId();
                state = handler.getState().name();
                if(applicationId!=null){
                    break;
                }
            }
            handler.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(applicationId==null){
            throw new Exception(app.getName()+" failed to launch");
        }

        if(saveSnapshot){
            try {
                appInstanceService.save(app,applicationId,state);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File getJar(C2Properties prop , String group, String artifact, String version) throws ArtifactResolutionException {
        Package pkg = new Package();
        pkg.setArtifact(artifact);
        pkg.setGroup(group);
        pkg.setPackage_type(Package.PackageType.MAVEN);
        pkg.setVersion(version);
        for (AbstractRegistrySvc reg: MavenSvcFactory.create(prop, c2PlatformProperties.getTmp()+"/maven")){
            return reg.download(pkg);
        }
        return null;
    }

    private SparkLauncher addArgs(SparkLauncher launcher, List<String> args){
        for(String arg: args){
            launcher = launcher.addAppArgs(arg);
        }
        return launcher;
    }

    private SparkLauncher addSparkArgs(SparkLauncher launcher, Set<SparkArgKeyValuePair> args){
        for(SparkArgKeyValuePair kv: args){
            launcher = launcher.addSparkArg(kv.getName(), kv.getValue());
        }
        return launcher;
    }

    private  List<File> createTempFiles(AppInstance appInstance, Map<String, app.c2.model.File> tmpFiles, boolean inheritProjectFiles) throws IOException {
        String baseDir = c2PlatformProperties.getTmp()+"/project_"+appInstance.getProjectId()+"/spark-submit";
        if (new File(baseDir).getAbsoluteFile().exists()) {
            FileManager.clean(baseDir, 2, false);
        }
        if (!new File(baseDir).getAbsoluteFile().exists()) {
            Files.createDirectories(Paths.get(baseDir));
        }
        String tmpDir = baseDir+"/"+System.currentTimeMillis()+"/"+appInstance.getName();
        Files.createDirectories(Paths.get(tmpDir));

        List<File> ioFiles = new ArrayList<>();
        if(appInstance.getFileIds().size()==0){
            return ioFiles;
        }
        Iterator<app.c2.model.File> filesItrs =  fileStorageService.getFiles( appInstance.getFileIds()).iterator();
        while (filesItrs.hasNext()) {
            app.c2.model.File f = filesItrs.next();
            tmpFiles.put(f.getName(),f);
            Files.write(Paths.get(tmpDir).resolve(f.getName()), f.getFileBlob());
            ioFiles.add(new File(Paths.get(tmpDir).resolve(f.getName()).toAbsolutePath().toString()));
        }
        return ioFiles;
    }

    private SparkLauncher createTmpFiles(SparkLauncher launcher, String tmpDir, Map<String, app.c2.model.File> files, Map<String, Set<Long>> fileIds) throws IOException {
        if(fileIds.size()==0){
            return launcher;
        }
        Iterator<app.c2.model.File> filesItrs =  fileStorageService.getFiles(fileIds.get("default")).iterator();
        while (filesItrs.hasNext()) {
            app.c2.model.File f = filesItrs.next();
            files.put(f.getName(),f);
            Files.write(Paths.get(tmpDir).resolve(f.getName()), f.getFileBlob());
            launcher = launcher.addFile(Paths.get(tmpDir).resolve(f.getName()).toAbsolutePath().toString());
        }
        return launcher;
    }

    public AppInstance submitApp(String appId, boolean saveSnapshot) throws Exception {
        Optional<AppInstance> appInstanceOptional = appInstanceService.findById(appId);
        if(!appInstanceOptional.isPresent()){
            throw new Exception("Submitted invalid AppId");
        }
        return  submitApp(appInstanceOptional.get(), false, saveSnapshot, true);
    }

    public AppInstance submitApp(AppInstance appInstance, boolean inheritProjectFiles,boolean saveSnapshot,  boolean async) throws Exception {

        Project project = projectService.findById(appInstance.getProjectId()).orElseGet(null);
        if (project==null) {
            throw new Exception("Project not found.");
        }
        C2Properties prop = (project.getEnv());
        String hadoopConfDir = c2PlatformProperties.getTmp()+"/project_"+project.getId()+"/hadoopConf";
        File hadoopConfDirFile = new File(hadoopConfDir);
        hadoopConfDirFile.mkdirs();
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/core-site.xml"), prop.getHadoopYarnProperties().getCoreSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/hdfs-site.xml"), prop.getHadoopYarnProperties().getHdfsSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/yarn-site.xml"), prop.getHadoopYarnProperties().getYarnSite(),"UTF-8");

        SparkSvc sparkSvc = new SparkSvc(c2PlatformProperties.getSparkHome(),hadoopConfDirFile.getAbsolutePath());

        File jar = getJar( prop , appInstance.getJarGroupId(), appInstance.getJarArtifactId(), appInstance.getJarVersion());
        if (jar == null) {
            throw new Exception("Application artifact not found.");
        }

        String sparkAppNameToSubmit = getSparkAppName(project.getName(), project.getId(), appInstance.getName());

        YarnSvc yarnSvc = YarnSvcFactory.create(prop);
        if(yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                .get().stream()
                .filter(f->f.getName().equalsIgnoreCase(sparkAppNameToSubmit))
                .count()>0){
            throw new Exception(appInstance.getName()+" already running");
        }

        // add files

        Map<String, app.c2.model.File> tmpFiles = new HashMap<>();
        List<File> resources = createTempFiles( appInstance,  tmpFiles, inheritProjectFiles);
        Set<Long> fileIds = tmpFiles.entrySet().stream().map(f->f.getValue().getId()).collect(Collectors.toSet());
        appInstance.setFileIds(fileIds);


        if(async){
            startSparkLauncherAsync( sparkAppNameToSubmit,  sparkSvc,  appInstance,  jar,  resources, saveSnapshot);
        }else{
            startSparkLauncher( sparkAppNameToSubmit,  sparkSvc,  appInstance,  jar, resources,  saveSnapshot);
        }
        return appInstance;
    }

    public void startSparkLauncher(String sparkAppNameToSubmit, SparkSvc sparkSvc, AppInstance appInstance, File jar, List<File> resources, boolean saveSnapshot) throws Exception {
        String applicationId = null;
        String state = null;

        applicationId = sparkSvc.submitSpark(sparkAppNameToSubmit,
                appInstance.getJarMainClass(),
                jar,appInstance.getJarArgs(),
                appInstance.getSparkArgs(),null,resources);

        if(saveSnapshot){
            try {
                if(applicationId==null){
                    applicationId  = appInstance.getName()+"_"+System.currentTimeMillis();
                }
                appInstanceService.save(appInstance,applicationId,state);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Async
    public void startSparkLauncherAsync(String sparkAppNameToSubmit, SparkSvc sparkSvc, AppInstance appInstance, File jar, List<File> resources, boolean saveSnapshot) throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SparkAppHandle handler = null;
                String applicationId = null;
                String state = null;
                try {
                    applicationId = sparkSvc.submitSpark(sparkAppNameToSubmit,
                            appInstance.getJarMainClass(),
                            jar,appInstance.getJarArgs(),
                            appInstance.getSparkArgs(),null,resources);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(saveSnapshot){
                    try {
                        if(applicationId==null){
                            applicationId  = appInstance.getName()+"_"+System.currentTimeMillis();
                        }
                        appInstanceService.save(appInstance,applicationId,state);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }
}

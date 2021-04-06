package c2.service;

import c2.C2PlatformProperties;
import c2.common.SystemUtil;
import c2.model.AppInstance;
import c2.model.Project;
import c2.properties.C2Properties;
import c2.services.mvnRegistry.AbstractRegistrySvc;
import c2.services.mvnRegistry.RegistrySvcFactory;
import c2.services.mvnRegistry.model.Package;
import c2.services.yarn.YarnSvc;
import org.apache.commons.io.FileUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
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
    private String getSparkAppName(Project project, AppInstance app){
        return project.getName().replaceAll(" ","")+project.getId()+"_"+app.getName();
    }
    @Autowired AppInstanceService appInstanceService;
    @Autowired
    C2PlatformProperties c2PlatformProperties;
    @Autowired ProjectService projectService;
    @Autowired FileStorageService fileStorageService;

    @Async
    public void startSparkLauncher(SparkLauncher finalLauncher, AppInstance app){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
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
                    applicationId  = "failed_application_"+System.currentTimeMillis();
                }

                AppInstance appInstance = null;
                try {
                    appInstance = appInstanceService.save(app,applicationId,state);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private File getJar(C2Properties prop , String group, String artifact, String version){
        for (AbstractRegistrySvc reg: RegistrySvcFactory.create(prop)){
            Optional<Package> optionalPackage = reg.getPackage(group, artifact, version);
            if(optionalPackage.isPresent()){
                return reg.download(optionalPackage.get());
            }
        }
        return null;
    }

    private SparkLauncher addArgs(SparkLauncher launcher, List<String> args){
        for(String arg: args){
            launcher = launcher.addAppArgs(arg);
        }
        return launcher;
    }

    private SparkLauncher addSparkArgs(SparkLauncher launcher, Map<String, String> args){
        for(String key: args.keySet()){
            launcher = launcher.addSparkArg(key, args.get(key));
        }
        return launcher;
    }

    private SparkLauncher addFiles(SparkLauncher launcher, Project project, AppInstance appInstance, Map<String, c2.model.File> tmpFiles, boolean inheritProjectFiles) throws IOException {
        String baseDir = c2PlatformProperties.getTmp()+"/project_"+project.getId()+"/spark-submit";
        for(File f : new File(baseDir).listFiles()){
            if(FileUtils.isFileOlder(f, System.currentTimeMillis() - 1000*60*60*2)){
                FileUtils.deleteDirectory(f);
            }
        }
        String tmpDir = baseDir+"/"+System.currentTimeMillis()+"/"+appInstance.getName();
        Files.createDirectories(Paths.get(tmpDir));
        if(inheritProjectFiles){
            launcher = createTmpFiles(launcher, tmpDir, tmpFiles, project.getFileIds());
        }
        launcher = createTmpFiles(launcher, tmpDir, tmpFiles, appInstance.getFileIds());
        return launcher;
    }

    private SparkLauncher createTmpFiles(SparkLauncher launcher, String tmpDir, Map<String, c2.model.File> files, List<Long> fileIds) throws IOException {
        if(fileIds.size()==0){
            return launcher;
        }
        Iterator<c2.model.File> filesItrs =  fileStorageService.getFiles(fileIds).iterator();
        while (filesItrs.hasNext()) {
            c2.model.File f = filesItrs.next();
            files.put(f.getName(),f);
            Files.write(Paths.get(tmpDir).resolve(f.getName()), f.getFileBlob());
            launcher = launcher.addFile(Paths.get(tmpDir).resolve(f.getName()).toAbsolutePath().toString());
        }
        return launcher;
    }

    public AppInstance submitApp(String appId) throws Exception {
        Optional<AppInstance> appInstanceOptional = appInstanceService.findById(appId);
        if(!appInstanceOptional.isPresent()){
            throw new Exception("Submitted invalid AppId");
        }
        return  submitApp(appInstanceOptional.get(), false);
    }

    public AppInstance submitApp(AppInstance appInstance, boolean inheritProjectFiles) throws Exception {
        long launchTime = System.currentTimeMillis();
        Project project = projectService.findById(appInstance.getProjectId()).orElseGet(null);
        if (project==null) {
            throw new Exception("Project not found.");
        }

        C2Properties prop = (project.getEnv());
        loadYarnEnv(project);
        File jar = getJar( prop , appInstance.getJarGroupId(), appInstance.getJarArtifactId(), appInstance.getJarVersion());
        if (jar == null) {
            throw new Exception("Application artifact not found.");
        }

        String sparkAppNameToSubmit = getSparkAppName(project, appInstance);

        YarnSvc yarnSvc = new YarnSvc(c2PlatformProperties.getYarnHost());
        if(yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                .get().stream()
                .filter(f->f.getName().equalsIgnoreCase(sparkAppNameToSubmit))
                .count()>0){
            throw new Exception("App name, "+appInstance.getName()+" has been submitted");
        }

        SparkLauncher launcher= new SparkLauncher()
                .setSparkHome(c2PlatformProperties.getSparkHome())
                .setMaster("yarn")
                .setDeployMode("cluster")
                .setAppResource(jar.getAbsolutePath())
                .setMainClass(appInstance.getJarMainClass())
                .setAppName(sparkAppNameToSubmit);

        // add app args
        launcher = addArgs(launcher,appInstance.getJarArgs());
        // add spark args
        launcher = addSparkArgs(launcher,appInstance.getSparkArgs());

        // add files
        String baseDir = c2PlatformProperties.getTmp()+"/project_"+appInstance.getProjectId()+"/spark-submit";
        for(File f : new File(baseDir).listFiles()){
            if(FileUtils.isFileOlder(f, System.currentTimeMillis() - 1000*60*60*2)){
                FileUtils.deleteDirectory(f);
            }
        }
        Map<String, c2.model.File> tmpFiles = new HashMap<>();
        launcher = addFiles( launcher,  project,  appInstance,  tmpFiles, inheritProjectFiles);
        List<Long> fileIds = tmpFiles.entrySet().stream().map(f->f.getValue().getId()).collect(Collectors.toList());
        String tmpDir = baseDir+"/"+launchTime+"/"+appInstance.getName();
        Files.createDirectories(Paths.get(tmpDir));
        appInstance.setFileIds(fileIds);

        startSparkLauncher(launcher,appInstance);
        return appInstance;
    }

    public static void loadYarnEnv(Project project) throws Exception {
        C2Properties prop = (project.getEnv());
        String hadoopConfDir = "tmp/project_"+project.getId()+"/hadoopConf";
        File hadoopConfDirFile = new File(hadoopConfDir);
        hadoopConfDirFile.mkdirs();
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/core-site.xml"), prop.getHadoopProperties().getCoreSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/hdfs-site.xml"), prop.getHadoopProperties().getHdfsSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/yarn-site.xml"), prop.getHadoopProperties().getYarnSite(),"UTF-8");
        Map<String, String> env = new HashMap<>();
        env.put("HADOOP_CONF_DIR", hadoopConfDirFile.getAbsolutePath());
        SystemUtil.setEnv(env);
    }
}

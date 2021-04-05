package c2.service;

import c2.C2PlatformProperties;
import c2.dao.AppDao;
import c2.model.App;
import c2.model.AppInstance;
import c2.model.Project;
import c2.properties.C2Properties;
import c2.properties.C2PropertiesLoader;
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
import java.lang.reflect.Field;
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

    protected static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

    public AppInstance submitApp(long projectId, String appName) throws Exception {

        // launch app
        long launchTime = System.currentTimeMillis();
        Optional<App> appOptional = findById(projectId,appName);
        App app = null;
        if(!appOptional.isPresent()){
            throw new Exception("Invalid AppName");
        }else{
            app = appOptional.get();
        }
        Project project = projectService.findById(projectId).orElseGet(null);
        if (project==null) {
            throw new Exception("Project not found.");
        }

        C2Properties prop = (project.getEnv());
        String hadoopConfDir = "tmp/project_"+projectId+"/hadoopConf";
        File hadoopConfDirFile = new File(hadoopConfDir);
        hadoopConfDirFile.mkdirs();
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/core-site.xml"), prop.getHadoopProperties().getCoreSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/hdfs-site.xml"), prop.getHadoopProperties().getHdfsSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/yarn-site.xml"), prop.getHadoopProperties().getYarnSite(),"UTF-8");
        Map<String, String> env = new HashMap<>();
        env.put("HADOOP_CONF_DIR", hadoopConfDirFile.getAbsolutePath());
        setEnv(env);

        File jar = null;
        for (AbstractRegistrySvc reg: RegistrySvcFactory.create(prop)){
            Optional<Package> optionalPackage = reg.getPackage(app.getJarGroupId(), app.getJarArtifactId(), app.getJarVersion());
            if(optionalPackage.isPresent()){
                jar = reg.download(optionalPackage.get());
                break;
            }
        }

        String sparkAppNameToSubmit = project.getName().replaceAll(" ","")+projectId+"_"+app.getName();
        if(new YarnSvc(sparkProperties.getYarnHost()).setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING").get().stream().filter(f->f.getName().equalsIgnoreCase(sparkAppNameToSubmit))
                .count()>0){
            throw new Exception("App name, "+app.getName()+" has been submitted");
        }
        if (jar==null) {
            throw new Exception("Application artifact not found.");
        }
        String sparkMaster = "yarn";
        String deployMode = "cluster";
        SparkLauncher launcher= new SparkLauncher()
                .setSparkHome(sparkProperties.getSparkHome())
                .setMaster(sparkMaster)
                .setDeployMode(deployMode)
                .setAppResource(jar.getAbsolutePath())
                .setMainClass(app.getJarMainClass())
                .setAppName(sparkAppNameToSubmit);

        // add app args
        for(String arg: app.getJarArgs()){
            launcher = launcher.addAppArgs(arg);
        }

        // add spark args
        for(String key: app.getSparkArgs().keySet()){
            launcher = launcher.addSparkArg(key, app.getSparkArgs().get(key));
        }

        // add files
        String tmpDir = sparkProperties.getTmp()+"/command-center/"+projectId+"/"+app.getName()+"/"+launchTime;
        Files.createDirectories(Paths.get(tmpDir));

        List<Long> fileIds = project.getFileIds();
        Iterator<c2.model.File> filesItr =  fileStorageService.getFiles(fileIds).iterator();
        while (filesItr.hasNext()) {
            c2.model.File f = filesItr.next();
            Files.write(Paths.get(tmpDir).resolve(f.getName()), f.getFileBlob());
            launcher = launcher.addFile(Paths.get(tmpDir).resolve(f.getName()).toAbsolutePath().toString());
        }

        SparkLauncher finalLauncher = launcher;
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(applicationId==null){
            applicationId  = "failed_application_"+launchTime;
        }


        AppInstance appInstance = null;
        try {
            appInstance = appInstanceService.save(app,applicationId,fileIds,state);
            FileUtils.deleteDirectory(new File(tmpDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appInstance;

//        Runnable sparkLauncherRunnable =
//                new Runnable(){
//                    public void run(){
//                        SparkAppHandle handler = null;
//                        String applicationId = null;
//                        String state = null;
//                        try {
//                            handler = finalLauncher.startApplication();
//                            while(!handler.getState().isFinal()){
//                                applicationId = handler.getAppId();
//                                state = handler.getState().name();
//                                if(applicationId!=null){
//                                    break;
//                                }
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

//                        if(applicationId==null){
//                            applicationId  = "failed_application_"+launchTime;
//                        }

//                        try {
//                            appInstanceService.save(app,applicationId,fileIds,state);
//                            FileUtils.deleteDirectory(new File(tmpDir));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
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
        return new ArrayList<c2.model.File>();

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

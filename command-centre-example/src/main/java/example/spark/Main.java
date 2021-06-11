package example.spark;

import app.c2.properties.C2Properties;
import app.c2.properties.C2PropertiesLoader;
import app.c2.properties.GitProperties;
import app.c2.service.git.GitSvc;
import app.c2.service.maven.GitlabRegistrySvc;
import app.c2.service.maven.model.Package;
import org.apache.commons.io.FileUtils;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
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

    public static void main(String arg[]) throws Exception {
        long projectId = 1;
        String CCPropertiesFile = Main.class.getClassLoader().getResource("properties.yml").getPath();
        C2Properties prop = C2PropertiesLoader.load(CCPropertiesFile);
        String hadoopConfDir = "tmp/project_"+projectId+"/hadoopConf";
        File hadoopConfDirFile = new File(hadoopConfDir);
        hadoopConfDirFile.mkdirs();
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/core-site.xml"), prop.getHadoopYarnProperties().getCoreSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/hdfs-site.xml"), prop.getHadoopYarnProperties().getHdfsSite(),"UTF-8");
        FileUtils.writeStringToFile(new File(hadoopConfDir+"/yarn-site.xml"), prop.getHadoopYarnProperties().getYarnSite(),"UTF-8");
        Map<String, String> env = new HashMap<>();
        env.put("HADOOP_CONF_DIR", hadoopConfDirFile.getAbsolutePath());
        setEnv(env);

        String mavenRepoUrl = prop.getMavenProperties().get(0).getUrl();
        String mavenPrivateToken =  prop.getMavenProperties().get(0).getPrivateToken();

        // Download Jar from Maven
        String groupId = "c2";
        String artifactId = "spark-app";
        GitlabRegistrySvc reg = new GitlabRegistrySvc(mavenRepoUrl,null,null,mavenPrivateToken,"tmp/project_"+projectId+"/repository");
        List<Package> packages = reg.getPackages(groupId,artifactId);
        for(Package p : packages){
            System.out.println(p.getGroup()+":"+p.getArtifact()+":"+p.getVersion());
        }
        Package pkg = reg.getPackages(groupId, artifactId).stream().findFirst().get();
        String jarUrl = reg.download(pkg ).getAbsolutePath();

        // Download config from repo
        String configFile = "spark-app/src/main/resources/config.yml";
        String selectedBranch ="refs/heads/master";
        GitProperties repo = prop.getGitProperties().get(0);
        GitSvc svc = new GitSvc(repo.getUrl(),repo.getToken(),"tmp");
        File f = svc.getFile(selectedBranch,configFile);
        System.out.println(f.getAbsolutePath());
        System.out.println(f.getName());

        // Launch
        SparkAppHandle handler =
                new SparkLauncher()
                        .setSparkHome("D:\\spark-2.4.1-bin-hadoop2.7")
                        .setMaster( "yarn")
                        .setDeployMode("cluster")
                        .addSparkArg("spark.driver.memory", "1G")
                        .addSparkArg("spark.executor.instances", "1")
                        .addSparkArg("spark.executor.memory", "1G")
                        .addSparkArg("spark.executor.cores", "1")
                        .addFile(f.getAbsolutePath())
                        .setAppResource(jarUrl)
                        .setMainClass("app.SparkApp")
                        .addAppArgs(f.getName(), "app1")
                        .setAppName("test")
                        .startApplication();

        while(!handler.getState().isFinal()){
            System.out.println("Wait:Loop APP_ID : "+handler.getAppId()+" state: "+handler.getState());
        }
    }
}

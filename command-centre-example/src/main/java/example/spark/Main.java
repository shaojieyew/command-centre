package example.spark;

import c2.properties.C2Properties;
import c2.properties.C2PropertiesLoader;
import c2.properties.GitRepoServiceProperties;
import c2.services.git.GitSvc;
import c2.services.mvnRegistry.GitlabRegistrySvc;
import c2.services.mvnRegistry.model.Package;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String arg[]) throws IOException, GitAPIException {

        String mavenRepoHost = "https://gitlab.com";
        String mavenPrivateToken = "B8UxzhjZiBDJK51ZVHxY";
        String gitLabProjectId = "25216265";
        String groupId = "c2";
        String artifactId = "spark-app";

        String CCPropertiesFile = Main.class.getClassLoader().getResource("properties.yml").getPath();
        C2Properties prop = C2PropertiesLoader.load(CCPropertiesFile);

        String sparkMaster = "yarn";
        String deployMode = "cluster";

        // Download Jar from Maven
        GitlabRegistrySvc reg = new GitlabRegistrySvc(mavenRepoHost,mavenPrivateToken,gitLabProjectId,"tmp/repository");
        List<Package> packages = reg.getPackages(groupId,artifactId);
        for(Package p : packages){
            System.out.println(p.getGroup()+":"+p.getArtifact()+":"+p.getVersion());
        }
        Package pkg = reg.getPackages(groupId, artifactId).stream().findFirst().get();
        String jarUrl = reg.download(pkg).getAbsolutePath();

        // Download config from repo
        String configFile ="spark-app/src/main/resources/config.yml";
        String selectedBranch ="refs/heads/master";
        GitRepoServiceProperties repo = prop.getGitRemoteRepositoryProperties().get(0);
        GitSvc svc = new GitSvc(repo.getRemoteUrl(),repo.getToken());
        File f = svc.getFile(selectedBranch,configFile);
        System.out.println(f.getAbsolutePath());
    // Launch
    SparkAppHandle handler =
        new SparkLauncher()
            .setSparkHome("D:\\spark-2.4.1-bin-hadoop2.7")
            .setMaster(sparkMaster)
            .setDeployMode(deployMode)
            .addSparkArg("spark.driver.memory", "1G")
            .addSparkArg("spark.executor.instances", "1")
            .addSparkArg("spark.executor.memory", "1G")
            .addSparkArg("spark.executor.cores", "1")
            .addFile(f.getAbsolutePath())
            .setAppResource(jarUrl)
            .setMainClass("app.SparkApp")
            .addAppArgs(f.getCanonicalFile().getName(), "app1")
            .setAppName("test")
            .startApplication();

        while(!handler.getState().isFinal()){
            System.out.println("Wait:Loop APP_ID : "+handler.getAppId()+" state: "+handler.getState());
        }
    }
}

package example.artifact;

import c2.services.mvnRegistry.GitlabRegistrySvc;
import c2.services.mvnRegistry.model.Package;
import c2.services.util.JarAnalyzer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Main {

    // artifact downloader
    public static void main(String[] args) throws Exception {
        GitlabRegistrySvc reg = new GitlabRegistrySvc(
                "https://gitlab.com",
                "B8UxzhjZiBDJK51ZVHxY",
                "25216265",
                "tmp/project_1/maven/repository");
        List<Package> packages = reg.getPackages("c2","spark-app");
        for(Package p : packages){
            System.out.println(p.getGroup()+":"+p.getArtifact()+":"+p.getVersion());
        }
        System.out.println(reg.download(packages.get(0)).getAbsolutePath());
        Map<Class, Method> mains =  JarAnalyzer.getMainMethods(reg.download(packages.get(0)).getAbsolutePath());
    }
}

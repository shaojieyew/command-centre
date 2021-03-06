package example.artifact;

import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.GitlabRegistrySvc;
import app.c2.service.maven.model.Package;
import app.c2.service.util.JarAnalyzer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Main {
    // artifact downloader
    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        AbstractRegistrySvc reg = new GitlabRegistrySvc(
                "https://gitlab.com/api/v4/projects/25819110/packages/maven",
                null,null, "B8UxzhjZiBDJK51ZVHxY",
                "tmp/project_1/maven/repository");
        List<Package> packages = reg.getPackages("c2","spark-app");
        for(Package p : packages){
            System.out.println(p.getGroup()+":"+p.getArtifact()+":"+p.getVersion());
        }
        System.out.println(reg.download(packages.get(0)).getAbsolutePath());
        Map<Class, Method> mains =  JarAnalyzer.getMainMethods(reg.download(packages.get(0)).getAbsolutePath());
        mains.entrySet().forEach(s->System.out.println(s.getKey()+": "+s.getValue()));
    }
}

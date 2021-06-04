package app.task.spark;

import app.c2.service.git.GitSvc;
import app.c2.service.git.GitSvcFactory;
import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.MavenSvcFactory;
import app.c2.service.maven.model.Package;
import app.c2.service.spark.SparkSvc;
import app.c2.service.spark.SparkSvcFactory;
import app.c2.service.spark.model.SparkArgKeyValuePair;
import app.cli.SparkCli;
import app.spec.resource.Resource;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RunSparkApp extends Task {

    SparkCli cli;
    SparkDeploymentSpec spec;

    public RunSparkApp(SparkCli cli, SparkDeploymentKind kind, SparkDeploymentSpec spec){
        super();
        if(spec.getArtifact()==null && kind.getArtifact()!=null){
            spec.setArtifact(kind.getArtifact());
        }
        if(spec.getMainClass()==null && kind.getMainClass()!=null){
            spec.setMainClass(kind.getMainClass());
        }
        if(spec.getJarArgs()==null && kind.getJarArgs()!=null){
            spec.setJarArgs(kind.getJarArgs());
        }
        if(spec.getResources()==null && kind.getResources()!=null){
            spec.setResources(kind.getResources());
        }else{
            if(spec.getResources()!=null && kind.getResources()!=null){
                Set<String> names = spec.getResources().stream().map(s->s.getName().toUpperCase()).collect(Collectors.toSet());
                for (Resource resource : kind.getResources()) {
                    if(!names.contains(resource.getName().toUpperCase())){
                        spec.getResources().add(resource);
                    }
                }
            }
        }

        if(spec.getSparkArgs()==null && kind.getSparkArgs()!=null){
            spec.setSparkArgs(kind.getSparkArgs());
        }else{
            if(spec.getSparkArgs()!=null && kind.getSparkArgs()!=null){
                Set<String> sparkConfKey = spec.getSparkArgs().stream().map(s->s.getName().toUpperCase()).collect(Collectors.toSet());
                for (SparkArgKeyValuePair sparkArg : kind.getSparkArgs()) {
                    if(!sparkConfKey.contains(sparkArg.getName().toUpperCase())){
                        spec.getSparkArgs().add(sparkArg);
                    }
                }
            }
        }

        if(spec.getNamespace()==null && kind.getNamespace()!=null){
            spec.setNamespace(kind.getNamespace());
        }
        this.cli = cli;
        this.spec = spec;
    }

    @Override
    protected String getTaskName() {
        return "Run Spark Application - "+spec.getName();
    }

    @Override
    protected void preTask() throws Exception {
        if(spec==null){
            throw new Exception("Invalid application spec");
        }
    }

    @Override
    protected void postTask() throws Exception {
    }

    @Override
    protected void task() throws Exception {

        SparkSvc sparkSvc = SparkSvcFactory.create(cli.getC2CliProperties().getSparkHome(),cli.getC2CliProperties());

        Package pkg = new Package();

        String[] artifactParam = spec.getArtifact().split(":");
        if(artifactParam.length<3){
            throw new Exception("Invalid artifact '"+spec.getArtifact()+"'");
        }

        pkg.setGroup(artifactParam[0]);
        pkg.setArtifact(artifactParam[1]);
        pkg.setVersion(artifactParam[2]);
        pkg.setPackage_type(Package.PackageType.MAVEN);
        File jar = null;
        for (AbstractRegistrySvc abstractRegistrySvc : MavenSvcFactory.create(cli.getC2CliProperties())) {
            jar = abstractRegistrySvc.download(pkg);
            if(jar!=null){
                break;
            }
        }
        if(jar==null){
            throw new Exception("Artifact not found");
        }
        List<File> files = new ArrayList<>();
        if(spec.getResources()!=null) {
            for (Resource resource : spec.getResources()) {
                File file;
                String basePath = cli.getC2CliProperties().getTmpDirectory()+"/spark/"+System.currentTimeMillis();
                File finalFile;
                switch (resource.getType().toUpperCase()) {
                    case "GIT":
                        String[] sourceArr = resource.getSource().split("/-/");
                        String remoteUrl = sourceArr[0];
                        String branch = sourceArr[1];
                        String path = sourceArr[2];
                        GitSvc gitSvc = GitSvcFactory.create(cli.getC2CliProperties(),remoteUrl);
                        file = gitSvc.getFile(branch, path);
                        FileUtils.forceMkdir(new File(basePath));
                        finalFile = new File(basePath,resource.getName());
                        Files.copy(file.toPath(),
                                finalFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        files.add(finalFile);
                        break;
                    case "LOCAL":
                        file = new File(resource.getSource());
                        FileUtils.forceMkdir(new File(basePath));
                        finalFile = new File(basePath,resource.getName());
                        Files.copy(file.toPath(),
                                finalFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        files.add(finalFile);
                        break;
                    case "STRING":
                        FileUtils.forceMkdir(new File(basePath));
                        String filePath = basePath+"/"+resource.getName();
                        FileWriter myWriter = new FileWriter(filePath);
                        myWriter.write(resource.getSource());
                        myWriter.close();
                        file = new File(filePath);
                        FileUtils.forceMkdir(new File(basePath));
                        finalFile = new File(basePath,resource.getName());
                        Files.copy(file.toPath(),
                                finalFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        files.add(finalFile);
                        break;
                    default:
                }
            }
        }

        sparkSvc.submitSpark(spec.getName(), spec.getMainClass(), jar, spec.getJarArgs(), spec.getSparkArgs(), files);
        Thread.sleep(1000);
    }
}

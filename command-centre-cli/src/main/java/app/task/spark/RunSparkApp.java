package app.task.spark;

import app.c2.service.git.GitSvc;
import app.c2.service.git.GitSvcFactory;
import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.MavenSvcFactory;
import app.c2.service.maven.model.Package;
import app.c2.service.spark.SparkSvc;
import app.c2.service.spark.SparkSvcFactory;
import app.cli.SparkCli;
import app.spec.resource.Resource;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RunSparkApp extends Task {

    SparkCli cli;
    SparkDeploymentSpec spec;

    public RunSparkApp(SparkCli cli, SparkDeploymentSpec spec){
        super();
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
        pkg.setArtifact(spec.getJarArtifactId());
        pkg.setGroup(spec.getJarGroupId());
        pkg.setPackage_type(Package.PackageType.MAVEN);
        pkg.setVersion(spec.getJarVersion());
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
                switch (resource.getType().toUpperCase()) {
                    case "GIT":
                        String[] sourceArr = resource.getSource().split("/-/");
                        String remoteUrl = sourceArr[0];
                        String branch = sourceArr[1];
                        String path = sourceArr[2];
                        GitSvc gitSvc = GitSvcFactory.create(cli.getC2CliProperties(),remoteUrl);
                        file = gitSvc.getFile(branch, path);
                        files.add(file);
                        break;
                    case "LOCAL":
                        file = new File(resource.getSource());
                        files.add(file);
                        break;
                    case "STRING":
                        String filePath = cli.getC2CliProperties().getTmpDirectory()+"/spark/"+System.currentTimeMillis()+"/"+resource.getName();
                        FileOutputStream fos = new FileOutputStream(filePath);
                        DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
                        outStream.writeUTF(resource.getSource());
                        outStream.close();
                        file = new File(filePath);
                        files.add(file);
                        break;
                    default:
                }
            }
        }

        sparkSvc.submitSpark(spec.getName(), spec.getMainClass(), jar, spec.getJarArgs(), spec.getSparkArgs(), files);

    }
}

package app.task.spark;

import app.c2.service.git.GitSvc;
import app.c2.service.git.GitSvcFactory;
import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.MavenSvcFactory;
import app.c2.service.maven.model.Package;
import app.c2.service.spark.SparkSvc;
import app.c2.service.spark.SparkSvcFactory;
import app.c2.service.spark.model.SparkArgKeyValuePair;
import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import app.c2.service.yarn.model.YarnApp;
import app.cli.SparkCli;
import app.spec.resource.Resource;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.Task;
import app.util.DateHelper;
import app.util.YamlLoader;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class RunSparkApp extends Task {

    enum ResourceSourceType{
        STRING, LOCAL, GIT
    }

    SparkCli cli;
    SparkDeploymentSpec spec;

    boolean saveSnapshot = true;

    public boolean isSaveSnapshot() {
        return saveSnapshot;
    }

    public void setSaveSnapshot(boolean saveSnapshot) {
        this.saveSnapshot = saveSnapshot;
    }

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
        String appName = spec.getName();
        YarnSvc yarnSvc = YarnSvcFactory.create(cli.getC2CliProperties());
        Optional<YarnApp> yarnAppOptional = yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                .get().stream()
                .filter(f->f.getName().equalsIgnoreCase(appName)).findFirst();
        if(yarnAppOptional.isPresent()){
            throw new Exception("Spark application '"+appName+"' already submitted");
        }
    }

    @Override
    protected void postTask() throws Exception {
    }

    @Override
    protected void task() throws Exception {
        String appName = spec.getName();

        SparkSvc sparkSvc = SparkSvcFactory.create(cli.getC2CliProperties().getSparkHome(),cli.getC2CliProperties());

        File jar = null;
        File artifactJar = new File(spec.getArtifact());
        if(artifactJar.exists() && artifactJar.isFile()){
            jar = artifactJar;
        }else{
            Package pkg = new Package();

            String[] artifactParam = spec.getArtifact().split(":");
            if(artifactParam.length<3){
                throw new Exception("Invalid artifact '"+spec.getArtifact()+"'");
            }
            pkg.setGroup(artifactParam[0]);
            pkg.setArtifact(artifactParam[1]);
            pkg.setVersion(artifactParam[2]);
            pkg.setPackage_type(Package.PackageType.MAVEN);

            for (AbstractRegistrySvc abstractRegistrySvc : MavenSvcFactory.create(cli.getC2CliProperties(), cli.getC2CliProperties().getTmpDirectory())) {
                jar = abstractRegistrySvc.download(pkg);
                if(jar!=null){
                    break;
                }
            }
        }

        if(jar==null){
            throw new Exception("Artifact not found");
        }
        List<File> resources = new ArrayList<>();
        if(spec.getResources()!=null) {
            for (Resource resource : spec.getResources()) {
                File file;
                String basePath = cli.getC2CliProperties().getTmpDirectory()+ String.format("/%s/%s/%s", cli.getSparkSubmitDir(), appName, DateHelper.getDateString());
                File finalFile;

                if(resource.getType().equalsIgnoreCase(ResourceSourceType.GIT.name())){
                    String[] sourceArr = resource.getSource().split("/-/");
                    String remoteUrl = sourceArr[0];
                    String branch = sourceArr[1];
                    String path = sourceArr[2];
                    GitSvc gitSvc = GitSvcFactory.create(cli.getC2CliProperties(),remoteUrl, cli.getC2CliProperties().getTmpDirectory());
                    file = gitSvc.getFile(branch, path);
                    FileUtils.forceMkdir(new File(basePath));
                    finalFile = new File(basePath,resource.getName());
                    Files.copy(file.toPath(),
                            finalFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    resources.add(finalFile);
                }
                if(resource.getType().equalsIgnoreCase(ResourceSourceType.LOCAL.name())){
                    file = new File(resource.getSource());
                    FileUtils.forceMkdir(new File(basePath));
                    finalFile = new File(basePath,resource.getName());
                    Files.copy(file.toPath(),
                            finalFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    resources.add(finalFile);
                }
                if(resource.getType().equalsIgnoreCase(ResourceSourceType.STRING.name())){
                    FileUtils.forceMkdir(new File(basePath));
                    String filePath = String.format("%s/%s", basePath, resource.getName());
                    FileWriter myWriter = new FileWriter(filePath);
                    myWriter.write(resource.getSource());
                    myWriter.close();
                    file = new File(filePath);
                    FileUtils.forceMkdir(new File(basePath));
                    finalFile = new File(basePath,resource.getName());
                    Files.copy(file.toPath(),
                            finalFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    resources.add(finalFile);
                }
            }
        }
        sparkSvc.submitSpark(spec.getName(), spec.getMainClass(), jar, spec.getJarArgs(), spec.getSparkArgs(), resources);
        if(saveSnapshot){
            saveSnapshot(cli, spec,jar, resources);
        }
    }

    private void saveSnapshot(SparkCli cli, SparkDeploymentSpec spec, File jar, List<File> resources) throws IOException {
        String sparkAppSnapshotDirectory = String.format("%s/%s/%s", cli.getSparkSubmitDir(),spec.getName(), DateHelper.getDateString() );
        //copy artifact
        File artifactJar = new File(spec.getArtifact());
        if(artifactJar.exists() && artifactJar.isFile()){
            if(!artifactJar.getAbsolutePath().contains(new File(cli.getSparkSubmitJarDir()).getAbsolutePath())){
                File newJar = new File(String.format("%s/%s", sparkAppSnapshotDirectory,jar.getName()));
                FileUtils.copyFile(jar, newJar);
                spec.setArtifact(newJar.getAbsolutePath());
            }
        }else{
            String jarDirectory = cli.getSparkSubmitJarDir();
            int index = 0;
            for (String s : spec.getArtifact().split(":")) {
                if(index==0){
                    for (String s1 : s.split("\\.")) {
                        jarDirectory = jarDirectory +"/"+s1;
                    }
                }else{
                    jarDirectory = jarDirectory +"/"+s;
                }
                index++;
            }
            File newJar = new File(String.format("%s/%s/%s", cli.getSparkSubmitJarDir(),jarDirectory,jar.getName()));
            FileUtils.copyFile(jar, newJar);
            spec.setArtifact(newJar.getAbsolutePath());
        }

        // copy resources
        List<Resource> resourcesList = new ArrayList<>();
        for (File oldResourceFile : resources) {
            File newResourceFile = new File(String.format("%s/%s", sparkAppSnapshotDirectory, oldResourceFile.getName()));
            FileUtils.copyFile(oldResourceFile, newResourceFile);
            Resource resource = new Resource();
            resource.setName(newResourceFile.getName());
            resource.setType(RunSparkApp.ResourceSourceType.LOCAL.name());
            resource.setSource(newResourceFile.getAbsolutePath());
            resourcesList.add(resource);
        }
        spec.setResources(resourcesList);

        SparkDeploymentKind sparkDeploymentKind = new SparkDeploymentKind();
        List<SparkDeploymentSpec> specs = new ArrayList<>();
        specs.add(spec);
        sparkDeploymentKind.setSpec(specs);
        sparkDeploymentKind.setKind(cli.KIND_APP_DEPLOYMENT);
        YamlLoader yml = new YamlLoader(SparkDeploymentKind.class);
        yml.write(sparkDeploymentKind, String.format("%s/%s.yml", sparkAppSnapshotDirectory, spec.getName()));
    }
}

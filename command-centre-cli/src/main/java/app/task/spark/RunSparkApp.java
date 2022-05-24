package app.task.spark;

import app.c2.service.git.GitSvc;
import app.c2.service.git.GitSvcFactory;
import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.MavenSvcFactory;
import app.c2.service.maven.model.Package;
import app.c2.service.spark.SparkSvc;
import app.c2.service.spark.SparkSvcFactory;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    protected File getJar(String path) throws Exception {

        File mainJar = null;
        File artifactJar = new File(path);
        if(artifactJar.exists() && artifactJar.isFile()){
            mainJar = artifactJar;
        }else{
            Package pkg = new Package();

            String[] artifactParam = path.split(":");
            if(artifactParam.length<3){
                throw new Exception("Invalid artifact '"+path+"'");
            }
            pkg.setGroup(artifactParam[0]);
            pkg.setArtifact(artifactParam[1]);
            pkg.setVersion(artifactParam[2]);
            pkg.setPackage_type(Package.PackageType.MAVEN);

            for (AbstractRegistrySvc abstractRegistrySvc : MavenSvcFactory.create(cli.getC2CliProperties(), cli.getC2CliProperties().getTmpDirectory())) {
                mainJar = abstractRegistrySvc.download(pkg);
                if(mainJar!=null){
                    break;
                }
            }
        }
        return mainJar;
    }

    @Override
    protected void task() throws Exception {
        String appName = spec.getName();

        SparkSvc sparkSvc = SparkSvcFactory.create(cli.getC2CliProperties().getSparkHome(),cli.getC2CliProperties());

        File mainJarFile = getJar(spec.getArtifact());

        if(mainJarFile==null){
            throw new Exception("Artifact not found");
        }

        List<File> jarFiles = new ArrayList<>();
        if(spec.getJars()!=null) {
            for (String jar : spec.getJars()) {
                File jarFile = getJar(jar);
                if (jarFile != null) {
                    jarFiles.add(jarFile);
                }
            }
        }

        List<File> resources = new ArrayList<>();
        if(spec.getResources()!=null) {
            for (Resource resource : spec.getResources()) {
                File file;
                String basePath = cli.getC2CliProperties().getTmpDirectory()+ String.format("/%s/%s/%s", cli.getSparkSubmitDir(), appName, DateHelper.getDateString());
                File finalFile;

                if(resource.getType().equalsIgnoreCase(ResourceSourceType.GIT.name())){
                    if( cli.getC2CliProperties().getGitProperties().size()==0){
                        throw new IllegalStateException("No git properties found in c2 setting");
                    }

                    //set default master branch and first git setting
                    String[] sourceArr = resource.getSource().split("/-/");
                    String remoteUrl = cli.getC2CliProperties().getGitProperties().get(0).getUrl();
                    String branch = "refs/heads/master";
                    String path = resource.getSource();

                    if(sourceArr.length>=3){
                         remoteUrl = sourceArr[0];
                         branch = sourceArr[1];
                         path = sourceArr[2];
                    }

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
        sparkSvc.submitSpark(spec.getName(), spec.getMainClass(), mainJarFile, spec.getJarArgs(), spec.getSparkArgs(), jarFiles,  resources);
        if(saveSnapshot){
            saveSnapshot(cli, spec,mainJarFile, jarFiles, resources);
        }
    }

    private String copyArtifactToSnapshot(String sparkAppSnapshotDirectory, File artifactJar) throws IOException {
        if(!artifactJar.getAbsolutePath().contains(new File(cli.getSparkSubmitJarDir()).getAbsolutePath())){
            File newJar = new File(String.format("%s/%s", sparkAppSnapshotDirectory,artifactJar.getName()));
            FileUtils.copyFile(artifactJar, newJar);
            return newJar.getAbsolutePath();
        }else{
            return artifactJar.getAbsolutePath();
        }
    }

    private void saveSnapshot(SparkCli cli, SparkDeploymentSpec spec, File mainJar,  List<File> jars, List<File> resources) throws IOException {
        String sparkAppSnapshotDirectory = String.format("%s/%s/%s", cli.getSparkSubmitDir(),spec.getName(), DateHelper.getDateString() );
        //copy artifact
        spec.setArtifact(copyArtifactToSnapshot(sparkAppSnapshotDirectory, mainJar));
        List<String> jarsLocation = new ArrayList<>();
        for(File jar: jars){
            jarsLocation.add(copyArtifactToSnapshot(sparkAppSnapshotDirectory, jar));
        }
        spec.setJars(jarsLocation);

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

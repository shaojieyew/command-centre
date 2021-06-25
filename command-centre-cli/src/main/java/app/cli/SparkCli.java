package app.cli;

import app.c2.service.spark.model.SparkArgKeyValuePair;
import app.cli.type.Action;
import app.spec.Kind;
import app.spec.SpecException;
import app.spec.resource.Resource;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import app.task.spark.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SparkCli extends Cli {


    private static String SPARK_SUBMIT_DIR = "spark-submit";
    private static String SPARK_SUBMIT_JAR_DIR = "repository";

    private static org.slf4j.Logger LOG = LoggerFactory
            .getLogger(SparkCli.class);

    public List<Kind> getSpecFile() {
        return super.getSpecFile().stream()
                .filter(k->k.getKind().equalsIgnoreCase(Cli.KIND_APP_DEPLOYMENT))
                .collect(Collectors.toList());
    }

    @Override
    public Integer task() throws Exception {
        if(getCliAction().equalsIgnoreCase(Action.ls.toString())){
            new ListSparkApp(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.run.toString()) || getCliAction().equalsIgnoreCase(Action.start.toString())){
            new RunSparkApps(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.apply.toString())){
            new ApplySparkApps(this).startTask();
        } else if(getCliAction().equalsIgnoreCase(Action.stop.toString())){
            new StopSparkApps(this).startTask();
        }
        return 0;
    }

    public static Logger logger = LoggerFactory.getLogger(SparkCli.class);
    public List<SparkDeploymentKind> getSubmittedAppSpec() throws IOException {
        List<SparkDeploymentKind> kinds = new ArrayList<>();
        File dir = new File(getSparkSubmitDir());
        if(!dir.exists() || !dir.isDirectory()){
            return kinds;
        }
        for (File sparkSubmitDir : Objects.requireNonNull(dir.listFiles())) {
            long max = Long.MIN_VALUE;
            for (File file : Objects.requireNonNull(sparkSubmitDir.listFiles())) {
                try{
                    long date = Long.parseLong(file.getName());
                    if(date>max){
                        max = date;
                    }
                }catch (Exception e){

                }
            }

            if(max>Long.MIN_VALUE){
                String appDir = String.format("%s/%s", sparkSubmitDir.getAbsolutePath(),  max);
                try {
                    kinds.addAll(loadFile(appDir, false)
                            .stream()
                            .filter(k->k instanceof SparkDeploymentKind)
                            .map(k->(SparkDeploymentKind)k)
                            .collect(Collectors.toList()));
                } catch (IOException | SpecException e) {
                    logger.warn("cannot load files from = {}, reason = {}",dir ,e.getMessage());
                }
            }
        }
        return kinds;
    }

    /**
     * returns location of snapshot directory for spark spec and resources
     * @return
     */
    public String getSparkSubmitDir(){
        return  getC2CliProperties().getSparkSnapshotDirectory()+"/"+SPARK_SUBMIT_DIR;
    }

    /**
     * returns location of snapshot directory for jar binaries
     * @return
     */
    public String getSparkSubmitJarDir(){
        return  getC2CliProperties().getSparkSnapshotDirectory()+"/"+SPARK_SUBMIT_JAR_DIR;
    }

    /**
     * this method override and filter out all the Spark Kinds
     * @param kinds
     * @return
     */
    public static List<SparkDeploymentSpec> getSpecsFromKind(List<Kind> kinds){
        return  getSpecsFromSparkKind(kinds.stream()
                .filter(k->k instanceof SparkDeploymentKind)
                .map(k->(SparkDeploymentKind)k)
                .collect(Collectors.toList()));
    }

    /**
     * this method flatten nested Spec in Kind
     * @param kinds
     * @return list of flattened Spark Spec
     */
    public static List<SparkDeploymentSpec> getSpecsFromSparkKind(List<SparkDeploymentKind> kinds){
        return kinds.stream()
                .filter(k->k instanceof SparkDeploymentKind)
                .map(k->(SparkDeploymentKind)k)
                .flatMap(kind->kind.getSpec().stream()
                        .map(spec->{
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

                            if(spec.getEnableHealthCheck()==null && kind.getEnableHealthCheck()!=null){
                                spec.setEnableHealthCheck(kind.getEnableHealthCheck());
                            }

                            if(spec.getNamespace()==null && kind.getNamespace()!=null){
                                spec.setNamespace(kind.getNamespace());
                            }

                            return spec;
                        })).collect(Collectors.toList());
    }
}



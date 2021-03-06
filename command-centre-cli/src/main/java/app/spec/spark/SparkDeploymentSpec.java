package app.spec.spark;

import app.spec.resource.Resource;
import app.spec.Spec;
import app.c2.service.spark.model.SparkArgKeyValuePair;

import java.util.List;
import java.util.Set;

public class SparkDeploymentSpec implements Spec {
    private String name;
    private String artifact;
    private String mainClass;
    private List<String> jars;
    private List<String> jarArgs;
    private Set<SparkArgKeyValuePair> sparkArgs;
    private List<Resource> resources;
    private String namespace;
    private String enableHealthCheck;

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public List<String> getJarArgs() {
        return jarArgs;
    }

    public void setJarArgs(List<String> jarArgs) {
        this.jarArgs = jarArgs;
    }

    public Set<SparkArgKeyValuePair> getSparkArgs() {
        return sparkArgs;
    }

    public void setSparkArgs(Set<SparkArgKeyValuePair> sparkArgs) {
        this.sparkArgs = sparkArgs;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEnableHealthCheck() {
        return enableHealthCheck;
    }

    public void setEnableHealthCheck(String enableHealthCheck) {
        this.enableHealthCheck = enableHealthCheck;
    }


    public List<String> getJars() {
        return jars;
    }

    public void setJars(List<String> jars) {
        this.jars = jars;
    }

    @Override
    public String toString() {
        return "SparkDeploymentSpec{" +
                "name='" + name + '\'' +
                ", artifact='" + artifact + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", jars=" + jars +
                ", jarArgs=" + jarArgs +
                ", sparkArgs=" + sparkArgs +
                ", resources=" + resources +
                ", namespace='" + namespace + '\'' +
                ", enableHealthCheck='" + enableHealthCheck + '\'' +
                '}';
    }
}
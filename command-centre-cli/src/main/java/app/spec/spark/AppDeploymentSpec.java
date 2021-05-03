package app.spec.spark;

import app.spec.resource.Resource;
import app.spec.Spec;

import java.util.List;
import java.util.Map;

public class AppDeploymentSpec implements Spec {
    private String name;
    private String jarGroupId;
    private String jarArtifactId;
    private String jarVersion;
    private String mainClass;
    private List<String> jarArgs;
    private Map<String, String> sparkArgs;
    private List<Resource> resources;
    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJarGroupId() {
        return jarGroupId;
    }

    public void setJarGroupId(String jarGroupId) {
        this.jarGroupId = jarGroupId;
    }

    public String getJarArtifactId() {
        return jarArtifactId;
    }

    public void setJarArtifactId(String jarArtifactId) {
        this.jarArtifactId = jarArtifactId;
    }

    public String getJarVersion() {
        return jarVersion;
    }

    public void setJarVersion(String jarVersion) {
        this.jarVersion = jarVersion;
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

    public Map<String, String> getSparkArgs() {
        return sparkArgs;
    }

    public void setSparkArgs(Map<String, String> sparkArgs) {
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

    @Override
    public String toString() {
        return "AppDeploymentSpec{" +
                "name='" + name + '\'' +
                ", jarGroupId='" + jarGroupId + '\'' +
                ", jarArtifactId='" + jarArtifactId + '\'' +
                ", jarVersion='" + jarVersion + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", jarArgs=" + jarArgs +
                ", sparkArgs=" + sparkArgs +
                ", resources=" + resources +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
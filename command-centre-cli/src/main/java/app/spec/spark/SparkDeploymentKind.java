package app.spec.spark;

import app.c2.service.spark.model.SparkArgKeyValuePair;
import app.spec.Kind;
import app.spec.Spec;
import app.spec.SpecException;
import app.spec.resource.Resource;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

public class SparkDeploymentKind extends Kind<SparkDeploymentSpec> {
    private String artifact;
    private String mainClass;
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

    public void validate() throws SpecException {
        super.validate();
        for(Spec s : getSpec()){
            SparkDeploymentSpec spec = (SparkDeploymentSpec)s;
            if(StringUtils.isEmpty(spec.getName()) ){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.name");
            }
            if(StringUtils.isEmpty(spec.getArtifact())&&StringUtils.isEmpty(this.getArtifact())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.artifact");
            }
            if(StringUtils.isEmpty(spec.getMainClass())&&StringUtils.isEmpty(this.getMainClass())){
                throw new SpecException(getFileOrigin().getAbsolutePath()+ " - Missing spec.mainClass");
            }
        }
    }
}

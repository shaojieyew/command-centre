package app.c2.properties;

import java.util.List;

public class C2Properties {
    private NifiProperties nifiProperties;
    private List<MavenProperties> mavenProperties;
    private List<GitProperties> gitProperties;
    private YarnProperties hadoopYarnProperties;
    private SparkCheckpointProperties sparkCheckpointProperties;

    public NifiProperties getNifiProperties() {
        return nifiProperties;
    }

    public void setNifiProperties(NifiProperties nifiProperties) {
        this.nifiProperties = nifiProperties;
    }

    public List<MavenProperties> getMavenProperties() {
        return mavenProperties;
    }

    public void setMavenProperties(List<MavenProperties> mavenProperties) {
        this.mavenProperties = mavenProperties;
    }

    public List<GitProperties> getGitProperties() {
        return gitProperties;
    }

    public void setGitProperties(List<GitProperties> gitProperties) {
        this.gitProperties = gitProperties;
    }

    public YarnProperties getHadoopYarnProperties() {
        return hadoopYarnProperties;
    }

    public void setHadoopYarnProperties(YarnProperties hadoopYarnProperties) {
        this.hadoopYarnProperties = hadoopYarnProperties;
    }

    public SparkCheckpointProperties getSparkCheckpointProperties() {
        return sparkCheckpointProperties;
    }

    public void setSparkCheckpointProperties(SparkCheckpointProperties sparkCheckpointProperties) {
        this.sparkCheckpointProperties = sparkCheckpointProperties;
    }
}

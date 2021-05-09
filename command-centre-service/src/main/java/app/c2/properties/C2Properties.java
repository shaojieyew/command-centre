package app.c2.properties;

import app.c2.properties.GitProperties;
import app.c2.properties.HadoopProperties;
import app.c2.properties.MavenProperties;
import app.c2.properties.NifiProperties;

import java.util.List;

public class C2Properties {
    private NifiProperties nifiProperties;
    private List<MavenProperties> mavenProperties;
    private List<GitProperties> gitProperties;
    private HadoopProperties hadoopProperties;

    @Override
    public String toString() {
        return "C2Properties{" +
                "mavenProperties=" + mavenProperties +
                ", gitProperties=" + gitProperties +
                ", hadoopProperties=" + hadoopProperties +
                '}';
    }

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

    public void setHadoopProperties(HadoopProperties hadoopProperties) {
        this.hadoopProperties = hadoopProperties;
    }

    public HadoopProperties getHadoopProperties() {
        return  this.hadoopProperties;
    }
}

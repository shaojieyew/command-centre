package c2.properties;

import java.util.List;

public class C2Properties {
    // private HdfsServiceProperties hdfsProperties;
    // private NifiServiceProperties nifiProperties;
    private List<MavenProperties> mavenProperties;
    private List<GitProperties> gitProperties;
    private HadoopProperties hadoopProperties;

//
//    public HdfsServiceProperties getHdfsProperties() {
//        return hdfsProperties;
//    }
//
//    public void setHdfsProperties(HdfsServiceProperties hdfsProperties) {
//        this.hdfsProperties = hdfsProperties;
//    }
//
//    public NifiServiceProperties getNifiProperties() {
//        return nifiProperties;
//    }
//
//    public void setNifiProperties(NifiServiceProperties nifiProperties) {
//        this.nifiProperties = nifiProperties;
//    }

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


    public HadoopProperties getHadoopProperties() {
        return hadoopProperties;
    }

    public void setHadoopProperties(HadoopProperties hadoopProperties) {
        this.hadoopProperties = hadoopProperties;
    }
}

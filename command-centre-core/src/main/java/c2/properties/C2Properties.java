package c2.properties;

import java.util.List;

public class C2Properties {
    // private HdfsServiceProperties hdfsProperties;
    // private NifiServiceProperties nifiProperties;
    private List<MvnRepoServiceProperties> mvnRemoteRepositoryProperties;
    private List<GitRepoServiceProperties> gitRemoteRepositoryProperties;
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

    public List<MvnRepoServiceProperties> getMvnRemoteRepositoryProperties() {
        return mvnRemoteRepositoryProperties;
    }
    public void setMvnRemoteRepositoryProperties(List<MvnRepoServiceProperties> mvnRemoteRepositoryProperties) {
        this.mvnRemoteRepositoryProperties = mvnRemoteRepositoryProperties;
    }

    public List<GitRepoServiceProperties> getGitRemoteRepositoryProperties() {
        return gitRemoteRepositoryProperties;
    }

    public void setGitRemoteRepositoryProperties(List<GitRepoServiceProperties> gitRemoteRepositoryProperties) {
        this.gitRemoteRepositoryProperties = gitRemoteRepositoryProperties;
    }
}

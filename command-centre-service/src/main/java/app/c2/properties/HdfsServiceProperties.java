package app.c2.properties;

public class HdfsServiceProperties {
    private String coreSite;
    private String hdfsSite;
    private String hdfsBackupDirectory;
    private String hdfsWebHost;
    private String hdfsWebPort;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCoreSite() {
        return coreSite;
    }

    public void setCoreSite(String coreSite) {
        this.coreSite = coreSite;
    }

    public String getHdfsSite() {
        return hdfsSite;
    }

    public void setHdfsSite(String hdfsSite) {
        this.hdfsSite = hdfsSite;
    }

    public String getHdfsBackupDirectory() {
        return hdfsBackupDirectory;
    }

    public void setHdfsBackupDirectory(String hdfsBackupDirectory) {
        this.hdfsBackupDirectory = hdfsBackupDirectory;
    }

    public String getHdfsWebHost() {
        return hdfsWebHost;
    }

    public void setHdfsWebHost(String hdfsWebHost) {
        this.hdfsWebHost = hdfsWebHost;
    }

    public String getHdfsWebPort() {
        return hdfsWebPort;
    }

    public void setHdfsWebPort(String hdfsWebPort) {
        this.hdfsWebPort = hdfsWebPort;
    }
}
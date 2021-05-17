package app.c2.properties;

public class WebHdfsProperties {
    private String webHdfsHost;
    private String username;
    private KerberosProperties kerberos;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebHdfsHost() {
        return webHdfsHost;
    }

    public void setWebHdfsHost(String webHdfsHost) {
        this.webHdfsHost = webHdfsHost;
    }
    public KerberosProperties getKerberos() {
        return kerberos;
    }

    public void setKerberos(KerberosProperties kerberos) {
        this.kerberos = kerberos;
    }
}
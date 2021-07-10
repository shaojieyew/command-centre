package app.c2.properties;

public class HadoopProperties {
    private String coreSite;
    private String hdfsSite;
    private String yarnSite;
    private String webHdfsHost;
    private String []yarnHost;
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

    public String[] getYarnHost() {
        return yarnHost;
    }

    public void setYarnHost(String[] yarnHost) {
        this.yarnHost = yarnHost;
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

    public String getYarnSite() {
        return yarnSite;
    }

    public void setYarnSite(String yarnSite) {
        this.yarnSite = yarnSite;
    }

    public KerberosProperties getKerberos() {
        return kerberos;
    }

    public void setKerberos(KerberosProperties kerberos) {
        this.kerberos = kerberos;
    }
}
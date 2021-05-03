package app.c2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.app.c2")
public class C2PlatformProperties {
    private String sparkHome;
    private String tmp;
    private String yarnHost;
    private String nifiHost;

    public String getSparkHome() {
        return sparkHome;
    }

    public void setSparkHome(String sparkHome) {
        this.sparkHome = sparkHome;
    }

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    public String getYarnHost() {
        return yarnHost;
    }

    public void setYarnHost(String yarnHost) {
        this.yarnHost = yarnHost;
    }

    public String getNifiHost() {
        return nifiHost;
    }

    public void setNifiHost(String nifiHost) {
        this.nifiHost = nifiHost;
    }
}

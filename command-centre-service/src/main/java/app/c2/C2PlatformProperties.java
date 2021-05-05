package app.c2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.app.c2")
public class C2PlatformProperties {
    private String sparkHome;
    private String tmp;

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
}

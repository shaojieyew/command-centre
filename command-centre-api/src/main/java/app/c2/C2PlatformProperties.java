package app.c2;

import app.c2.properties.C2Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("c2.config")
public class C2PlatformProperties extends C2Properties {
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

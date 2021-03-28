package c2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.c2")
public class C2PlatformProperties {
    private String spark;
    private String tmp;
    private String yarn;

    public String getSpark() {
        return spark;
    }

    public void setSpark(String spark) {
        this.spark = spark;
    }

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    public String getYarn() {
        return yarn;
    }

    public void setYarn(String yarn) {
        this.yarn = yarn;
    }
}

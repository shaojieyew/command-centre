package app;

import app.c2.properties.C2Properties;

public class C2CliProperties extends C2Properties {
    private String sparkHome;
    private String tmpDirectory;

    public String getSparkHome() {
        return sparkHome;
    }

    public void setSparkHome(String sparkHome) {
        this.sparkHome = sparkHome;
    }

    public String getTmpDirectory() {
        return tmpDirectory;
    }

    public void setTmpDirectory(String tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
    }

    @Override
    public String toString() {
        return "C2CliProperties{" +
                "sparkHome='" + sparkHome + '\'' +
                ", tmpDirectory='" + tmpDirectory + '\'' +
                '}';
    }
}

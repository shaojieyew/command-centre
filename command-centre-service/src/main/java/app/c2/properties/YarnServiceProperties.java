package app.c2.properties;

public class YarnServiceProperties {
    private String yarnHost;
    private String yarnPort;

    public String getYarnHost() {
        return yarnHost;
    }

    public void setYarnHost(String yarnHost) {
        this.yarnHost = yarnHost;
    }

    public String getYarnPort() {
        return yarnPort;
    }

    public void setYarnPort(String yarnPort) {
        this.yarnPort = yarnPort;
    }

    @Override
    public String toString() {
        return "YarnServiceProperties{" +
                "yarnHost='" + yarnHost + '\'' +
                ", yarnPort='" + yarnPort + '\'' +
                '}';
    }
}
package app.c2.properties;

public class SparkServiceProperties {
    private String sparkHome;

    public String getSparkHome() {
        return sparkHome;
    }

    public void setSparkHome(String sparkHome) {
        this.sparkHome = sparkHome;
    }

    @Override
    public String toString() {
        return "SparkServiceProperties{" +
                "sparkHome='" + sparkHome + '\'' +
                '}';
    }
}
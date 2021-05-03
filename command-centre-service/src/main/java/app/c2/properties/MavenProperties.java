package app.c2.properties;

public class MavenProperties {
    private String host;
    private String privateToken;
    private String projectId;
    private String type;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public void setPrivateToken(String privateToken) {
        this.privateToken = privateToken;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MavenProperties{" +
                "host='" + host + '\'' +
                ", privateToken='" + privateToken + '\'' +
                ", projectId='" + projectId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
package app.c2.properties;

public class MavenProperties {
    private String url;
    private String privateToken;
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public void setPrivateToken(String privateToken) {
        this.privateToken = privateToken;
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
                "host='" + url + '\'' +
                ", privateToken='" + privateToken + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
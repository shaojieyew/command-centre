package app.c2.properties;

public class GitProperties {
    private String url;
    private String token;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "GitProperties{" +
                "remoteUrl='" + url + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
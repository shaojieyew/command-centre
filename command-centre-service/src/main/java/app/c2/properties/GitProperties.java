package app.c2.properties;

public class GitProperties {
    private String remoteUrl;
    private String token;

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
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
                "remoteUrl='" + remoteUrl + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
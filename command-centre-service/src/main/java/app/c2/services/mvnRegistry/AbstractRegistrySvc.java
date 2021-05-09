package app.c2.services.mvnRegistry;

import app.c2.C2PlatformProperties;
import app.c2.services.mvnRegistry.downloader.ArtifactDownloader;
import app.c2.services.mvnRegistry.model.Package;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractRegistrySvc {
    private String localRepository;
    private String remoteUrl;
    private String privateToken;
    ArtifactDownloader downloader = new ArtifactDownloader();

    public AbstractRegistrySvc(String remoteUrl, String privateToken, String localRepository) {
        this.remoteUrl = remoteUrl;
        this.privateToken = privateToken;
        this.localRepository = localRepository;
    }

    public AbstractRegistrySvc(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getPrivateToken() {
        return privateToken;
    }

    public void setPrivateToken(String privateToken) {
        this.privateToken = privateToken;
    }

    public final static String type = "";

    /**
     * get list of packages in registry
     * @return
     */
    abstract public List<Package> getPackages();

    /**
     * get list of packages in registry filtered by group
     * @param group
     * @return
     */
    abstract public List<Package> getPackages(String group);

    /**
     * get list of packages in registry filtered by group and artifact
     * @param group
     * @param artifact
     * @return
     */
    abstract public List<Package> getPackages(String group, String artifact);
    /**
     * get list of packages in registry filtered by group and artifact
     * @param group
     * @param artifact
     * @return
     */
    abstract public Optional<Package> getPackage(String group, String artifact, String version);

    /**
     * download package
     * @param pkg
     * @return returns the downloaded pkg as File
     */
    public File download(Package pkg) {
        ArtifactDownloader downloader = new ArtifactDownloader();
        downloader.setRemoteRepoUrl(remoteUrl);
        if(privateToken!=null && privateToken.length()>0){
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("PRIVATE-TOKEN",privateToken);
            downloader.setHeaders(headers);
        }
        downloader.setLocalRepoPath(localRepository);
        return downloader.download(pkg.getGroup(), pkg.getArtifact(),pkg.getVersion(),"","jar");
    }
}

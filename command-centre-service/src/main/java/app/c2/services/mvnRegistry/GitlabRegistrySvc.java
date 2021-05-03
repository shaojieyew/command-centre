package app.c2.services.mvnRegistry;

import app.c2.services.mvnRegistry.downloader.ArtifactDownloader;
import app.c2.services.mvnRegistry.model.Package;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import util.HttpService;
import util.Util;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class GitlabRegistrySvc extends AbstractRegistrySvc {
    final static Logger logger = Util.initLogger(GitlabRegistrySvc.class);

    public final static String type = "gitlab";
    private String host;
    private String privateToken;
    private String projectId;

    public GitlabRegistrySvc(String host, String privateToken, String projectId, String localRepository) {
        this.host = host;
        this.privateToken = privateToken;
        this.projectId = projectId;
        this.localRepository = localRepository;
    }

    @Override
    public List<Package> getPackages() {
        List<Package> packages = new ArrayList<>();
        String url = host+"/api/v4/projects/"+projectId+"/packages";
        String strResponse = "";
        HashMap<String,String> requestMap = new HashMap();
        requestMap.put("content-type","application/json");
        requestMap.put("PRIVATE-TOKEN",privateToken);
        try {
            HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.GET, url, requestMap, null);
            int statusCode = con.getResponseCode();
            strResponse = HttpService.inputStreamToString(con.getInputStream());
            if(statusCode != 200){
                throw new Exception(strResponse);
            }

            JSONParser parser = new JSONParser();
            JSONArray jsonPackages = (JSONArray)parser.parse(strResponse);
            for(int i = 0 ;i < jsonPackages.size(); i ++){
                JSONObject jsonPackage = (JSONObject)jsonPackages.get(i);
                Package artifactPackage = new Package();
                String name = jsonPackage.get("name").toString();
                artifactPackage.setGroup(name.substring(0,name.lastIndexOf("/")));
                artifactPackage.setArtifact(name.substring(name.lastIndexOf("/")+1,name.length()));
                artifactPackage.setVersion(jsonPackage.get("version").toString());
                artifactPackage.setId(jsonPackage.get("id").toString());
                if(jsonPackage.get("package_type").toString().equals("maven")){
                    artifactPackage.setPackage_type(Package.PackageType.MAVEN);
                    packages.add(artifactPackage);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return packages;
    }

    @Override
    public List<Package> getPackages(String group) {
        return getPackages().stream().filter(pkg->pkg.getGroup().equalsIgnoreCase(group)).collect(Collectors.toList());
    }


    @Override
    public List<Package> getPackages(String group, String artifact) {
        return getPackages().stream()
                .filter(pkg->(
                        pkg.getGroup().equalsIgnoreCase(group) && pkg.getArtifact().equalsIgnoreCase(artifact)
                )).collect(Collectors.toList());
    }

    @Override
    public Optional<Package> getPackage(String group, String artifact, String version) {
        return getPackages().stream()
                .filter(pkg->(
                        pkg.getGroup().equalsIgnoreCase(group) && pkg.getArtifact().equalsIgnoreCase(artifact) && pkg.getVersion().equalsIgnoreCase(version)
                )).findFirst();
    }

    @Override
    public File download(Package pkg) {
        ArtifactDownloader downloader = new ArtifactDownloader();
        downloader.setRemoteRepoUrl(host+"/api/v4/projects/"+projectId+"/packages/maven");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("PRIVATE-TOKEN",privateToken);
        downloader.setHeaders(headers);
        downloader.setLocalRepoPath(localRepository);
        return downloader.download(pkg.getGroup(), pkg.getArtifact(),pkg.getVersion(),"","jar");
    }

}

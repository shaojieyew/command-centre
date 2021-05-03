package app.c2.services.git;

import app.c2.properties.C2Properties;
import app.c2.properties.GitProperties;

import java.util.*;

/**
 * Creating GitSvc using from C2Properties
 */
public class GitSvcFactory {
    public static List<GitSvc> create(C2Properties prop){
        List<GitSvc> svcs = new ArrayList<>();
        prop.getGitProperties().forEach(gitProp->{
            GitSvc gitSvc = new GitSvc(gitProp.getRemoteUrl(), gitProp.getToken());
            svcs.add(gitSvc);
        });
        return svcs;
    }
    public static GitSvc create(C2Properties prop, String remoteUrl){
        Optional<GitProperties> repoProp =prop.getGitProperties().stream().filter(gitProp->
            gitProp.getRemoteUrl().equalsIgnoreCase(remoteUrl)
        ).findFirst();
        if(repoProp.isPresent()){
            GitSvc gitSvc = new GitSvc(repoProp.get().getRemoteUrl(),
                    repoProp.get().getToken());
            return gitSvc;
        }
        return null;
    }
}

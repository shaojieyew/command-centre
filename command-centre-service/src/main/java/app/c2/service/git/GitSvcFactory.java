package app.c2.service.git;

import app.c2.properties.C2Properties;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * Creating GitSvc using from C2Properties
 */
public class GitSvcFactory {

    public static List<GitSvc> create(C2Properties prop){
        return new GitSvcFactory().createSvc(prop);
    }
    public static GitSvc create(C2Properties prop, String remoteUrl){
        return new GitSvcFactory().createSvc(prop, remoteUrl);
    }

    public List<GitSvc> createSvc(C2Properties prop){
        String localRepository = "tmp/git/repository";
        List<GitSvc> svcs = new ArrayList<>();
        prop.getGitProperties().forEach(gitProp->{
            String hash = DigestUtils.md5Hex((gitProp.getUrl()).getBytes());
            GitSvc gitSvc = new GitSvc(gitProp.getUrl(), gitProp.getToken(), localRepository+"/"+hash);
            svcs.add(gitSvc);
        });
        return svcs;
    }

    public GitSvc createSvc(C2Properties prop, String remoteUrl){
        List<GitSvc> svcs = createSvc(prop);
        return svcs.stream().filter(s->s.getRemoteUrl().equalsIgnoreCase(remoteUrl)).findFirst().orElseGet(null);
    }
}

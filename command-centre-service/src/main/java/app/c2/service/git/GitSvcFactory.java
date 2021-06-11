package app.c2.service.git;

import app.c2.properties.C2Properties;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * Creating GitSvc using from C2Properties
 */
public class GitSvcFactory {

    public static List<GitSvc> create(C2Properties prop, String tmpDir){
        return new GitSvcFactory().createSvc(prop, tmpDir);
    }
    public static GitSvc create(C2Properties prop, String remoteUrl, String tmpDir){
        return new GitSvcFactory().createSvc(prop, remoteUrl, tmpDir);
    }

    public List<GitSvc> createSvc(C2Properties prop, String tmpDir){
        String localRepository = tmpDir+"/git/repository";
        List<GitSvc> svcs = new ArrayList<>();
        prop.getGitProperties().forEach(gitProp->{
            String hash = DigestUtils.md5Hex((gitProp.getUrl()).getBytes());
            GitSvc gitSvc = new GitSvc(gitProp.getUrl(), gitProp.getToken(), localRepository+"/"+hash);
            svcs.add(gitSvc);
        });
        return svcs;
    }

    public GitSvc createSvc(C2Properties prop, String remoteUrl, String tmpDir){
        List<GitSvc> svcs = createSvc(prop, tmpDir);
        return svcs.stream().filter(s->s.getRemoteUrl().equalsIgnoreCase(remoteUrl)).findFirst().orElseGet(null);
    }
}

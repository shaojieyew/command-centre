package app.c2.services.git;

import app.c2.C2PlatformProperties;
import app.c2.common.SpringContext;
import app.c2.properties.C2Properties;
import app.c2.properties.GitProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Creating GitSvc using from C2Properties
 */
@Service
public class GitSvcFactory {

    private C2PlatformProperties getC2PlatformProperties() {
        return SpringContext.getBean(C2PlatformProperties.class);
    }

    public static List<GitSvc> create(C2Properties prop){
        return new GitSvcFactory().createSvc(prop);
    }
    public static GitSvc create(C2Properties prop, String remoteUrl){
        return new GitSvcFactory().createSvc(prop, remoteUrl);
    }

    public List<GitSvc> createSvc(C2Properties prop){
        String localRepository = getC2PlatformProperties().getTmp()+"/git/repository";
        List<GitSvc> svcs = new ArrayList<>();
        prop.getGitProperties().forEach(gitProp->{
            String hash = DigestUtils.md5Hex((gitProp.getRemoteUrl()).getBytes());
            GitSvc gitSvc = new GitSvc(gitProp.getRemoteUrl(), gitProp.getToken(), localRepository+"/"+hash);
            svcs.add(gitSvc);
        });
        return svcs;
    }

    public GitSvc createSvc(C2Properties prop, String remoteUrl){
        List<GitSvc> svcs = createSvc(prop);
        return svcs.stream().filter(s->s.getRemoteUrl().equalsIgnoreCase(remoteUrl)).findFirst().orElseGet(null);
    }
}

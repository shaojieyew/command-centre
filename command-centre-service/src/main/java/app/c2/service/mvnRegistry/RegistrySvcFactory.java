package app.c2.service.mvnRegistry;

import app.c2.C2PlatformProperties;
import app.c2.common.SpringContext;
import app.c2.properties.C2Properties;

import java.util.ArrayList;
import java.util.List;

public class RegistrySvcFactory {

    private C2PlatformProperties getC2PlatformProperties() {
        return SpringContext.getBean(C2PlatformProperties.class);
    }

    public static List<AbstractRegistrySvc> create(C2Properties props){
        return new RegistrySvcFactory().createSvc(props);
    }

    public List<AbstractRegistrySvc> createSvc(C2Properties props){
        List<AbstractRegistrySvc> svcs = new ArrayList<>();
        String localRepository = getC2PlatformProperties().getTmp()+"/maven/repository";
        props.getMavenProperties().forEach(mvnProp->{
            switch(mvnProp.getType()){
                case GitlabRegistrySvc.type:
                    svcs.add(new GitlabRegistrySvc(mvnProp.getUrl(),mvnProp.getPrivateToken(),localRepository));
                    break;
                case JFrogRegistrySvc.type:
                    svcs.add( new JFrogRegistrySvc(mvnProp.getUrl(),mvnProp.getPrivateToken(),localRepository));
                    break;
                default:
            }
        });
        return svcs;
    }
}

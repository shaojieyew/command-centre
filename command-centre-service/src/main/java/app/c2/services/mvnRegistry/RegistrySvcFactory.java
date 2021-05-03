package app.c2.services.mvnRegistry;

import app.c2.properties.C2Properties;
import java.util.ArrayList;
import java.util.List;

public class RegistrySvcFactory {
    public static List<AbstractRegistrySvc> create(C2Properties props){
        List<AbstractRegistrySvc> svcs = new ArrayList<>();
        props.getMavenProperties().forEach(mvnProp->{
            switch(mvnProp.getType()){
                case GitlabRegistrySvc.type:
                    svcs.add(new GitlabRegistrySvc(mvnProp.getHost(),mvnProp.getPrivateToken(),mvnProp.getProjectId(),"tmp/repository"));
                    break;
                case JFrogRegistrySvc.type:
                    svcs.add( new JFrogRegistrySvc(mvnProp.getHost()));
                    break;
                default:
            }
        });
        return svcs;
    }
}

package app.c2.service.maven;

import app.c2.properties.C2Properties;

import java.util.ArrayList;
import java.util.List;

public class MavenSvcFactory {

    public static List<AbstractRegistrySvc> create(C2Properties props){
        return new MavenSvcFactory().createSvc(props);
    }

    public List<AbstractRegistrySvc> createSvc(C2Properties props){
        List<AbstractRegistrySvc> svcs = new ArrayList<>();
        String localRepository = "tmp/maven/repository";
        props.getMavenProperties().forEach(mvnProp->{
            switch(mvnProp.getType()){
                case GitlabRegistrySvc.type:
                    svcs.add(new GitlabRegistrySvc(mvnProp.getUrl(),mvnProp.getUsername(), mvnProp.getPassword(), mvnProp.getPrivateToken(),localRepository));
                    break;
                case JFrogRegistrySvc.type:
                    svcs.add( new JFrogRegistrySvc(mvnProp.getUrl(),mvnProp.getUsername(), mvnProp.getPassword(), mvnProp.getPrivateToken(),localRepository));
                    break;
                default:
                    svcs.add( new MavenRegistrySvc(mvnProp.getUrl(),mvnProp.getUsername(), mvnProp.getPassword(), mvnProp.getPrivateToken(),localRepository));
                    break;
            }
        });
        return svcs;
    }
}

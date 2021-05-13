package app.c2.services.yarn;

import app.c2.properties.C2Properties;
import app.c2.properties.KerberosProperties;

public class YarnSvcFactory {
    public  static YarnSvc create(C2Properties props){
        YarnSvc yarnSvc = new YarnSvc(props.getHadoopYarnProperties().getYarnHost());
        if(props.getHadoopYarnProperties().getKerberos()!=null){
            KerberosProperties kerberosProperties = props.getNifiProperties().getKerberos();
            if(kerberosProperties.getKeytab()!=null && kerberosProperties.getPrinciple()!=null){
                yarnSvc.setPrinciple(kerberosProperties.getPrinciple());
                yarnSvc.setKeytab(kerberosProperties.getKeytab());
            }
        }
        return yarnSvc;
    }
}
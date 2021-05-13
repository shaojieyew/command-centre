package app.c2.services.nifi;

import app.c2.properties.C2Properties;
import app.c2.properties.KerberosProperties;

public class NifiSvcFactory {
    public  static NifiSvc create(C2Properties props){
        NifiSvc nifiSvc = new NifiSvc(props.getNifiProperties().getHost());
        if(props.getNifiProperties().getKerberos()!=null){
            KerberosProperties kerberosProperties = props.getNifiProperties().getKerberos();
            if(kerberosProperties.getKeytab()!=null && kerberosProperties.getPrinciple()!=null){
                nifiSvc.setPrinciple(kerberosProperties.getPrinciple());
                nifiSvc.setKeytab(kerberosProperties.getKeytab());
            }
        }
        return nifiSvc;
    }
}

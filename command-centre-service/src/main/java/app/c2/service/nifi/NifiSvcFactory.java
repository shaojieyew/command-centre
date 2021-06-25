package app.c2.service.nifi;

import app.c2.properties.C2Properties;
import app.c2.properties.KerberosProperties;

public class NifiSvcFactory {
    public  static NifiSvcV2 create(C2Properties props) throws Exception {
        NifiSvcV2 nifiSvc = new NifiSvcV2(props.getNifiProperties().getHost());
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

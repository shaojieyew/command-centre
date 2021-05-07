package app.c2.services.hdfs;

import app.c2.properties.C2Properties;
import app.c2.properties.KerberosProperties;
import app.c2.services.yarn.YarnSvc;

public class HdfsSvcFactory {
    public  static HdfsSvc create(C2Properties props){
        HdfsSvc hdfsSvc = new HdfsSvc(props.getHadoopProperties().getWebHdfsHost(),"c2");
        if(props.getHadoopProperties().getKerberos()!=null){
            KerberosProperties kerberosProperties = props.getNifiProperties().getKerberos();
            if(kerberosProperties.getKeytab()!=null && kerberosProperties.getPrinciple()!=null){
                hdfsSvc.setPrinciple(kerberosProperties.getPrinciple());
                hdfsSvc.setKeytab(kerberosProperties.getKeytab());
            }
        }
        return hdfsSvc;
    }
}
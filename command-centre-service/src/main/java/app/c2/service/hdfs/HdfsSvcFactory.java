package app.c2.service.hdfs;

import app.c2.properties.HadoopProperties;
import app.c2.properties.KerberosProperties;

public class HdfsSvcFactory {
    public  static HdfsSvc create(HadoopProperties props){
        HdfsSvc hdfsSvc = new HdfsSvc();
        hdfsSvc.setWebHdfsUrl(props.getWebHdfsHost());
        hdfsSvc.setUsername(props.getUsername()!=null?props.getUsername():"user");
        if(props.getKerberos()!=null){
            KerberosProperties kerberosProperties = props.getKerberos();
            if(kerberosProperties.getKeytab()!=null && kerberosProperties.getPrinciple()!=null){
                hdfsSvc.setPrinciple(kerberosProperties.getPrinciple());
                hdfsSvc.setUsername(kerberosProperties.getPrinciple());
                hdfsSvc.setKeytab(kerberosProperties.getKeytab());
            }
        }
        return hdfsSvc;
    }
}
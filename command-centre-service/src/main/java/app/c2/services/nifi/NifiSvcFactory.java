package app.c2.services.nifi;

import app.c2.properties.C2Properties;

public class NifiSvcFactory {
    public  static NifiSvc create(C2Properties props){
        return new NifiSvc(props.getNifiProperties().getHost()+":"+props.getNifiProperties().getPort());
    }
}

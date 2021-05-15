package app.c2.service.kafka;

import app.c2.properties.C2Properties;
import app.c2.properties.KerberosProperties;
import app.c2.properties.SparkCheckpointProperties;
import app.c2.service.hdfs.HdfsSvc;
import app.c2.service.hdfs.HdfsSvcFactory;

public class SparkCheckpointSvcFactory {
    public static SparkCheckpointSvc create(C2Properties prop){
        SparkCheckpointProperties cpProp = prop.getSparkCheckpointProperties();
        cpProp.getWebHdfsProperties();
        HdfsSvc hdfsSvc = HdfsSvcFactory.create(prop.getSparkCheckpointProperties().getWebHdfsProperties());
        SparkCheckpointSvc sparkCheckpointSvc
                = new SparkCheckpointSvc(hdfsSvc, prop.getSparkCheckpointProperties().getKafkaProperties().getKafkaHosts());
        KerberosProperties hdfsKerberosProperties = prop.getSparkCheckpointProperties().getWebHdfsProperties().getKerberos();
        KerberosProperties kafkaKerberosProperties = prop.getSparkCheckpointProperties().getKafkaProperties().getKerberos();
        if(hdfsKerberosProperties!=null && hdfsKerberosProperties.getPrinciple()!=null && hdfsKerberosProperties.getKeytab()!=null){
            sparkCheckpointSvc.setHdfsKerberosProperties(hdfsKerberosProperties);
        }
        if(kafkaKerberosProperties!=null && kafkaKerberosProperties.getPrinciple()!=null && kafkaKerberosProperties.getKeytab()!=null){
            sparkCheckpointSvc.setKafkaKerberosProperties(kafkaKerberosProperties);
        }
        return sparkCheckpointSvc;
    }

}

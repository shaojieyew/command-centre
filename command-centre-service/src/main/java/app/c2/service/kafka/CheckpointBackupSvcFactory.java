package app.c2.service.kafka;

import app.c2.properties.C2Properties;
import app.c2.properties.HadoopProperties;
import app.c2.service.hdfs.FileBackupSvc;
import app.c2.service.hdfs.HdfsSvc;
import app.c2.service.hdfs.HdfsSvcFactory;

public class CheckpointBackupSvcFactory {
    public static FileBackupSvc create(C2Properties properties){
        HdfsSvc hdfsSvc = HdfsSvcFactory.create(properties.getSparkCheckpointProperties().getWebHdfsProperties());
        return new FileBackupSvc(hdfsSvc, properties.getSparkCheckpointProperties().getBackupDirectory());
    }
}
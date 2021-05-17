package app.c2.service.hdfs;

import app.c2.properties.HadoopProperties;
import app.c2.properties.KerberosProperties;

public class FileBackupSvcFactory {
    public static FileBackupSvc create(HadoopProperties props, String backupDir){
        HdfsSvc hdfsSvc = HdfsSvcFactory.create(props);
        return new FileBackupSvc(hdfsSvc, backupDir);
    }
}
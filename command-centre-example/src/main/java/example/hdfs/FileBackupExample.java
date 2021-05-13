package example.hdfs;

import app.c2.properties.C2Properties;
import app.c2.properties.C2PropertiesLoader;
import app.c2.services.hdfs.FileBackupSvc;
import app.c2.services.hdfs.HdfsSvc;
import app.c2.services.hdfs.HdfsSvcFactory;
import com.google.common.io.Resources;

import java.util.stream.Collectors;

public class FileBackupExample {

    public static void main(String argp[]) throws Exception {
        String path = Resources.getResource("setting.yml").getPath();
        C2Properties c2PlatformProperties =  C2PropertiesLoader.load(path   );
        HdfsSvc hdfsSvc = HdfsSvcFactory.create(c2PlatformProperties);
        String backupDirectory = "/user/backmanager/backup";
        FileBackupSvc fm = new FileBackupSvc(hdfsSvc, backupDirectory);
        // hdfs and core site is required for file copy

        //mock

        hdfsSvc.deleteFile(backupDirectory, true);
        hdfsSvc.deleteFile("/user/testFolder1", true);
        hdfsSvc.deleteFile("/user/testFolder2", true);
        hdfsSvc.createDirectory("/user/testFolder1/abc");
        hdfsSvc.createDirectory("/user/testFolder2/def");
        hdfsSvc.createDirectory("/user/testFolder/zxc");
        hdfsSvc.createDirectory("/user/testFolder/zxy");


        //backup1
        String backupLists [][]= {{"/user/testFolder1","/user/testFolder2"},{"/user/testFolder"}};
        for(String[] backupList: backupLists){
            fm.backup("backup1",backupList);
        }

        //get all backups
        fm.getAllBackup().forEach(x->{
            System.out.println(x.getTimestamp()+" "+x.getName());
        });

        //restore
        for (String backupId: fm.getAllBackup().stream().map(x->x.getId()).collect(Collectors.toList())){
            fm.restoreBackup(backupId);
        }
    }



}

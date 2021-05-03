package example.hdfs;

import app.c2.services.hdfs.FileBackupSvc;
import app.c2.services.hdfs.HdfsSvc;
import example.spark.Main;

import java.util.stream.Collectors;

public class FileBackupExample {

    public static void main(String argp[]) throws Exception {
        String webHdfsHost = "http://localhost:9001";
        String username = "user";
        String backupDirectory = "/user/backmanager/backup";
        String coreSite  = Main.class.getClassLoader().getResource("core-site.xml").getPath();
        String hdfsSite  = Main.class.getClassLoader().getResource("hdfs-site.xml").getPath();
        FileBackupSvc fm = new FileBackupSvc(webHdfsHost, backupDirectory,username,coreSite, hdfsSite);
        // hdfs and core site is required for file copy

        //mock
        HdfsSvc hdfsSvc = new HdfsSvc(webHdfsHost, username);
        hdfsSvc.deleteFile(backupDirectory, true);
        hdfsSvc.deleteFile("/user/testFolder1", true);
        hdfsSvc.deleteFile("/user/testFolder2", true);
        hdfsSvc.createDirectory("/user/testFolder1/abc");
        hdfsSvc.createDirectory("/user/testFolder2/def");
        hdfsSvc.createDirectory("/user/testFolder/zxc");
        hdfsSvc.createDirectory("/user/testFolder/zxy");


        //backup1
        String backupLists [][]= {{"/user/testFolder1","/user/testFolder2"},{"/user/testFolder"}};
        boolean removeSrc = true;
        for(String[] backupList: backupLists){
            fm.backup("backup1",backupList, removeSrc);
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

package app.c2.service.hdfs;

import app.c2.service.hdfs.model.Backup;
import app.c2.service.hdfs.model.FileStatus;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * FileBackupSvc backup files in hdfs to backupDirectory
 * List backup files
 * Restore backup-ed files
 */
public class FileBackupSvc {
    HdfsSvc hdfsSvc;
    String backupDirectory;
    String tmpDirectory;


    public FileBackupSvc(HdfsSvc hdfsSvc, String backupDirectory) {
        this.hdfsSvc = hdfsSvc;
        this.backupDirectory = backupDirectory +"/backup";
        this.tmpDirectory = backupDirectory +"/tmp";
    }

    public void backup(String name, String[] backupList)  {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Long timestamp = System.currentTimeMillis();
        String backupId = DatatypeConverter
                .printHexBinary(digest.digest(
                        (timestamp+name).getBytes(StandardCharsets.UTF_8))).toUpperCase();
        Timestamp createdTimestamp = new Timestamp(timestamp);
        Backup backup = new Backup(createdTimestamp, name,backupId);
        for(String folderToBackup: backupList){
            String oldFilePath = folderToBackup;
            String FileName = oldFilePath.split("/")[oldFilePath.split("/").length-1];
            String oldLocation = oldFilePath.substring(0,oldFilePath.length()-FileName.length()-1);
            String newLocation = backupDirectory +"/"+backup.getFolderName()+oldLocation+"/"+"@BASE";
            if(hdfsSvc.createDirectory(newLocation)){
//                if(removeSrc){
                    hdfsSvc.renameFile(oldFilePath,newLocation);
//                }else{
//                    hdfsSvc.copyFile(oldFilePath,newLocation);
//                }
            }
        }
    }

    public List<Backup> getAllBackup()  {
        List<Backup> backups = new ArrayList<>();
        try {
            for(FileStatus backupFs: hdfsSvc.getFileStatusList(backupDirectory)){
                    Backup backup = new Backup(backupFs.getPathSuffix());
                    backups.add(backup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backups;
    }

    public boolean restoreBackup(String backupId){
        return restoreBackup( backupId, false);
    }

    public boolean restoreBackup(String backupId, boolean ignoreConflict) {
        Backup backup = getBackup(backupId);
        try{
            String path = backupDirectory + "/" + backup.getFolderName();
            String recoverLocation = "";
            String fname = "";
            while(!fname.equals("@BASE")){
                fname = hdfsSvc.getFileStatusList(path).get(0).getPathSuffix();
                path = path +"/"+fname;
                if(!fname.equals("@BASE"))
                    recoverLocation = recoverLocation +"/"+fname;
            }
            boolean isSuccess = true;

            List<FileStatus> srcDirFiles = hdfsSvc.getFileStatusList(recoverLocation);
            List<String> conflictFile = new ArrayList<>();
            for(FileStatus file: hdfsSvc.getFileStatusList(path)){
                if(srcDirFiles.stream().anyMatch(srcDirFile->{
                    return srcDirFile.getPathSuffix().equalsIgnoreCase(file.getPathSuffix())
                            && srcDirFile.getType().equalsIgnoreCase(file.getType());
                })){
                    conflictFile.add(file.getPathSuffix());
                }
            }

            if(conflictFile.size()>0 ){
                if(ignoreConflict){
                    String finalRecoverLocation = recoverLocation;
                    conflictFile.stream().forEach(f->{
                        hdfsSvc.deleteFile(finalRecoverLocation +"/"+f, true);
                    });
                }else{
                    throw new Exception("Could not restore backup file(s) to original location as following file(s) exist at "+recoverLocation+". "+ StringUtils.join(conflictFile,","));
                }
             }

            for(FileStatus folder: hdfsSvc.getFileStatusList(path)){
                fname = folder.getPathSuffix();
//                if(deleteBackup){
                    boolean isRenamed = hdfsSvc.renameFile(path+"/"+fname,recoverLocation);
                    if(!isRenamed)
                        isSuccess = false;
                    if(isSuccess){
                        deleteBackup(backupId);
                    }
//                }else{
//                    boolean isCopied =  hdfsSvc.copyFile(path+"/"+fname,recoverLocation);
//                    if(!isCopied)
//                        isSuccess = false;
//                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Backup getBackup(String backupId){
        return getAllBackup().stream().filter(r->r.getId().equals(backupId)).findFirst().orElseGet(null);
    }

    public boolean deleteBackup(String backupId){
        Backup backup = getBackup(backupId);
        try{
            String path = backupDirectory + "/" + backup.getFolderName();
            hdfsSvc.deleteFile(path , true);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}

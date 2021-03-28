package c2.services.hdfs;

import c2.services.hdfs.model.Backup;
import c2.services.hdfs.model.FileStatus;
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
    HdfsSvc dfs;
    String webHdfsUrl;
    String backupDirectory;
    String tmpDirectory;
    String username;
    String coreSite;
    String hdfsSite;

    public FileBackupSvc(String webHdfsHost, String backupDirectory, String username, String coreSite, String hdfsSite) {
        this.webHdfsUrl = webHdfsHost;
        this.backupDirectory = backupDirectory +"/backup";
        this.tmpDirectory = backupDirectory +"/tmp";
        this.username = username;
        this.coreSite = coreSite;
        this.hdfsSite = hdfsSite;
        init();
    }

    public void init(){
        dfs = new HdfsSvc(webHdfsUrl,username);
        dfs.setCoreSiteXmlLocation(coreSite);
        dfs.setHdfsSiteXmlLocation(hdfsSite);
        dfs.createDirectory(backupDirectory);
    }

    public void backup(String name, String[] backupList, boolean removeSrc)  {
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
            if(dfs.createDirectory(newLocation)){
                if(removeSrc){
                    dfs.renameFile(oldFilePath,newLocation);
                }else{
                    dfs.copyFile(oldFilePath,newLocation);
                }
            }
        }
    }

    public List<Backup> getAllBackup()  {
        List<Backup> backups = new ArrayList<>();
        try {
            for(FileStatus backupFs: dfs.getFileStatusList(backupDirectory)){
                try{
                    Backup backup = new Backup(backupFs.getPathSuffix());
                    backups.add(backup);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return backups;
    }

    public boolean restoreBackup(String backupId){
        return restoreBackup( backupId, false, false);
    }

    public boolean restoreBackup(String backupId, boolean ignoreConflict, boolean deleteBackup) {
        Backup backup = getBackup(backupId);
        try{
            String path = backupDirectory + "/" + backup.getFolderName();
            String recoverLocation = "";
            String fname = "";
            while(!fname.equals("@BASE")){
                fname = dfs.getFileStatusList(path).get(0).getPathSuffix();
                path = path +"/"+fname;
                if(!fname.equals("@BASE"))
                    recoverLocation = recoverLocation +"/"+fname;
            }
            boolean isSuccess = true;

            List<FileStatus> srcDirFiles = dfs.getFileStatusList(recoverLocation);
            List<String> conflictFile = new ArrayList<>();
            for(FileStatus file: dfs.getFileStatusList(path)){
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
                        dfs.deleteFile(finalRecoverLocation +"/"+f, true);
                    });
                }else{
                    throw new Exception("Could not restore backup file(s) to original location as following file(s) exist at "+recoverLocation+". "+ StringUtils.join(conflictFile,","));
                }
             }

            for(FileStatus folder: dfs.getFileStatusList(path)){
                fname = folder.getPathSuffix();
                if(deleteBackup){
                    boolean isRenamed = dfs.renameFile(path+"/"+fname,recoverLocation);
                    if(!isRenamed)
                        isSuccess = false;
                    if(isSuccess){
                        deleteBackup(backupId);
                    }
                }else{
                    boolean isCopied =  dfs.copyFile(path+"/"+fname,recoverLocation);
                    if(!isCopied)
                        isSuccess = false;
                }
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
            dfs.deleteFile(path , true);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}

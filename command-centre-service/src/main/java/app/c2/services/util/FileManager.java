package app.c2.services.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileManager {

    public static void clean(String path) throws IOException {
        clean(path, 24);
    }

    public static void clean(String path, int olderThanHours) throws IOException {
        File targetPath = new File(path);
        if(!targetPath.exists()){
            return;
        }
        if(targetPath.isFile()){
            if(FileUtils.isFileOlder(targetPath, System.currentTimeMillis() - ((long) 1000 * 60 * 60 * olderThanHours))){
                FileUtils.forceDelete(targetPath);
            }
        }else{
            Arrays.stream(targetPath.listFiles()).forEach(f->{
                try {
                    clean(f.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            if(targetPath.listFiles().length==0){
                FileUtils.deleteDirectory(targetPath);
            }
        }
    }
}

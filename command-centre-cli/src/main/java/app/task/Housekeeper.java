package app.task;

import app.c2.service.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Housekeeper implements Runnable{

    private Logger logger = LoggerFactory.getLogger(Housekeeper.class);

    private String path;
    public Housekeeper(String path) {
        this.path = path;
    }

    public static void houseKeep(String path){
        Thread thread = new Thread(new Housekeeper(path));
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        File file = new File(path);

        if(!file.exists()){
            logger.warn("invalid file path: {}", path);
        }

        if(!file.isDirectory()){
            logger.warn("path is not a directory: {}", path);
        }

        try {
            FileManager.clean(path, 4, true);
        } catch (IOException e) {
        }

    }
}

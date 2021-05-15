package app.c2.service.hdfs.model;

import java.sql.Timestamp;

public class Backup{
    private Timestamp timestamp;
    private String name;
    private String id;

    public Backup(Timestamp timestamp, String name, String id) {
        this.timestamp = timestamp;
        this.name = name.replace("_","");
        this.id = id;
    }
    public Backup(String folderName) {
        this(new Timestamp(Long.parseLong(folderName.split("_")[2])), folderName.split("_")[1],folderName.split("_")[0]);
    }

    public String getFolderName(){
        return id+"_"+name+"_"+timestamp.getTime();
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
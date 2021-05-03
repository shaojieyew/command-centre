package app.c2.services.hdfs.model;

public class FileStatus {
    private long accessTime;
    private long blockSize;
    private long childrenNum;
    private long fileId;
    private String group;
    private long length;
    private long modificationTime;
    private String owner;
    private String pathSuffix;
    private String permission;
    private long replication;
    private long storagePolicy;
    private String type;

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getChildrenNum() {
        return childrenNum;
    }

    public void setChildrenNum(long childrenNum) {
        this.childrenNum = childrenNum;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPathSuffix() {
        return pathSuffix;
    }

    public void setPathSuffix(String pathSuffix) {
        this.pathSuffix = pathSuffix;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public long getReplication() {
        return replication;
    }

    public void setReplication(long replication) {
        this.replication = replication;
    }

    public long getStoragePolicy() {
        return storagePolicy;
    }

    public void setStoragePolicy(long storagePolicy) {
        this.storagePolicy = storagePolicy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

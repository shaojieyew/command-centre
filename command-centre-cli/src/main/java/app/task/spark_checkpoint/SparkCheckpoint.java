package app.task.spark_checkpoint;

public  class SparkCheckpoint{
    private String checkpointPath;
    private String topic;
    private long offsetBacklog;

    public SparkCheckpoint(String checkpointPath, String topic, long offsetBacklog) {
        this.checkpointPath = checkpointPath;
        this.topic = topic;
        this.offsetBacklog = offsetBacklog;
    }

    public String getCheckpointPath() {
        return checkpointPath;
    }

    public void setCheckpointPath(String checkpointPath) {
        this.checkpointPath = checkpointPath;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getOffsetBacklog() {
        return offsetBacklog;
    }

    public void setOffsetBacklog(long offsetBacklog) {
        this.offsetBacklog = offsetBacklog;
    }
}
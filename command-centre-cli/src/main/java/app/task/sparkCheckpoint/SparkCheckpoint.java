package app.task.sparkCheckpoint;

public  class SparkCheckpoint{
    String checkpointPath;
    String topic;
    long offsetBacklog;

    public SparkCheckpoint(String checkpointPath, String topic, long offsetBacklog) {
        this.checkpointPath = checkpointPath;
        this.topic = topic;
        this.offsetBacklog = offsetBacklog;
    }
}
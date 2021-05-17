package app.c2.properties;

public class SparkCheckpointProperties {
    private KafkaProperties kafkaProperties;
    private HadoopProperties webHdfsProperties;
    private String backupDirectory;

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    public KafkaProperties getKafkaProperties() {
        return kafkaProperties;
    }

    public void setKafkaProperties(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public HadoopProperties getWebHdfsProperties() {
        return webHdfsProperties;
    }

    public void setWebHdfsProperties(HadoopProperties hadoopProperties) {
        this.webHdfsProperties = hadoopProperties;
    }
}
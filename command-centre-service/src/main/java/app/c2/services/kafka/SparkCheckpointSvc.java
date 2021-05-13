package app.c2.services.kafka;

import app.c2.properties.C2PropertiesLoader;
import app.c2.properties.KerberosProperties;
import app.c2.services.hdfs.HdfsSvc;
import app.c2.services.hdfs.model.FileStatus;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


public class SparkCheckpointSvc {
    private static Logger LOG = LoggerFactory
            .getLogger(SparkCheckpointSvc.class);

    public List<String> bootstrapServers ;
    public HdfsSvc hdfsSvc;
    public KerberosProperties kafkaKerberosProperties;
    public KerberosProperties hdfsKerberosProperties;

    public static void main(String arg[]) throws Exception {
        SparkCheckpointSvc sparkCheckpointSvc
                = SparkCheckpointSvcFactory.create(C2PropertiesLoader.load("C:\\Users\\YSJ\\.c2\\setting.yml"));
        Set<String> checkpointLocations = new HashSet<>();
        checkpointLocations.add("/user/YSJ/checkpoint");
        sparkCheckpointSvc.getKafkaBacklogWithCheckpoints(checkpointLocations);
        return;
    }

    public KerberosProperties getKafkaKerberosProperties() {
        return kafkaKerberosProperties;
    }

    public void setKafkaKerberosProperties(KerberosProperties kafkaKerberosProperties) {
        this.kafkaKerberosProperties = kafkaKerberosProperties;
    }

    public KerberosProperties getHdfsKerberosProperties() {
        return hdfsKerberosProperties;
    }

    public void setHdfsKerberosProperties(KerberosProperties hdfsKerberosProperties) {
        this.hdfsKerberosProperties = hdfsKerberosProperties;
    }

    public SparkCheckpointSvc(HdfsSvc hdfsSvc, List<String> bootstrapServers){
        this.bootstrapServers = bootstrapServers;
        this.hdfsSvc = hdfsSvc;
    }

    public static Set<String> getTopics( Map<String, Map<TopicPartition, Long>> checkpoints){
        return (checkpoints.entrySet()
                .stream().flatMap(cp->cp.getValue().keySet().stream()
                        .map(topicPartition->topicPartition.topic()).distinct())).distinct().collect(Collectors.toSet());
    }

    public Map<String, Map<String, Long>> getKafkaBacklogWithCheckpoints(Set<String> checkpointDirs) throws Exception {
        Map<String, Map<TopicPartition, Long>> checkpoints = getCheckpoints(checkpointDirs);
        Map<String, Map<String, Long>> aggCheckpoint = new HashMap<>();
        Set<String> topics = SparkCheckpointSvc.getTopics(checkpoints);
        Map<TopicPartition, Long> endOffsets = getKafkaEndOffset(topics);
        checkpoints.keySet().stream().forEach(checkpointPath->{
            Map<TopicPartition, Long> checkpointsOffset = checkpoints.get(checkpointPath);
            Map<String, Long> result = new HashMap<>();

            endOffsets.keySet().forEach(topicPartition->{
                String topic = topicPartition.topic();
                if(checkpointsOffset.keySet().stream().filter(s->s.topic().equals(topic)).findFirst().isPresent()){
                    long currentOffset = 0;
                    if(checkpointsOffset.containsKey(topicPartition)){
                        currentOffset = checkpointsOffset.get(topicPartition);
                    }
                    long endOffset = endOffsets.get(topicPartition);
                    long backlog = endOffset-currentOffset;
                    if(result.containsKey(topic)){
                        result.put(topic, backlog+result.get(topic));
                    }else{
                        result.put(topic, backlog);
                    }
                }
            });
            aggCheckpoint.put(checkpointPath, result);
        });

        return aggCheckpoint;
    }

    public Map<String, Map<TopicPartition, Long>> getCheckpoints(String baseCheckpointDirs) throws Exception {
        return getCheckpoints(Arrays.stream(baseCheckpointDirs.split(",")).collect(Collectors.toSet()));
    }

    public Map<String, Map<TopicPartition, Long>> getCheckpointsWithHint(String checkpointDirs) throws Exception {
        return getCheckpointsWithHint(Arrays.stream(checkpointDirs.split(",")).collect(Collectors.toSet()));
    }

    public Map<String, Map<TopicPartition, Long>> getCheckpoints(Set<String> baseCheckpointDirs) throws Exception {
        Map<String, Map<TopicPartition, Long>> checkpointInfo = new HashMap<>();
        Set<String> checkpointDirs = new HashSet<>();
        for(String path : baseCheckpointDirs){
            if(path.charAt(path.length()-1) == '/'){
                path=path.substring(0,path.length()-1);
            }
            detectSparkStreamingCheckpointDir(path.replace("\\","/"), checkpointDirs);
        }
        return getCheckpointsWithHint(checkpointDirs);
    }

    public Map<String, Map<TopicPartition, Long>> getCheckpointsWithHint( Set<String> checkpointDirs) throws Exception {
        Map<String, Map<TopicPartition, Long>> checkpointInfo = new HashMap<>();
        for(String path : checkpointDirs) {
            Map<String, Map<TopicPartition, Long>> newCheckpointInfo = extractCheckpointInfoFromOffsetFile(path);
            newCheckpointInfo.forEach((pathName, partitionOffsets) -> checkpointInfo.merge(pathName, partitionOffsets, (oldCheckpoint, newCheckpoint) -> {
                newCheckpoint.forEach((partition, offSets) -> oldCheckpoint.merge(partition, offSets, (oldPartitionOffset, newPartitionOffset) -> {
                    if (oldPartitionOffset > newPartitionOffset) {
                        return newPartitionOffset;
                    } else {
                        return oldPartitionOffset;
                    }
                }));
                return oldCheckpoint;
            }));
        }
        return checkpointInfo;
    }

    public void detectSparkStreamingCheckpointDir(String path, Set<String> dirs) throws Exception {
        List<FileStatus> files = hdfsSvc.getFileStatusList(path);
        boolean isCheckpointDir = false;
        if(files.size()<10){
            if(files.stream().filter(fs->{
                return (fs.getPathSuffix().equalsIgnoreCase("commits") && fs.getType().equalsIgnoreCase("DIRECTORY"))
                        || (fs.getPathSuffix().equalsIgnoreCase("metadata") && fs.getType().equalsIgnoreCase("FILE"))
                        || (fs.getPathSuffix().equalsIgnoreCase("offsets") && fs.getType().equalsIgnoreCase("DIRECTORY"))
                        || (fs.getPathSuffix().equalsIgnoreCase("sources") && fs.getType().equalsIgnoreCase("DIRECTORY"));
            }).count()==4){
                isCheckpointDir = true;
                dirs.add(path);
            }
        }
        if(!isCheckpointDir){
            for(FileStatus fs : files){
                if(fs.getType().equalsIgnoreCase("DIRECTORY")){
                    try{
                        detectSparkStreamingCheckpointDir(path+"/"+fs.getPathSuffix(), dirs);
                    }catch (Exception ex){
                        LOG.warn(ex.getMessage());
                    }
                }
            }
        }
    }

//    public void detectSparkStreamingKafkaCheckpointDir(String path, Map<String, Map<TopicPartition, Long>> checkpointInfo) throws Exception {
//        List<FileStatus> files = hdfsSvc.getFileStatusList(path);
//        boolean isCheckpointDir = false;
//        if(files.size()<10){
//            if(files.stream().filter(fs->{
//                return (fs.getPathSuffix().equalsIgnoreCase("commits") && fs.getType().equalsIgnoreCase("DIRECTORY"))
//                        || (fs.getPathSuffix().equalsIgnoreCase("metadata") && fs.getType().equalsIgnoreCase("FILE"))
//                        || (fs.getPathSuffix().equalsIgnoreCase("offsets") && fs.getType().equalsIgnoreCase("DIRECTORY"))
//                        || (fs.getPathSuffix().equalsIgnoreCase("sources") && fs.getType().equalsIgnoreCase("DIRECTORY"));
//            }).count()==4){
//                isCheckpointDir = true;
//                Map<String, Map<TopicPartition, Long>> newCheckpointInfo = extractCheckpointInfoFromOffsetFile(path);
//                newCheckpointInfo.forEach((pathName, partitionOffsets) -> checkpointInfo.merge(pathName, partitionOffsets, (oldCheckpoint, newCheckpoint) -> {
//                    newCheckpoint.forEach((partition, offSets) -> oldCheckpoint.merge(partition, offSets, (oldPartitionOffset, newPartitionOffset) -> {
//                        if(oldPartitionOffset>newPartitionOffset){
//                            return newPartitionOffset;
//                        }else{
//                            return oldPartitionOffset;
//                        }
//                    }));
//                    return oldCheckpoint;
//                }));
//            }
//        }
//        if(!isCheckpointDir){
//            for(FileStatus fs : files){
//                if(fs.getType().equalsIgnoreCase("DIRECTORY")){
//                    try{
//                        detectSparkStreamingKafkaCheckpointDir(path+"/"+fs.getPathSuffix(), checkpointInfo);
//                    }catch (Exception ex){
//                        LOG.warn(ex.getMessage());
//                    }
//                }
//            }
//        }
//    }

    public String getCheckpointFile(String path) throws Exception {
        Map<String, Map<TopicPartition, Long>> checkpointInfo = new HashMap<>();
        if(path.charAt(path.length()-1) == '/'){
            path=path.substring(0,path.length()-1);
        }
        String []paths = path.split("/");
        List<FileStatus> files = hdfsSvc.getFileStatusList(path+"/offsets");
        long maxOffset = Long.MIN_VALUE;
        for (FileStatus file : files) {
            long folderOffset = Long.parseLong(file.getPathSuffix());
            if(folderOffset>maxOffset){
                maxOffset = folderOffset;
            }
        }
        if(Long.MIN_VALUE<maxOffset){
            return hdfsSvc.readFile(path+"/offsets/"+maxOffset);
        }
        return null;
    }

    private Map<String, Map<TopicPartition, Long>> extractCheckpointInfoFromOffsetFile(String path) throws Exception {
        Map<String, Map<TopicPartition, Long>> checkpointInfo = new HashMap<>();
        if(path.charAt(path.length()-1) == '/'){
            path=path.substring(0,path.length()-1);
        }
        String []paths = path.split("/");
        List<FileStatus> files = hdfsSvc.getFileStatusList(path+"/offsets");
        long maxOffset = Long.MIN_VALUE;
        for (FileStatus file : files) {
            long folderOffset = Long.parseLong(file.getPathSuffix());
            if(folderOffset>maxOffset){
                maxOffset = folderOffset;
            }
        }
        if(Long.MIN_VALUE<maxOffset){
            String[] result = hdfsSvc.readFile(path+"/offsets/"+maxOffset).split("\n");
            if(result.length<=2){
                return checkpointInfo;
            }

            Map<TopicPartition, Long> partitionOffset= new HashMap<>();
            for(int i=2; i<result.length;i++){
                org.json.simple.parser.JSONParser parser = new JSONParser();
                JSONObject checkpointJson = (JSONObject)parser.parse(result[i]);
                checkpointJson.keySet().stream().forEach(topic->{
                    JSONObject partitionJson = ((JSONObject)checkpointJson.get(topic));
                    partitionJson.keySet().stream().forEach(partition->{
                        long offset = (Long)partitionJson.get(partition);
                        TopicPartition topicPartition = new TopicPartition((String)topic,Integer.parseInt((String) partition));
                        if(partitionOffset.containsKey(topicPartition)){
                            long existingOffset = partitionOffset.get(topicPartition);
                            if(offset<existingOffset){
                                partitionOffset.put(topicPartition, offset);
                            }
                        }else{
                            partitionOffset.put(topicPartition, offset);
                        }
                    });
                });
            }
            checkpointInfo.put(path,partitionOffset);

        }
        return checkpointInfo;
    }

    public Map<TopicPartition, Long> getKafkaEndOffset(Set<String> topics) throws Exception {
//        System.setProperty("java.security.auth.login.config", "/home/user/jaas-client.conf");
        Properties props = new Properties();
        props.put("bootstrap.servers", String.join(",",bootstrapServers));
        props.put("group.id", UUID.randomUUID().toString());
        props.put("key.deserializer", ByteArrayDeserializer.class.getName());
        props.put("value.deserializer", ByteArrayDeserializer.class.getName());
//        props.put("security.protocol","SASL_PLAINTEXT");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(topics);
        Set<TopicPartition> assignment;
        long startedTime = System.currentTimeMillis();
        while ((assignment = consumer.assignment()).isEmpty()) {
            consumer.poll(Duration.ofMillis(500));
            if(System.currentTimeMillis()-startedTime>(1000*30)){
                throw new Exception("Could not get kafka offset");
            }
        }
        Map<TopicPartition, Long> offsets = new HashMap<>();
        consumer.endOffsets(assignment).forEach((partition, offset) -> offsets.put(partition,offset));
        return offsets;
    }
}

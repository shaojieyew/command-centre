package app.c2.model;

import app.c2.properties.C2Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

  @GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "PROJECT_SEQ")
  @SequenceGenerator ( name = "PROJECT_SEQ" , sequenceName = "PROJECT_SEQ")
  @Id
  private long id;

  private String name;

  private String env;

  private String owner;

  private Timestamp updateTimestamp;

  private String fileIds;

  @PrePersist
  void preInsert() {
    if (this.updateTimestamp == null)
      this.updateTimestamp = new Timestamp(System.currentTimeMillis());
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getUpdateTimestamp() {
    return updateTimestamp;
  }

  public void setUpdateTimestamp(Timestamp update_timestamp) {
    this.updateTimestamp = update_timestamp;
  }
  
  public C2Properties getEnv() throws JsonProcessingException {
    if(this.env==null){
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.readValue(this.env, C2Properties.class);
  }

  public void setEnv(C2Properties env) throws JsonProcessingException {
    if(env!=null){
      this.env = new ObjectMapper().writeValueAsString(env);
    }
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Map<String, Set<Long>> getFileIds() throws JsonProcessingException {
    Map<String, Set<Long>>  emptyMap = new HashMap<String, Set<Long>>();
    emptyMap.put("default",new HashSet<>());
    if(this.fileIds==null){
      return emptyMap;
    }

    ObjectMapper mapper = new ObjectMapper();

    //org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
    //TypeFactory typeFactory = mapper.getTypeFactory();
    try {
      Map<String, Set<Long>> data = mapper.readValue(fileIds, new TypeReference<Map<String, Set<Long>>>(){});
      if(data.get("default")==null){
        data.put("default",new HashSet<>());
      }
      return data;
      // return mapper.readValue(this.fileIds,typeFactory.constructCollectionType(List.class, Long.class));
    } catch (IOException e) {
      return emptyMap;
    }
  }

  public Set<Long> getFileIds(String namespace) throws JsonProcessingException {
    Set<Long> result = getFileIds().get(namespace);
    if(result==null){
      result = new HashSet<>();
    }
    return result;
  }
  public void setFileIds(Map<String, Set<Long>> fileIds) throws JsonProcessingException {
    this.fileIds = new ObjectMapper().writeValueAsString(fileIds);
  }

}

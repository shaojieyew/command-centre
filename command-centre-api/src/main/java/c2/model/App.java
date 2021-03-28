package c2.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import javax.persistence.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "app")
public class App  {

  public App(String name, long projectId, String jarGroupId, String jarArtifactId, String jarVersion, String jarMainClass,List<String> jarArg, Map<String, String>  sparkArg) throws JsonProcessingException {
    setName(name);
    setProjectId(projectId);
    setJarGroupId(jarGroupId);
    setJarArtifactId(jarArtifactId);
    setJarVersion(jarVersion);
    setJarMainClass(jarMainClass);
    setJarArgs(jarArg);
    setSparkArgs(sparkArg);
  }

  public App() {

  }


  @Id
  private String id;

  private long projectId;

  private String name;

  private String fileIds;

  private Timestamp updatedTimestamp;

  private String jarGroupId;

  private String jarArtifactId;

  private String jarVersion;

  private String jarMainClass;

  private String jarArgs;

  private String sparkArgs;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getProjectId() {
    return projectId;
  }

  public void setProjectId(long projectId) {
    this.projectId = projectId;
  }

  public Timestamp getUpdatedTimestamp() {
    return updatedTimestamp;
  }

  public void setUpdatedTimestamp(Timestamp updated_timestamp) {
    this.updatedTimestamp = updated_timestamp;
  }

  public String getJarGroupId() {
    return jarGroupId;
  }

  public void setJarGroupId(String jarGroupId) {
    this.jarGroupId = jarGroupId;
  }

  public String getJarArtifactId() {
    return jarArtifactId;
  }

  public void setJarArtifactId(String jarArtifactId) {
    this.jarArtifactId = jarArtifactId;
  }

  public String getJarVersion() {
    return jarVersion;
  }

  public void setJarVersion(String jarVersion) {
    this.jarVersion = jarVersion;
  }

  public String getJarMainClass() {
    return jarMainClass;
  }

  public void setJarMainClass(String jarMainClass) {
    this.jarMainClass = jarMainClass;
  }

  public List<String> getJarArgs() throws IOException {
    if(this.jarArgs==null){
      return new ArrayList<String>();
    }
    org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
    TypeFactory typeFactory = mapper.getTypeFactory();
    List<String> jarArgs = mapper.readValue(this.jarArgs,typeFactory.constructCollectionType(List.class, String.class));
    return jarArgs;
  }

  public void setJarArgs(List<String> jarArg) throws JsonProcessingException {
    this.jarArgs = new ObjectMapper().writeValueAsString(jarArg);
  }

  public Map<String, String> getSparkArgs() throws JsonProcessingException {
    if(this.sparkArgs==null){
      return new HashMap<>();
    }
    TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<HashMap<String,String>>() {};
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, String> o = mapper.readValue(sparkArgs, typeRef);
    return o;
  }

  public void setSparkArgs(Map<String, String> sparkArg) throws JsonProcessingException {
    this.sparkArgs = new ObjectMapper().writeValueAsString(sparkArg);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public List<Long> getFileIds()  {
    if(this.fileIds==null){
      return new ArrayList<Long>();
    }
    org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
    TypeFactory typeFactory = mapper.getTypeFactory();
    try {
      return mapper.readValue(this.fileIds,typeFactory.constructCollectionType(List.class, Long.class));
    } catch (IOException e) {
      return new ArrayList<Long>();
    }
  }

  public void setFileIds(List<Long> fileIds) throws JsonProcessingException {
    this.fileIds = new ObjectMapper().writeValueAsString(fileIds);
  }

  @PreUpdate
  @PrePersist
  void preInsert() {
    if (this.updatedTimestamp == null)
      this.updatedTimestamp = new Timestamp(System.currentTimeMillis());
  }
}

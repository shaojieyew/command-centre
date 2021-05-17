package app.c2.model;

import app.c2.model.compositeField.AppId;
import app.c2.service.spark.model.SparkArgKeyValuePair;
import app.c2.service.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@IdClass(AppId.class)
@Table(name = "app")
public class App implements Serializable{
  @Id
  private long projectId;
  @Id
  private String name;

  private String fileIds;

  private Timestamp updatedTimestamp;

  private String jarGroupId;

  private String jarArtifactId;

  private String jarVersion;

  private String jarMainClass;

  private String jarArgs;

  private String sparkArgs;

  private String yarnStatus;

  private String yarnAppId;

  private String namespace;

  public String getNamespace() {
    if(namespace==null){
      return FileStorageService.DEFAULT_NAMESPACE;
    }
    return namespace;
  }

  public void setNamespace(String namespace) {
    if(namespace==null){
      this.namespace = FileStorageService.DEFAULT_NAMESPACE;
    }else{
      this.namespace = namespace;
    }
  }

  public String getYarnAppId() {
    return yarnAppId;
  }

  public void setYarnAppId(String yarnAppId) {
    this.yarnAppId = yarnAppId;
  }

  public String getYarnStatus() {
    return yarnStatus;
  }

  public void setYarnStatus(String yarnStatus) {
    this.yarnStatus = yarnStatus;
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


  public Set<SparkArgKeyValuePair> getSparkArgs() throws JsonProcessingException {
    if(this.sparkArgs==null){
      return new HashSet<>();
    }
    TypeReference<HashSet<SparkArgKeyValuePair>> typeRef
            = new TypeReference<HashSet<SparkArgKeyValuePair>>() {};
    ObjectMapper mapper = new ObjectMapper();
    HashSet<SparkArgKeyValuePair> o = mapper.readValue(sparkArgs, typeRef);
    return o;
  }

  public void setSparkArgs(Set<SparkArgKeyValuePair> sparkArg) throws JsonProcessingException {
    this.sparkArgs = new ObjectMapper().writeValueAsString(sparkArg);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public Set<Long> getFileIds()  {
    if(this.fileIds==null){
      return new HashSet<>();
    }
    org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
    TypeFactory typeFactory = mapper.getTypeFactory();
    try {
      return mapper.readValue(this.fileIds,typeFactory.constructCollectionType(Set.class, Long.class));
    } catch (IOException e) {
      return new HashSet<Long>();
    }
  }

  public void setFileIds(Set<Long> fileIds) throws JsonProcessingException {
    this.fileIds = new ObjectMapper().writeValueAsString(fileIds);
  }

  @PreUpdate
  @PrePersist
  void preInsert() {
    if (this.updatedTimestamp == null)
      this.updatedTimestamp = new Timestamp(System.currentTimeMillis());
  }
}

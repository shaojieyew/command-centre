package c2.model;

import c2.properties.C2Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import javax.persistence.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

  private String principle;

  private byte[] keytab;

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
    return new ObjectMapper().readValue(this.env, C2Properties.class);
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

  public String getPrinciple() {
    return principle;
  }

  public boolean isKeytabUploaded() {
    return keytab!=null;
  }

  public void setPrinciple(String principle) {
    this.principle = principle;
  }

  public void setKeytab(byte[] keytab) {
    this.keytab = keytab;
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

}

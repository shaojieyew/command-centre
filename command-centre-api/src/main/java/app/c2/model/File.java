package app.c2.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "files")
public class File {

  @GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "FILE_SEQ")
  @SequenceGenerator ( name = "FILE_SEQ" , sequenceName = "FILE_SEQ")
  @Id
  private long id;
  private String name;
  private String fileHash;
  private byte[] fileBlob;
  private String fileType;
  private long projectId;
//  private String url;
  private String source;
  private String sourceType;
  private long size;
  private Timestamp updateTimestamp;

  @PreUpdate
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

  public String getFileHash() {
    return fileHash;
  }

  public void setFileHash(String fileHash) {
    this.fileHash = fileHash;
  }

  public byte[] getFileBlob() {
    return fileBlob;
  }

  public void setFileBlob(byte[] fileBlob) {
    this.fileBlob = fileBlob;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getProjectId() {
    return projectId;
  }

  public void setProjectId(long projectId) {
    this.projectId = projectId;
  }

  public Timestamp getUpdateTimestamp() {
    return updateTimestamp;
  }

  public void setUpdateTimestamp(Timestamp update_timestamp) {
    this.updateTimestamp = update_timestamp;
  }

//  public String getUrl() {
//    return url;
//  }
//
//  public void setUrl(String url) {
//    this.url = url;
//  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }
}

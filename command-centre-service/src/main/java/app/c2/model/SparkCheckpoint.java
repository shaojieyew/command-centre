package app.c2.model;


import app.c2.model.compositeKey.NifiQueryId;
import app.c2.model.compositeKey.SparkCheckpointId;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@IdClass(SparkCheckpointId.class)
@Table(name = "spark_checkpoint")
public class SparkCheckpoint implements Serializable {
  @Id
  private long projectId;
  @Id
  private String path;

  private Timestamp updatedTimestamp;

  public long getProjectId() {
    return projectId;
  }

  public void setProjectId(long projectId) {
    this.projectId = projectId;
  }


  public Timestamp getUpdatedTimestamp() {
    return updatedTimestamp;
  }

  public void setUpdatedTimestamp(Timestamp updatedTimestamp) {
    this.updatedTimestamp = updatedTimestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @PreUpdate
  @PrePersist
  void preInsert() {
    if (this.updatedTimestamp == null)
      this.updatedTimestamp = new Timestamp(System.currentTimeMillis());
  }
}

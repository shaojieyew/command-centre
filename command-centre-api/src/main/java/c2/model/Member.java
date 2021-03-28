package c2.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "members")
public class Member {

  @GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "_SEQ")
  @SequenceGenerator ( name = "_SEQ" , sequenceName = "_SEQ")
  @Id
  private long id;

  private String userId;

  private long projectId;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public long getProjectId() {
    return projectId;
  }

  public void setProjectId(long projectId) {
    this.projectId = projectId;
  }
}

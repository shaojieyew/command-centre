package app.c2.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
public class User {

  @Id
  private String userId;

  private String email;

  private Timestamp updateTimestamp;

  @PreUpdate
  @PrePersist
  void preInsert() {
    if (this.updateTimestamp == null)
      this.updateTimestamp = new Timestamp(System.currentTimeMillis());
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Timestamp getUpdateTimestamp() {
    return updateTimestamp;
  }

  public void setUpdateTimestamp(Timestamp create_date) {
    this.updateTimestamp = create_date;
  }
}

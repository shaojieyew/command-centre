package app.c2.model.compositeField;

import java.io.Serializable;
import java.util.Objects;

public class SparkCheckpointId implements Serializable {
    public long projectId;
    public String path;

    public SparkCheckpointId(){}

    public SparkCheckpointId(long projectId, String path) {
        this.projectId = projectId;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SparkCheckpointId appId = (SparkCheckpointId) o;
        return projectId == appId.projectId &&
                Objects.equals(path, appId.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, path);
    }

    @Override
    public String toString() {
        return "AppId{" +
                "projectId=" + projectId +
                ", name='" + path + '\'' +
                '}';
    }
}
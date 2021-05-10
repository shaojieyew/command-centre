package app.c2.model.compositeKey;

import java.io.Serializable;
import java.util.Objects;

public class NifiQueryId implements Serializable {
    public long projectId;
    public String name;

    public NifiQueryId(){}

    public NifiQueryId(long projectId, String name) {
        this.projectId = projectId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NifiQueryId that = (NifiQueryId) o;
        return projectId == that.projectId &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, name);
    }

    @Override
    public String toString() {
        return "NifiQueryId{" +
                "projectId=" + projectId +
                ", name='" + name + '\'' +
                '}';
    }
}
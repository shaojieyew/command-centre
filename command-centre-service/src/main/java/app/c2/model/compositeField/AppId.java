package app.c2.model.compositeField;

import java.io.Serializable;
import java.util.Objects;

public class AppId implements Serializable {
    public long projectId;
    public String name;

    public AppId(){}

    public AppId(long projectId, String name) {
        this.projectId = projectId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppId appId = (AppId) o;
        return projectId == appId.projectId &&
                Objects.equals(name, appId.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, name);
    }

    @Override
    public String toString() {
        return "AppId{" +
                "projectId=" + projectId +
                ", name='" + name + '\'' +
                '}';
    }
}
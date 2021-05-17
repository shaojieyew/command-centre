package app.c2.service.nifi.model;

import java.util.Objects;

public class NifiComponent {
    String id;
    String name;
    String flowPath;
    String flowPathId;
    String type;
    String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlowPath() {
        return flowPath;
    }

    public void setFlowPath(String flowPath) {
        this.flowPath = flowPath;
    }

    public String getFlowPathId() {
        return flowPathId;
    }

    public void setFlowPathId(String flowPathId) {
        this.flowPathId = flowPathId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NifiComponent)) return false;
        NifiComponent that = (NifiComponent) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

package app.c2.services.nifi;


public class NifiComponent{
    String id;
    String name;
    String flowPath;
    String type;
    String status;

    public NifiComponent(String id, String name, String flowPath, String type) {
        this.id = id;
        this.name = name;
        this.flowPath = flowPath;
        this.type = type;
    }
    public NifiComponent(String id, String name, String flowPath, String type, String status) {
        this.id = id;
        this.name = name;
        this.flowPath = flowPath;
        this.type = type;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
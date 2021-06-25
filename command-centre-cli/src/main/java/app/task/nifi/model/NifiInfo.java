package app.task.nifi.model;

import app.c2.service.nifi.model.NifiComponent;

public class NifiInfo extends NifiComponent {
    private String query;
    private String queryName;
    private String id;
    private String name;
    private String flowPath;
    private String flowPathId;
    private String type;
    private String status;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFlowPath() {
        return flowPath;
    }

    @Override
    public void setFlowPath(String flowPath) {
        this.flowPath = flowPath;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    public NifiInfo(NifiComponent nifiComponent, String queryName, String query) {
        this.id = nifiComponent.getId() ;
        this.name = nifiComponent.getName();
        this.flowPath = nifiComponent.getFlowPath();
        this.type = nifiComponent.getType();
        this.status = nifiComponent.getStatus();
        this.query = query;
        this.queryName = queryName;
    }
}
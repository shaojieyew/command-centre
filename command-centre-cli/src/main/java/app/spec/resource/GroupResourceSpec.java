package app.spec.resource;


import app.spec.Spec;

import java.util.List;

public class GroupResourceSpec implements Spec {
    private String name;
    private List<Resource> resources;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "GroupResourceSpec{" +
                "name='" + name + '\'' +
                ", resources=" + resources +
                '}';
    }
}
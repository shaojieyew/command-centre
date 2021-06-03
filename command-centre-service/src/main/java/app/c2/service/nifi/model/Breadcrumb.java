package app.c2.service.nifi.model;

public class Breadcrumb {
    private String id;
    private String name;

    private Breadcrumb breadcrumb;
    private Breadcrumb parentBreadcrumb;

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

    public Breadcrumb getBreadcrumb() {
        return breadcrumb;
    }

    public void setBreadcrumb(Breadcrumb breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public Breadcrumb getParentBreadcrumb() {
        return parentBreadcrumb;
    }

    public void setParentBreadcrumb(Breadcrumb parentBreadcrumb) {
        this.parentBreadcrumb = parentBreadcrumb;
    }
}

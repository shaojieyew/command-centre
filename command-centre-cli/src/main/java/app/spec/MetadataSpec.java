package app.spec;

public  class MetadataSpec implements Spec {
    private  String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}

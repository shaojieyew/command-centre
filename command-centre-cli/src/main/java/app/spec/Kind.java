package app.spec;

import java.util.List;

public class Kind<T> {
    private String kind;
    private List<T> spec;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public  List<T> getSpec() {
        return (List<T>)spec;
    }

    public void setSpec( List<T> spec) {
        this.spec = spec;
    }
}

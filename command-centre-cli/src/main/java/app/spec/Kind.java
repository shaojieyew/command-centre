package app.spec;

import java.io.File;
import java.util.List;

public class Kind<T> {
    private String kind;
    private List<T> spec;
    private File fileOrigin;

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

    public File getFileOrigin() {
        return fileOrigin;
    }

    public void setFileOrigin(File fileOrigin) {
        this.fileOrigin = fileOrigin;
    }

    public void validate() throws SpecException {
        if(spec==null){
            throw new SpecException(fileOrigin.getAbsolutePath()+ " - Missing spec");
        }
    }
}

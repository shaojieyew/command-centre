package app.spec;

public class SpecException extends Exception {
    public SpecException(String missing_spec) {
        super(missing_spec);
    }
}

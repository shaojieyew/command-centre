package app.c2.model;

import java.io.Serializable;
import java.util.Objects;

public class KeyValuePair implements Serializable {
    public String name;
    public String value;

    public KeyValuePair() {
    }

    public KeyValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValuePair)) return false;
        KeyValuePair that = (KeyValuePair) o;
        return getName().equals(that.getName()) &&
                getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "KeyValuePair{" +
                "key='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
package io.esastack.restclient.ext.matcher;

public class KVMatcher {
    private String name;
    private StringMatcher value;

    public KVMatcher() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(StringMatcher value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public StringMatcher getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KVMatcher{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}

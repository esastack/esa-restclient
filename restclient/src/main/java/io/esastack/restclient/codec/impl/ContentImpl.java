package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.restclient.codec.Content;

public class ContentImpl<V> implements Content<V> {

    private V value;

    protected ContentImpl(V value) {
        Checks.checkNotNull(value, "value");
        this.value = value;
    }

    @Override
    public V value() {
        return value;
    }

}

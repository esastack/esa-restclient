package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.restclient.codec.Content;

public class ContentImpl implements Content {

    private Object content;

    protected ContentImpl(Object content) {
        Checks.checkNotNull(content, "content");
        this.content = content;
    }

    public Object content() {
        return content;
    }
}

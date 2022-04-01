package io.esastack.restclient.ext.config;

public interface ConfigItem {

    String type();

    <T> T getContent(Class<T> contentClass);
}

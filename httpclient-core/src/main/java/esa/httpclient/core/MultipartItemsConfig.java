package esa.httpclient.core;

import java.io.File;

public interface MultipartItemsConfig {

    MultipartItemsConfig attr(String name, String value);

    MultipartItemsConfig file(String name, File file);

    MultipartItemsConfig file(String name, File file, String contentType);

    MultipartItemsConfig file(String name, File file, String contentType, boolean isText);

    MultipartItemsConfig file(String name, String filename, File file, String contentType, boolean isText);

}

package esa.httpclient.core;

import java.io.File;

public interface MultipartConfig {
    MultipartConfig multipartEncode(boolean multipartEncode);

    MultipartConfig attr(String name, String value);

    MultipartConfig file(String name, File file);

    MultipartConfig file(String name, File file, String contentType);

    MultipartConfig file(String name, File file, String contentType, boolean isText);

    MultipartConfig file(String name, String filename, File file, String contentType, boolean isText);

}

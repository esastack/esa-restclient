package esa.httpclient.core;

import java.io.File;

public interface MultipartItemsConfigure {

    MultipartItemsConfigure attr(String name, String value);

    MultipartItemsConfigure file(String name, File file);

    MultipartItemsConfigure file(String name, File file, String contentType);

    MultipartItemsConfigure file(String name, File file, String contentType, boolean isText);

    MultipartItemsConfigure file(String name, String filename, File file, String contentType, boolean isText);

}

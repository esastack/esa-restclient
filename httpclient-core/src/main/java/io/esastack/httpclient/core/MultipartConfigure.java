package io.esastack.httpclient.core;

import java.io.File;
import java.util.Map;

public interface MultipartConfigure {

    MultipartConfigure attr(String name, String value);

    MultipartConfigure attrs(Map<String, String> attrMap);

    MultipartConfigure file(String name, File file);

    MultipartConfigure file(String name, File file, String contentType);

    MultipartConfigure file(String name, File file, String contentType, boolean isText);

    MultipartConfigure file(String name, String filename, File file, String contentType, boolean isText);

}

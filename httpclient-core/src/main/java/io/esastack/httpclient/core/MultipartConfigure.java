package io.esastack.httpclient.core;

import esa.commons.collection.MultiValueMap;

import java.io.File;
import java.util.Map;
import java.util.List;

public interface MultipartConfigure {

    MultipartConfigure attr(String name, String value);

    MultipartConfigure attrs(Map<String, String> attrMap);

    MultipartConfigure attrs(MultiValueMap<String, String> values);

    MultipartConfigure file(String name, File file);

    MultipartConfigure file(String name, File file, String contentType);

    MultipartConfigure file(String name, File file, String contentType, boolean isText);

    MultipartConfigure file(String name, String filename, File file, String contentType, boolean isText);

    MultipartConfigure files(List<MultipartFileItem> files);
}

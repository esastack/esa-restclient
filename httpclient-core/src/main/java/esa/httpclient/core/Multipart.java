package esa.httpclient.core;

import esa.commons.collection.MultiValueMap;

import java.util.List;

public interface Multipart {

    MultiValueMap<String, String> attrs();

    List<MultipartFileItem> files();

    boolean multipartEncode();
}

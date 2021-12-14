package io.esastack.httpclient.core;

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultipartBodyImpl implements MultipartBody {

    /**
     * Data which isn't always exists, so it's good to be instantiate lazily.
     */
    private MultiValueMap<String, String> attrs;
    private List<MultipartFileItem> files;
    private boolean useMultipartEncode = true;

    @Override
    public MultiValueMap<String, String> attrs() {
        return attrs;
    }

    @Override
    public List<MultipartFileItem> files() {
        return files;
    }

    @Override
    public MultipartBody multipartEncode(boolean multipartEncode) {
        this.useMultipartEncode = multipartEncode;
        return self();
    }

    @Override
    public MultipartBody attr(String name, String value) {
        checkAttrsNotNull();
        attrs.add(name, value);
        return self();
    }

    @Override
    public MultipartBody attrs(MultiValueMap<String, String> values) {
        if (values == null || values.isEmpty()) {
            return self();
        }
        checkAttrsNotNull();
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            attrs.addAll(entry.getKey(), entry.getValue());
        }
        return self();
    }

    @Override
    public MultipartConfigure attrs(Map<String, String> attrMap) {
        if (attrMap == null || attrMap.isEmpty()) {
            return self();
        }
        checkAttrsNotNull();
        for (Map.Entry<String, String> entry : attrMap.entrySet()) {
            attr(entry.getKey(), entry.getValue());
        }

        return self();
    }
    
    @Override
    public MultipartBody files(List<MultipartFileItem> files) {
        if (files == null || files.isEmpty()) {
            return self();
        }
        checkMultipartFile();
        checkFilesNotNull();
        this.files.addAll(files);
        return self();
    }

    @Override
    public MultipartBody file(String name, File file) {
        return files(Collections.singletonList(new MultipartFileItem(name, file)));
    }

    @Override
    public MultipartBody file(String name, File file, String contentType) {
        return files(Collections.singletonList(new MultipartFileItem(name, file, contentType)));
    }

    @Override
    public MultipartBody file(String name, File file, String contentType, boolean isText) {
        return files(Collections.singletonList(new MultipartFileItem(name, file, contentType, isText)));
    }

    @Override
    public MultipartBody file(String name, String filename, File file, String contentType, boolean isText) {
        return files(Collections.singletonList(new MultipartFileItem(name, filename, file, contentType, isText)));
    }

    @Override
    public boolean multipartEncode() {
        return useMultipartEncode;
    }

    private MultipartBody self() {
        return this;
    }

    private void checkAttrsNotNull() {
        if (attrs == null) {
            attrs = new HashMultiValueMap<>();
        }
    }

    private void checkMultipartFile() {
        if (!useMultipartEncode) {
            throw new IllegalStateException("File is not allowed to be added, maybe multipart is false?");
        }
    }

    private void checkFilesNotNull() {
        if (files == null) {
            files = new LinkedList<>();
        }
    }

    @Override
    public MultipartBody copy() {
        MultipartBody copied = new MultipartBodyImpl();
        if (attrs != null) {
            List<String> values;
            for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
                values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    for (String value : values) {
                        copied.attr(entry.getKey(), value);
                    }
                }
            }
        }

        if (files != null) {
            for (MultipartFileItem item : files) {
                copied.file(item.name(), item.fileName(), item.file(), item.contentType(), item.isText());
            }
        }

        copied.multipartEncode(useMultipartEncode);
        return copied;
    }
}

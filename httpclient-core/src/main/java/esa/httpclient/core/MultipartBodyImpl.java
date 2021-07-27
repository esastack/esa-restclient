package esa.httpclient.core;

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultipartBodyImpl implements MultipartBody {

    private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

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
    public MultipartConfig multipartEncode(boolean multipartEncode) {
        this.useMultipartEncode = multipartEncode;
        return self();
    }

    @Override
    public MultipartConfig attr(String name, String value) {
        checkAttrsNotNull();
        attrs.add(name, value);
        return self();
    }

    @Override
    public MultipartConfig file(String name, File file) {
        return file(name, file.getName(), file, DEFAULT_BINARY_CONTENT_TYPE, false);
    }

    @Override
    public MultipartConfig file(String name, File file, String contentType) {
        return file(name, file.getName(), file, contentType,
                DEFAULT_TEXT_CONTENT_TYPE.equalsIgnoreCase(contentType));
    }

    @Override
    public MultipartConfig file(String name, File file, String contentType, boolean isText) {
        return file(name, file.getName(), file, contentType, isText);
    }

    @Override
    public MultipartConfig file(String name, String filename, File file, String contentType, boolean isText) {
        checkMultipartFile();
        checkFilesNotNull();
        files.add(new MultipartFileItem(name, filename, file, contentType, isText));
        return self();
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

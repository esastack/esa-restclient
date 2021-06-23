package esa.restclient.core.request;

import esa.commons.http.Cookie;
import esa.restclient.core.MediaType;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultMultipartRequest extends DefaultExecutableRequest implements MultipartRequest {

    /**
     * Body data
     */
    private final List<MultipartItem> multipartItems = new LinkedList<>();

    DefaultMultipartRequest(ExecutableRequest executableRequest) {
        super(executableRequest);
    }

    @Override
    public MultipartRequest attr(String name, Object value) {
        if (illegalArgs(name, value)) {
            return self();
        }

        attr(name, value, MediaType.TEXT_PLAIN, null);
        return self();
    }

    @Override
    public MultipartRequest attr(String name, Object value, MediaType contentType) {
        if (illegalArgs(name, value)) {
            return self();
        }
        attr(name, value, contentType, null);
        return self();
    }

    @Override
    public MultipartRequest attr(String name, Object value, MediaType contentType, String contentTransferEncoding) {
        if (illegalArgs(name, value)) {
            return self();
        }
        multipartItems.add(new DefaultMultipartItem(
                ContentDisposition.multipartContentDisposition(name),
                contentType,
                contentTransferEncoding,
                value
        ));
        return self();
    }

    @Override
    public MultipartRequest file(String name, File file) {
        if (illegalArgs(name, file)) {
            return self();
        }
        return file(name, file, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public MultipartRequest file(String name, File file, MediaType contentType) {
        if (illegalArgs(name, file)) {
            return self();
        }
        return file(name, file.getName(), file, contentType);
    }

    @Override
    public MultipartRequest file(String name, String filename, File file, MediaType contentType) {
        if (illegalArgs(name, file)) {
            return self();
        }
        file(name, filename, file, contentType, null);
        return self();
    }

    @Override
    public MultipartRequest file(String name, String filename, File file, MediaType contentType, String contentTransferEncoding) {
        if (illegalArgs(name, file)) {
            return self();
        }
        multipartItems.add(new DefaultMultipartItem(
                ContentDisposition.multipartContentDisposition(name, filename),
                contentType,
                contentTransferEncoding,
                file
        ));
        return self();
    }

    @Override
    public List<MultipartItem> multipartItems() {
        return Collections.unmodifiableList(multipartItems);
    }


    @Override
    public MultipartRequest readTimeout(long readTimeout) {
        super.readTimeout(readTimeout);
        return self();
    }

    @Override
    public MultipartRequest maxRedirects(int maxRedirects) {
        super.maxRedirects(maxRedirects);
        return self();
    }

    @Override
    public MultipartRequest maxRetries(int maxRetries) {
        super.maxRetries(maxRetries);
        return self();
    }

    @Override
    public MultipartRequest disableExpectContinue() {
        super.disableExpectContinue();
        return self();
    }

    @Override
    public MultipartRequest enableUriEncode() {
        super.enableUriEncode();
        return self();
    }

    @Override
    public MultipartRequest addParams(Map<String, String> params) {
        super.addParams(params);
        return self();
    }

    @Override
    public MultipartRequest addParam(String name, String value) {
        super.addParam(name, value);
        return self();
    }

    @Override
    public MultipartRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers) {
        super.addHeaders(headers);
        return self();
    }

    @Override
    public MultipartRequest addHeader(CharSequence name, CharSequence value) {
        super.addHeader(name, value);
        return self();
    }

    @Override
    public MultipartRequest setHeader(CharSequence name, CharSequence value) {
        super.setHeader(name, value);
        return self();
    }

    @Override
    public MultipartRequest cookie(Cookie cookie) {
        super.cookie(cookie);
        return self();
    }

    @Override
    public MultipartRequest cookie(String name, String value) {
        super.cookie(name, value);
        return self();
    }

    @Override
    public MultipartRequest accept(MediaType... mediaTypes) {
        super.accept(mediaTypes);
        return self();
    }

    @Override
    public MultipartRequest contentType(MediaType mediaType) {
        super.contentType(mediaType);
        return self();
    }

    @Override
    public MultipartRequest property(String name, Object value) {
        super.property(name, value);
        return self();
    }
    
    private MultipartRequest self() {
        return this;
    }

    private static boolean illegalArgs(Object obj1, Object obj2) {
        return obj1 == null || obj2 == null;
    }
}

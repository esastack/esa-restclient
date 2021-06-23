package esa.restclient.core.request;

import esa.commons.http.Cookie;
import esa.restclient.core.MediaType;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface MultipartRequest extends ExecutableRequest {

    MultipartRequest attr(String name, Object value);

    MultipartRequest attr(String name, Object value, MediaType contentType);

    MultipartRequest attr(String name, Object value, MediaType contentType, String contentTransferEncoding);

    MultipartRequest file(String name, File file);

    MultipartRequest file(String name, File file, MediaType contentType);

    MultipartRequest file(String name, String filename, File file, MediaType contentType);

    MultipartRequest file(String name, String filename, File file, MediaType contentType, String contentTransferEncoding);

    List<MultipartItem> multipartItems();

    @Override
    MultipartRequest addParams(Map<String, String> params);

    @Override
    MultipartRequest addParam(String name, String value);

    @Override
    MultipartRequest cookie(Cookie cookie);

    @Override
    MultipartRequest cookie(String name, String value);

    @Override
    MultipartRequest contentType(MediaType mediaType);

    @Override
    MultipartRequest accept(MediaType... mediaTypes);


    @Override
    MultipartRequest addHeaders(Map<? extends CharSequence, ? extends CharSequence> headers);

    @Override
    MultipartRequest addHeader(CharSequence name, CharSequence value);

    @Override
    MultipartRequest setHeader(CharSequence name, CharSequence value);

    @Override
    MultipartRequest enableUriEncode();

    @Override
    MultipartRequest readTimeout(long readTimeout);

    @Override
    MultipartRequest disableExpectContinue();

    @Override
    MultipartRequest maxRedirects(int maxRedirects);

    @Override
    MultipartRequest maxRetries(int maxRetries);

    @Override
    MultipartRequest property(String name, Object value);

}

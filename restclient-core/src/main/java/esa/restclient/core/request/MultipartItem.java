package esa.restclient.core.request;

import esa.restclient.core.MediaType;

public interface MultipartItem {
    ContentDisposition contentDisposition();

    MediaType contentType();

    String contentTransferEncoding();

    Object attribute();
}

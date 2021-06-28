package esa.restclient.request;

import esa.restclient.MediaType;

public interface MultipartItem {
    ContentDisposition contentDisposition();

    MediaType contentType();

    String contentTransferEncoding();

    Object attribute();
}

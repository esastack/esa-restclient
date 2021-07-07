package esa.restclient;

public interface MultipartItem {

    MediaType contentType();

    String contentTransferEncoding();

    Object attribute();

}

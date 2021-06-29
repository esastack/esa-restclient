package esa.restclient;

public interface MultipartItem {

    ContentDisposition contentDisposition();

    MediaType contentType();

    String contentTransferEncoding();

    Object attribute();

}

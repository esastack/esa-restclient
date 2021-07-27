package esa.httpclient.core;

public interface MultipartBody extends Multipart, MultipartItemsConfig, Reusable<MultipartBody> {
    MultipartBody multipartEncode(boolean multipartEncode);
}

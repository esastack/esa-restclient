package esa.httpclient.core;

public interface MultipartBody extends Multipart, MultipartItemsConfigure, Reusable<MultipartBody> {
    MultipartBody multipartEncode(boolean multipartEncode);
}

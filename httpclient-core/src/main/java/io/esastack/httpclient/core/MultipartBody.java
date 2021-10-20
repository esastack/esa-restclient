package io.esastack.httpclient.core;

public interface MultipartBody extends Multipart, MultipartConfigure, Reusable<MultipartBody> {
    MultipartBody multipartEncode(boolean multipartEncode);
}

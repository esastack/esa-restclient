package esa.restclient.request;

import esa.commons.Checks;
import esa.restclient.MediaType;

public class DefaultMultipartItem implements MultipartItem {

    private final ContentDisposition contentDisposition;
    private final MediaType contentType;
    private final String contentTransferEncoding;
    private final Object attribute;

    DefaultMultipartItem(ContentDisposition contentDisposition, MediaType contentType, String contentTransferEncoding, Object attribute) {
        Checks.checkNotNull(contentDisposition, "ContentDisposition must be not null!");
        this.contentDisposition = contentDisposition;
        this.contentType = contentType;
        this.contentTransferEncoding = contentTransferEncoding;
        this.attribute = attribute;
    }

    @Override
    public ContentDisposition contentDisposition() {
        return contentDisposition;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public String contentTransferEncoding() {
        return contentTransferEncoding;
    }

    @Override
    public Object attribute() {
        return attribute;
    }

}

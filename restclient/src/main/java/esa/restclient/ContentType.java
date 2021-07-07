package esa.restclient;

import esa.restclient.serializer.TxSerializer;

public class ContentType {

    private final MediaType mediaType;
    private final TxSerializer txSerializer;

    public ContentType(MediaType mediaType, TxSerializer txSerializer) {
        this.mediaType = mediaType;
        this.txSerializer = txSerializer;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public TxSerializer getTxSerializer() {
        return txSerializer;
    }
}

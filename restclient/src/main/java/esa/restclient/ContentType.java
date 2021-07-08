package esa.restclient;

import esa.commons.Checks;
import esa.restclient.serializer.Serializer;
import esa.restclient.serializer.TxSerializer;

public class ContentType {

    private final MediaType mediaType;
    private final TxSerializer txSerializer;

    public ContentType(MediaType mediaType, TxSerializer txSerializer) {
        Checks.checkNotNull(mediaType, "MediaType must not be null");
        this.mediaType = mediaType;
        this.txSerializer = txSerializer;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public TxSerializer getTxSerializer() {
        return txSerializer;
    }

    public static ContentType of(MediaType mediaType, TxSerializer txSerializer) {
        return new ContentType(mediaType, txSerializer);
    }

}

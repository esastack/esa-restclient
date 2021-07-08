package esa.restclient;

import esa.restclient.serializer.RxSerializer;

public class AcceptType {
    private final MediaType mediaType;
    private final RxSerializer rxSerializer;

    public AcceptType(MediaType mediaType, RxSerializer rxSerializer) {
        this.mediaType = mediaType;
        this.rxSerializer = rxSerializer;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public RxSerializer getRxSerializer() {
        return rxSerializer;
    }

    public static AcceptType of(MediaType mediaType, RxSerializer rxSerializer) {
        return new AcceptType(mediaType, rxSerializer);
    }
}

package io.esastack.restclient;

/**
 * Used to support the transfer of data received by httpClient to restClient
 *
 * @param <T> the class of content
 */
public final class ResponseBodyContent<T> implements BodyContent<T> {

    private final T content;

    private ResponseBodyContent(T content) {
        this.content = content;
    }

    @Override
    public T content() {
        return content;
    }

    public static ResponseBodyContent<byte[]> of(byte[] content) {
        return new ResponseBodyContent<>(content);
    }
}

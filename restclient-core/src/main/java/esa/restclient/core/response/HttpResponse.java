package esa.restclient.core.response;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.restclient.core.HttpMessage;
import esa.restclient.core.MediaType;

import java.io.InputStream;

public interface HttpResponse extends HttpMessage {
    int status();

    InputStream bodyStream();

    HttpHeaders trailers();

    MediaType contentType();
}

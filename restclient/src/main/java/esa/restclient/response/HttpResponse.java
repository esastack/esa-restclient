package esa.restclient.response;

import esa.commons.http.HttpHeaders;
import esa.restclient.HttpMessage;
import esa.restclient.MediaType;

import java.io.InputStream;

public interface HttpResponse extends HttpMessage {
    int status();

    InputStream bodyStream();

    HttpHeaders trailers();

    MediaType contentType();
}

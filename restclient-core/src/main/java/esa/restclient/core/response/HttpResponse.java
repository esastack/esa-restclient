package esa.restclient.core.response;

import esa.commons.http.HttpHeaders;
import esa.commons.netty.core.Buffer;
import esa.restclient.core.HttpMessage;

public interface HttpResponse extends HttpMessage {
    int status();

    Buffer body();

    HttpHeaders trailers();
}

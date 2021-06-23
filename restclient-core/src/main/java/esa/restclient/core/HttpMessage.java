package esa.restclient.core;

import esa.commons.http.HttpHeaders;
import esa.commons.http.HttpVersion;

public interface HttpMessage {

    HttpVersion version();

    HttpHeaders headers();
}

package esa.restclient;

import esa.commons.http.HttpHeaders;

public interface AcceptTypeResolver {
    AcceptType resolve(HttpHeaders responseHeaders);
}

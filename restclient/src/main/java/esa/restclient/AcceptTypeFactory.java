package esa.restclient;

import esa.commons.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

public interface AcceptTypeFactory {
    Optional<List<AcceptType>> create(HttpHeaders requestHeaders, RequestContext context);
}

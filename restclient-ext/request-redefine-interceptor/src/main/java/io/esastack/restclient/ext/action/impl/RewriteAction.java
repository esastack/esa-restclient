package io.esastack.restclient.ext.action.impl;

import io.esastack.restclient.RestResponse;
import io.esastack.restclient.ext.RedefineContext;
import io.esastack.restclient.ext.action.RequestRedefineAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionStage;

public class RewriteAction implements RequestRedefineAction {

    private final String authority;
    private final String path;

    public RewriteAction(String authority, String path) {
        if (authority == null && path == null) {
            throw new IllegalArgumentException("Both authority and path are null!");
        }

        this.authority = authority;
        this.path = path;
    }

    @Override
    public CompletionStage<RestResponse> doAction(RedefineContext context) {
        URI origin = context.request().uri().netURI();
        try {
            context.request().uri().uri(new URI(
                    origin.getScheme(),
                    authority == null ? origin.getRawAuthority() : authority,
                    path == null ? origin.getRawPath() : path,
                    origin.getRawQuery(),
                    origin.getRawFragment()
            ));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return context.next();
    }
}

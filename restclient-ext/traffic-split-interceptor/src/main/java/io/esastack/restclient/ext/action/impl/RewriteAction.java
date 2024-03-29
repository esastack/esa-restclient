/*
 * Copyright 2022 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient.ext.action.impl;

import io.esastack.httpclient.core.util.LoggerUtils;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.ext.TrafficSplitContext;
import io.esastack.restclient.ext.action.TrafficSplitAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionStage;

public class RewriteAction implements TrafficSplitAction {

    private final String authority;
    private final String path;

    public RewriteAction(RewriteActionConfig config) {
        authority = config.getUriAuthority();
        path = config.getPath();
        if (authority == null && path == null) {
            throw new IllegalArgumentException("Both authority and path are null!");
        }
    }

    @Override
    public CompletionStage<RestResponse> doAction(TrafficSplitContext context) {
        URI origin = context.request().uri().netURI();
        try {
            context.request().uri().uri(new URI(
                    origin.getScheme(),
                    authority == null ? origin.getRawAuthority() : authority,
                    path == null ? origin.getRawPath() : path,
                    origin.getRawQuery(),
                    origin.getRawFragment()
            ));
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger()
                        .debug("Do rewrite action in request redefine rule!" +
                                (authority != null
                                        ? "rewrite authority from "
                                        + origin.getRawAuthority()
                                        + " to " + authority + "." : "") +
                                (path != null
                                        ? "rewrite path from "
                                        + origin.getRawPath()
                                        + " to " + path + "." : ""));
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return context.next();
    }
}

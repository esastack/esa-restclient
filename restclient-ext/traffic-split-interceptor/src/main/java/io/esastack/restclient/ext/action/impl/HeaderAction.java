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
import io.esastack.restclient.ext.RedefineContext;
import io.esastack.restclient.ext.action.RequestRedefineAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class HeaderAction implements RequestRedefineAction {

    private final Map<String, String> headersToBeAdd;
    private final List<String> headersToBeRemove;

    public HeaderAction(HeaderActionConfig config) {
        headersToBeAdd = config.getAdd();
        headersToBeRemove = config.getRemove();
    }

    @Override
    public CompletionStage<RestResponse> doAction(RedefineContext context) {
        if (headersToBeAdd != null) {
            context.request().addHeaders(headersToBeAdd);
        }
        if (headersToBeRemove != null) {
            headersToBeRemove.forEach(name -> context.request().removeHeader(name));
        }
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger()
                    .debug("Do action of header in request redefine rule!" +
                                    "Add headers({}) and remove headers({}).",
                            headersToBeAdd,
                            headersToBeRemove);
        }
        return context.next();
    }
}

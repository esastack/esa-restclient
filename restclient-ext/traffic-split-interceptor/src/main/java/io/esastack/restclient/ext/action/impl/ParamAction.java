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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class ParamAction implements TrafficSplitAction {

    private final Map<String, String> paramsToBeAdd;
    private final Map<String, String> paramsToBeSet;
    private final List<String> paramsToBeRemove;

    public ParamAction(ParamActionConfig config) {
        paramsToBeAdd = config.getAdd();
        paramsToBeRemove = config.getRemove();
        paramsToBeSet = config.getSet();
    }

    @Override
    public CompletionStage<RestResponse> doAction(TrafficSplitContext context) {
        if (paramsToBeRemove != null) {
            paramsToBeRemove.forEach(name -> context.request().uri().params().remove(name));
        }
        if (paramsToBeSet != null) {
            paramsToBeSet.forEach((name, value) ->
                    context.request().uri().params().putSingle(name, value));
        }
        if (paramsToBeAdd != null) {
            context.request().addParams(paramsToBeAdd);
        }
        if (LoggerUtils.logger().isDebugEnabled()) {
            LoggerUtils.logger()
                    .debug("Do action of param in request redefine rule!" +
                                    "Add params({})、set params({}) and remove params({}).",
                            paramsToBeAdd,
                            paramsToBeSet,
                            paramsToBeRemove);
        }
        return context.next();
    }
}

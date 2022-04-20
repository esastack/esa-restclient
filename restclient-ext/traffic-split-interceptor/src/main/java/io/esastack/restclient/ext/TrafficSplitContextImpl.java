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
package io.esastack.restclient.ext;

import esa.commons.Checks;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.ext.action.TrafficSplitAction;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class TrafficSplitContextImpl implements TrafficSplitContext {

    private final RestRequest request;
    private final List<TrafficSplitAction> actions;
    private final int actionsSize;
    private final InvocationChain invocationChain;
    private int actionsIndex;
    private boolean hadProceed = false;

    public TrafficSplitContextImpl(RestRequest request,
                                   List<TrafficSplitAction> actions,
                                   InvocationChain invocationChain) {
        Checks.checkNotNull(request, "request");
        Checks.checkNotNull(actions, "actions");
        Checks.checkNotNull(invocationChain, "invocationChain");
        this.request = request;
        this.actions = actions;
        this.actionsSize = actions.size();
        this.invocationChain = invocationChain;
    }

    @Override
    public RestRequest request() {
        return request;
    }

    @Override
    public CompletionStage<RestResponse> next() {
        if (hadProceed) {
            throw new IllegalStateException("The context had end!Please don,t call next() repeat in the one action!");
        }

        if (actionsIndex == actionsSize) {
            this.hadProceed = true;
            return invocationChain.proceed(request);
        }

        return actions.get(actionsIndex++).doAction(this);
    }
}

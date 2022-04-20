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
package io.esastack.restclient.ext.rule;

import io.esastack.restclient.ext.action.TrafficSplitAction;
import io.esastack.restclient.ext.action.impl.HeaderAction;
import io.esastack.restclient.ext.action.impl.HeaderActionConfig;
import io.esastack.restclient.ext.action.impl.ParamAction;
import io.esastack.restclient.ext.action.impl.ParamActionConfig;
import io.esastack.restclient.ext.action.impl.RewriteAction;
import io.esastack.restclient.ext.action.impl.RewriteActionConfig;

import java.util.ArrayList;
import java.util.List;

public class ActionsConfig {
    private RewriteActionConfig rewrite;
    private HeaderActionConfig headers;
    private ParamActionConfig params;

    public void setRewrite(RewriteActionConfig rewrite) {
        this.rewrite = rewrite;
    }

    public void setHeaders(HeaderActionConfig headers) {
        this.headers = headers;
    }

    public void setParams(ParamActionConfig params) {
        this.params = params;
    }

    public List<TrafficSplitAction> build() {
        List<TrafficSplitAction> actions = new ArrayList<>();
        if (rewrite != null) {
            actions.add(new RewriteAction(rewrite));
        }
        if (params != null) {
            actions.add(new ParamAction(params));
        }
        if (headers != null) {
            actions.add(new HeaderAction(headers));
        }
        return actions;
    }

    @Override
    public String toString() {
        return "ActionsConfig{" +
                "rewrite=" + rewrite +
                ", headers=" + headers +
                ", params=" + params +
                '}';
    }
}

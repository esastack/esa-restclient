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

import io.esastack.restclient.ext.action.RequestRedefineAction;
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
    private HeaderActionConfig header;
    private ParamActionConfig param;

    public void setRewrite(RewriteActionConfig rewrite) {
        this.rewrite = rewrite;
    }

    public void setHeader(HeaderActionConfig header) {
        this.header = header;
    }

    public void setParam(ParamActionConfig param) {
        this.param = param;
    }

    public List<RequestRedefineAction> build() {
        List<RequestRedefineAction> actions = new ArrayList<>();
        if (rewrite != null) {
            actions.add(new RewriteAction(rewrite));
        }
        if (param != null) {
            actions.add(new ParamAction(param));
        }
        if (header != null) {
            actions.add(new HeaderAction(header));
        }
        return actions;
    }

    @Override
    public String toString() {
        return "ActionsConfig{" +
                "rewrite=" + rewrite +
                ", header=" + header +
                ", param=" + param +
                '}';
    }
}

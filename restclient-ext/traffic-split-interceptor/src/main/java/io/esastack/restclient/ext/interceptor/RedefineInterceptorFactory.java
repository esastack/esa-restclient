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
package io.esastack.restclient.ext.interceptor;

import esa.commons.Checks;
import esa.commons.spi.SpiLoader;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.RestRequest;
import io.esastack.restclient.RestResponse;
import io.esastack.restclient.exec.InvocationChain;
import io.esastack.restclient.exec.RestInterceptor;
import io.esastack.restclient.ext.RedefineContextImpl;
import io.esastack.restclient.ext.rule.RedefineRule;
import io.esastack.restclient.ext.rule.RedefineRuleSource;
import io.esastack.restclient.ext.spi.RuleSourceFactory;
import io.esastack.restclient.spi.RestInterceptorFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class RedefineInterceptorFactory implements RestInterceptorFactory {
    @Override
    public Collection<RestInterceptor> interceptors(RestClientOptions options) {
        return Collections.singletonList(new RedefineInterceptor(options));
    }

    private static final class RedefineInterceptor implements RestInterceptor {

        private static final int ORDER = 0;
        private final RedefineRuleSource rulesSource;

        private RedefineInterceptor(RestClientOptions options) {
            List<RuleSourceFactory> ruleSourceFactories = SpiLoader
                    .cached(RuleSourceFactory.class)
                    .getByGroup(options.name(), true);
            int ruleSourceFactoriesSize = ruleSourceFactories.size();
            if (ruleSourceFactoriesSize == 1) {
                this.rulesSource = Checks.checkNotNull(ruleSourceFactories.get(0).create(options),
                        "rulesSource");
            } else {
                throw new IllegalStateException("Unexpected size of ruleSourceFactories : " + ruleSourceFactoriesSize
                        + ", expected size is 1. RuleSourceFactories: " + ruleSourceFactories);
            }
        }

        @Override
        public CompletionStage<RestResponse> proceed(RestRequest request, InvocationChain next) {
            List<RedefineRule> rules = rulesSource.rules();
            for (RedefineRule rule : rules) {
                if (rule.match(request)) {
                    return new RedefineContextImpl(request, rule.actions(), next).next();
                }
            }
            return next.proceed(request);
        }

        @Override
        public int getOrder() {
            return ORDER;
        }
    }

}

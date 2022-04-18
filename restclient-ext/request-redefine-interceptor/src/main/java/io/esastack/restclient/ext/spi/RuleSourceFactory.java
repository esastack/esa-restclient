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
package io.esastack.restclient.ext.spi;

import esa.commons.spi.SPI;
import io.esastack.restclient.RestClientOptions;
import io.esastack.restclient.ext.rule.RedefineRuleSource;

@SPI
public interface RuleSourceFactory {

    /**
     * Create RedefineRuleSource by RestClientOptions. Different RedefineRuleSource can be
     * provided according to different RestClientOptions to provide users with more flexible
     * configuration.
     *
     * @param options RestClientOptions
     * @return RedefineRuleSource
     */
    RedefineRuleSource create(RestClientOptions options);
}

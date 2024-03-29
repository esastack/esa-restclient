/*
 * Copyright 2020 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.httpclient.core.spi;

import esa.commons.spi.SpiLoader;
import io.esastack.httpclient.core.exec.Interceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class InterceptorFactoryImpl implements InterceptorFactory {

    @Override
    public Collection<Interceptor> interceptors() {
        List<Interceptor> interceptors = SpiLoader.getAll(Interceptor.class);
        return interceptors == null ? Collections.emptyList() : Collections.unmodifiableList(interceptors);
    }

}

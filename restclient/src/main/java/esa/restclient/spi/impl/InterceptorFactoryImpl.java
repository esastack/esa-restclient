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
package esa.restclient.spi.impl;

import esa.commons.spi.SpiLoader;
import esa.restclient.exec.ClientInterceptor;
import esa.restclient.spi.InterceptorFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class InterceptorFactoryImpl implements InterceptorFactory {

    @Override
    public Collection<ClientInterceptor> interceptors() {
        List<ClientInterceptor> interceptors = SpiLoader.getAll(ClientInterceptor.class);
        return interceptors == null
                ? Collections.emptyList() : Collections.unmodifiableList(interceptors);
    }

}

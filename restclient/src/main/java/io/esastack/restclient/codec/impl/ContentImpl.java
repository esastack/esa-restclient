/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.restclient.codec.impl;

import esa.commons.Checks;
import io.esastack.restclient.codec.Content;

public class ContentImpl<V> implements Content<V> {

    private final V value;

    protected ContentImpl(V value) {
        Checks.checkNotNull(value, "value");
        this.value = value;
    }

    @Override
    public V value() {
        return value;
    }

}
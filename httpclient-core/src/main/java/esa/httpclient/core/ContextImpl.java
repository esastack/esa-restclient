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
package esa.httpclient.core;

import esa.commons.Checks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ContextImpl implements Context {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>(0);

    @Override
    public Object getAttr(String name) {
        Checks.checkNotNull(name, "name must not be null");
        return attributes.get(name);
    }

    @Override
    public Object setAttr(String name, Object value) {
        Checks.checkNotNull(name, "name must not be null");
        Checks.checkNotNull(value, "value must not be null");
        return attributes.put(name, value);
    }

    @Override
    public Object removeAttr(String name) {
        Checks.checkNotNull(name, "name must not be null");
        return attributes.remove(name);
    }

    @Override
    public Set<String> attrNames() {
        return attributes.keySet();
    }

    /**
     * Clears current context.
     * Please don't call this manually, otherwise something wrong may occur.
     */
    public void clear() {
        attributes.clear();
    }
}

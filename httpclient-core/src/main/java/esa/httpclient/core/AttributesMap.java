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
package esa.httpclient.core;

import esa.commons.Checks;
import esa.commons.annotation.Internal;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Internal
public class AttributesMap {

    protected final Map<String, Object> attributes = new ConcurrentHashMap<>(4);

    public Object getAttr(String name) {
        Checks.checkNotNull(name, "name must not be null");
        return attributes.get(name);
    }

    public Object setAttr(String name, Object value) {
        Checks.checkNotNull(name, "name must not be null");
        Checks.checkNotNull(value, "value must not be null");
        return attributes.put(name, value);
    }

    public Object removeAttr(String name) {
        Checks.checkNotNull(name, "name must not be null");
        return attributes.remove(name);
    }

    public Set<String> attrNames() {
        return attributes.keySet();
    }

    /**
     * Obtains generic type value by name
     *
     * @param name name
     * @param <T>  generic type
     * @return value
     */
    @SuppressWarnings("unchecked")
    public <T> T getUncheckedAttr(String name) {
        return (T) getAttr(name);
    }

    /**
     * Obtains generic type value by name with default value
     *
     * @param name         name
     * @param defaultValue default value
     * @param <T>          generic type
     * @return value
     */
    public <T> T getUncheckedAttr(String name, T defaultValue) {
        try {
            final T value = getUncheckedAttr(name);
            return value != null ? value : defaultValue;
        } catch (Throwable th) {
            return defaultValue;
        }
    }

    /**
     * Removes the attribute value by name
     *
     * @param name name
     * @param <T>  generic type
     * @return value
     */
    @SuppressWarnings("unchecked")
    public <T> T removeUncheckedAttr(String name) {
        return (T) removeAttr(name);
    }

}


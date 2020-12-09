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

import java.util.Set;

public interface Context {

    /**
     * Obtains attribute's names.
     *
     * @return  attribute's names.
     */
    Set<String> attrNames();

    /**
     * Obtains attribute value by name
     *
     * @param name name
     * @return attribute values
     */
    Object getAttr(String name);

    /**
     * Obtains generic type value by name
     *
     * @param name name
     * @param <T>  generic type
     * @return value
     */
    @SuppressWarnings("unchecked")
    default <T> T getUncheckedAttr(String name) {
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
    default <T> T getUncheckedAttr(String name, T defaultValue) {
        try {
            final T value = getUncheckedAttr(name);
            return value != null ? value : defaultValue;
        } catch (Throwable th) {
            return defaultValue;
        }
    }

    /**
     * Adds the attribute to the context
     *
     * @param name  name
     * @param value value
     * @return value
     */
    Object setAttr(String name, Object value);

    /**
     * Removes the attribute value by name
     *
     * @param name name
     * @return pre value, may be null if doesn't exist
     */
    Object removeAttr(String name);

    /**
     * Removes the attribute value by name
     *
     * @param name name
     * @param <T>  generic type
     * @return value
     */
    @SuppressWarnings("unchecked")
    default <T> T removeUncheckedAttr(String name) {
        return (T) removeAttr(name);
    }

}

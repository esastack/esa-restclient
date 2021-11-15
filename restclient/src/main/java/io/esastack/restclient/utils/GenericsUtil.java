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
package io.esastack.restclient.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class GenericsUtil {

    private GenericsUtil() {
    }

    public static void checkTypeCompatibility(final Class<?> type, final Type generics) {
        if (generics instanceof Class) {
            Class<?> classType = (Class<?>) generics;
            if (classType.isAssignableFrom(type)) {
                return;
            }
        } else if (generics instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) generics;
            Type rawType = parameterizedType.getRawType();
            checkTypeCompatibility(type, rawType);
            return;
        } else if (type.isArray() && (generics instanceof GenericArrayType)) {
            GenericArrayType at = (GenericArrayType) generics;
            Type rawType = at.getGenericComponentType();
            checkTypeCompatibility(type.getComponentType(), rawType);
            return;
        }
        throw new IllegalArgumentException(
                "The generics(" + generics + ") is incompatible with the type(" + type + ").");
    }
}

package io.esastack.restclient.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericTypeUtil {

    private GenericTypeUtil() {
    }

    public static void checkTypeCompatibility(final Class<?> type, final Type genericType) {
        if (genericType instanceof Class) {
            Class<?> classType = (Class<?>) genericType;
            if (classType.isAssignableFrom(type)) {
                return;
            }
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type rawType = parameterizedType.getRawType();
            checkTypeCompatibility(type, rawType);
            return;
        } else if (type.isArray() && (genericType instanceof GenericArrayType)) {
            GenericArrayType at = (GenericArrayType) genericType;
            Type rawType = at.getGenericComponentType();
            checkTypeCompatibility(type.getComponentType(), rawType);
            return;
        }
        throw new IllegalArgumentException("The genericType is incompatible with the type.");
    }
}

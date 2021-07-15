package esa.restclient.serializer;

import esa.commons.Checks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericEntity<T> {
    private final Type type;
    private final T entity;

    /**
     * Constructs a new generic entity. Derives represented class from type
     * parameter. Note that this constructor is protected, users should create
     * a (usually anonymous) subclass as shown above.
     *
     * @param entity the entity instance, must not be {@code null}.
     * @throws IllegalArgumentException if entity is {@code null}.
     */
    protected GenericEntity(final T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The entity must not be null");
        }
        this.entity = entity;
        this.type = getGenericType();
    }

    private Type getGenericType() {
        Type superclass = getClass().getGenericSuperclass();
        Checks.checkArg(superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public final Type getType() {
        return type;
    }

    public final T getEntity() {
        return entity;
    }
}

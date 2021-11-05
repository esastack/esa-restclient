package io.esastack.restclient.codec;

import esa.commons.Checks;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Stack;

public class GenericObject<T> {

    private final Class<?> rawType;
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
    protected GenericObject(final T entity) {
        Checks.checkNotNull(entity, "entity");
        this.entity = entity;
        this.type = getTypeArgument(getClass(), GenericObject.class);
        this.rawType = entity.getClass();
    }

    /**
     * Create a new instance of GenericEntity, supplying the generic type information.
     * The entity must be assignable to a variable of the
     * supplied generic type, e.g. if {@code entity} is an instance of
     * {@code ArrayList<String>} then {@code genericType} could
     * be the same or a superclass of {@code ArrayList} with the same generic
     * type like {@code List<String>}.
     *
     * @param entity      the entity instance, must not be {@code null}.
     * @param genericType the generic type, must not be {@code null}.
     * @throws IllegalArgumentException if the entity is not assignable to
     *                                  a variable of the supplied generic type or if entity or genericType
     *                                  is null.
     */
    public GenericObject(final T entity, final Type genericType) {
        Checks.checkNotNull(entity, "entity");
        Checks.checkNotNull(genericType, "genericType");
        this.entity = entity;
        this.rawType = entity.getClass();
        checkTypeCompatibility(this.rawType, genericType);
        this.type = genericType;
    }

    private void checkTypeCompatibility(final Class<?> c, final Type t) {
        if (t instanceof Class) {
            Class<?> ct = (Class<?>) t;
            if (ct.isAssignableFrom(c)) {
                return;
            }
        } else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type rt = pt.getRawType();
            checkTypeCompatibility(c, rt);
            return;
        } else if (c.isArray() && (t instanceof GenericArrayType)) {
            GenericArrayType at = (GenericArrayType) t;
            Type rt = at.getGenericComponentType();
            checkTypeCompatibility(c.getComponentType(), rt);
            return;
        }
        throw new IllegalArgumentException("The type is incompatible with the class of the entity.");
    }

    /**
     * Gets the raw type of the enclosed entity. Note that this is the raw type of
     * the instance, not the raw type of the type parameter. I.e. in the example
     * in the introduction, the raw type is {@code ArrayList} not {@code List}.
     *
     * @return the raw type.
     */
    public final Class<?> getRawType() {
        return rawType;
    }

    /**
     * Gets underlying {@code Type} instance. Note that this is derived from the
     * type parameter, not the enclosed instance. I.e. in the example
     * in the introduction, the type is {@code List<String>} not
     * {@code ArrayList<String>}.
     *
     * @return the type
     */
    public final Type getType() {
        return type;
    }

    /**
     * Get the enclosed entity.
     *
     * @return the enclosed entity.
     */
    public final T getEntity() {
        return entity;
    }

    /**
     * Return the value of the type parameter of {@code GenericType<T>}.
     *
     * @param clazz     subClass of {@code baseClass} to analyze.
     * @param baseClass base class having the type parameter the value of which we need to retrieve
     * @return the parameterized type of {@code GenericType<T>} (aka T)
     */
    private Type getTypeArgument(Class<?> clazz, Class<?> baseClass) {
        // collect superclasses
        Stack<Type> superclasses = new Stack<Type>();
        Type currentType;
        Class<?> currentClass = clazz;
        do {
            currentType = currentClass.getGenericSuperclass();
            superclasses.push(currentType);
            if (currentType instanceof Class) {
                currentClass = (Class) currentType;
            } else if (currentType instanceof ParameterizedType) {
                currentClass = (Class) ((ParameterizedType) currentType).getRawType();
            }
        } while (!currentClass.equals(baseClass));

        // find which one supplies type argument and return it
        TypeVariable tv = baseClass.getTypeParameters()[0];
        while (!superclasses.isEmpty()) {
            currentType = superclasses.pop();

            if (currentType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) currentType;
                Class<?> rawType = (Class) pt.getRawType();
                int argIndex = Arrays.asList(rawType.getTypeParameters()).indexOf(tv);
                if (argIndex > -1) {
                    Type typeArg = pt.getActualTypeArguments()[argIndex];
                    if (typeArg instanceof TypeVariable) {
                        // type argument is another type variable - look for the value of that
                        // variable in subclasses
                        tv = (TypeVariable) typeArg;
                        continue;
                    } else {
                        // found the value - return it
                        return typeArg;
                    }
                }
            }

            // needed type argument not supplied - break and throw exception
            break;
        }
        throw new IllegalArgumentException(currentType + " does not specify the type parameter T of GenericType<T>");
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = this == obj;
        if (!result && obj instanceof GenericObject) {
            // Compare inner type for equality
            GenericObject<?> that = (GenericObject<?>) obj;
            return this.type.equals(that.type) && this.entity.equals(that.entity);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return entity.hashCode() + type.hashCode() * 37 + 5;
    }

    @Override
    public String toString() {
        return "GenericObject{" + entity.toString() + ", " + type.toString() + "}";
    }
}

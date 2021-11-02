package io.esastack.restclient.codec;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Stack;

public class GenericType<T> {

    /**
     * Type represented by the generic type instance.
     */
    private final Type type;
    /**
     * The actual raw parameter type.
     */
    private final Class<?> rawType;

    /**
     * Create a {@link GenericType generic type} from a
     * Java {@code instance}.
     * <p>
     * If the supplied instance is a {@link GenericEntity}, the generic type
     * will be computed using the {@link GenericEntity#getType()}.
     * Otherwise {@code instance.getClass()} will be used.
     * </p>
     *
     * @param instance Java instance for which the {@code GenericType} description should be created.
     * @return {@code GenericType} describing the Java {@code instance}.
     * @since 2.1
     */
    public static GenericType forInstance(final Object instance) {
        final GenericType genericType;
        if (instance instanceof GenericEntity) {
            genericType = new GenericType(((GenericEntity) instance).getType());
        } else {
            genericType = (instance == null) ? null : new GenericType(instance.getClass());
        }
        return genericType;
    }

    /**
     * Constructs a new generic type, deriving the generic type and class from
     * type parameter. Note that this constructor is protected, users should create
     * a (usually anonymous) subclass as shown above.
     *
     * @throws IllegalArgumentException in case the generic type parameter value is not
     *                                  provided by any of the subclasses.
     */
    protected GenericType() {
        // Get the type parameter of GenericType<T> (aka the T value)
        type = getTypeArgument(getClass(), GenericType.class);
        rawType = getClass(type);
    }

    /**
     * Constructs a new generic type, supplying the generic type
     * information and deriving the class.
     *
     * @param genericType the generic type.
     * @throws IllegalArgumentException if genericType is {@code null} or not an instance of
     *                                  {@code Class} or {@link ParameterizedType} whose raw
     *                                  type is an instance of {@code Class}.
     */
    public GenericType(Type genericType) {
        if (genericType == null) {
            throw new IllegalArgumentException("Type must not be null");
        }

        type = genericType;
        rawType = getClass(type);
    }

    /**
     * Retrieve the type represented by the generic type instance.
     *
     * @return the actual type represented by this generic type instance.
     */
    public final Type getType() {
        return type;
    }

    /**
     * Returns the object representing the class or interface that declared
     * the type represented by this generic type instance.
     *
     * @return the class or interface that declared the type represented by this
     *         generic type instance.
     */
    public final Class<?> getRawType() {
        return rawType;
    }

    /**
     * Returns the object representing the class or interface that declared
     * the supplied {@code type}.
     *
     * @param type {@code Type} to inspect.
     * @return the class or interface that declared the supplied {@code type}.
     */
    private static Class getClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class) {
                return (Class) parameterizedType.getRawType();
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType array = (GenericArrayType) type;
            final Class<?> componentRawType = getClass(array.getGenericComponentType());
            return getArrayClass(componentRawType);
        }
        throw new IllegalArgumentException("Type parameter " + type.toString() + " not a class or " +
                "parameterized type whose raw type is a class");
    }

    /**
     * Get Array class of component class.
     *
     * @param c the component class of the array
     * @return the array class.
     */
    private static Class getArrayClass(Class c) {
        try {
            Object o = Array.newInstance(c, 0);
            return o.getClass();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Return the value of the type parameter of {@code GenericType<T>}.
     *
     * @param clazz     subClass of {@code baseClass} to analyze.
     * @param baseClass base class having the type parameter the value of which we need to retrieve
     * @return the parameterized type of {@code GenericType<T>} (aka T)
     */
    static Type getTypeArgument(Class<?> clazz, Class<?> baseClass) {
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
        if (!result && obj instanceof GenericType) {
            // Compare inner type for equality
            GenericType<?> that = (GenericType<?>) obj;
            return this.type.equals(that.type);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "GenericType{" + type.toString() + "}";
    }
}

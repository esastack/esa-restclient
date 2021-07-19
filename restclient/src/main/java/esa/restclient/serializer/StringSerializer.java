package esa.restclient.serializer;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer {

    public static final StringSerializer UTF_8_INSTANCE
            = new StringSerializer(StandardCharsets.UTF_8);

    private final Charset charset;

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String deSerialize(byte[] data, Type type) {
        if (data == null || type == null) {
            return null;
        }

        if (type instanceof Class &&
                ((Class<?>) type).isAssignableFrom(String.class)
        ) {
            return new String(data, charset);
        }

        throw new UnsupportedOperationException("StringSerializer only can deSerialize String.class and its parentClass");
    }

    @Override
    public byte[] serialize(Object target) {
        if (target == null) {
            return null;
        }

        if (target instanceof String) {
            return ((String) target).getBytes(charset);
        }

        throw new UnsupportedOperationException("StringSerializer only can serialize String.class and its subClass");
    }


    public static StringSerializer of(Charset charsets) {
        if (charsets == null || StandardCharsets.UTF_8.equals(charsets)) {
            return UTF_8_INSTANCE;
        }
        return new StringSerializer(charsets);
    }
}

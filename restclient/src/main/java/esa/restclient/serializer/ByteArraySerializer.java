package esa.restclient.serializer;

import java.lang.reflect.Type;

public class ByteArraySerializer implements Serializer {

    public static final ByteArraySerializer INSTANCE
            = new ByteArraySerializer();

    @Override
    public byte[] deSerialize(byte[] data, Type type) {
        if (byte[].class.equals(type)) {
            return data;
        }

        throw new UnsupportedOperationException("ByteArraySerializer only can deSerialize byte[].class");
    }

    @Override
    public byte[] serialize(Object target) {
        if (target == null) {
            return null;
        }

        if (target instanceof byte[]) {
            return (byte[]) target;
        }

        throw new UnsupportedOperationException("StringSerializer only can serialize byte[].class and its subClass");

    }
}

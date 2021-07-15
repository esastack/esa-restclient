package esa.restclient.serializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer {

    //TODO 丰富功能
    @Override
    public String deSerialize(byte[] data, Type type) {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deSerialize(HttpInputStream inputStream, Type type) throws Exception {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }

    //TODO 丰富功能
    @Override
    public byte[] serialize(Object target) {
        if (target == null) {
            return null;
        }

        return ((String) target).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void serialize(Object target, HttpOutputStream outputStream) throws Exception {
        //TODO implement the method!
        throw new UnsupportedOperationException("The method need to be implemented!");
    }
}

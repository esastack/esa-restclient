package io.esastack.restclient.codec;

import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.DefaultDecoder;
import io.esastack.restclient.codec.impl.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultDecoderTest {

    @Test
    void testDecodeNull() throws Exception {
        DefaultDecoder decoder = new DefaultDecoder();
        assertThrows(NullPointerException.class, () ->
                decoder.decode(null, null, null, null));

        then((Object) decoder.decode(null, null, ResponseBodyContent.of("hello".getBytes()), null))
                .isEqualTo(null);

        then((Object) decoder.decode(null, null, ResponseBodyContent.of(null), String.class))
                .isEqualTo(null);
    }

    @Test
    void testDecodeString() throws Exception {
        DefaultDecoder decoder = new DefaultDecoder();
        then((Object) decoder.decode(null, null, ResponseBodyContent.of("hello".getBytes()), String.class))
                .isEqualTo("hello");
    }

    @Test
    void testDecodeBytes() throws Exception {
        byte[] bytes = "hello".getBytes();
        DefaultDecoder decoder = new DefaultDecoder();
        then((Object) decoder.decode(null, null, ResponseBodyContent.of(bytes), byte[].class))
                .isEqualTo(bytes);
    }

    @Test
    void testDecodeJson() throws Exception {
        Person person = new Person("LiMing", "boy");
        byte[] bytes = new JacksonCodec().doEncode(null, null, person);
        DefaultDecoder decoder = new DefaultDecoder();
        then((Object) decoder.decode(null, null, ResponseBodyContent.of(bytes), Person.class))
                .isEqualTo(person);
    }

    private static class Person {
        private String name;
        private String sex;

        public Person() {
        }

        public Person(String name, String sex) {
            this.name = name;
            this.sex = sex;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Person person = (Person) o;
            return Objects.equals(name, person.name) &&
                    Objects.equals(sex, person.sex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, sex);
        }
    }

}

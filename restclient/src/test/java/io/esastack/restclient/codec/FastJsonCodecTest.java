package io.esastack.restclient.codec;

import com.alibaba.fastjson.JSON;
import io.esastack.commons.net.http.MediaTypeUtil;
import io.esastack.restclient.ResponseBodyContent;
import io.esastack.restclient.codec.impl.FastJsonCodec;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.BDDAssertions.then;

public class FastJsonCodecTest {

    @Test
    void testEncode() throws Exception {
        FastJsonCodec fastJsonCodec = new FastJsonCodec();

        then(fastJsonCodec.encode(MediaTypeUtil.APPLICATION_JSON_UTF8, null, null).content())
                .isEqualTo("null".getBytes(StandardCharsets.UTF_8));

        Person person = new Person("LiMing", "boy");
        then(fastJsonCodec.encode(null, null, person).content())
                .isEqualTo(JSON.toJSONBytes(person));
    }

    @Test
    void testDecode() throws Exception {
        FastJsonCodec fastJsonCodec = new FastJsonCodec();
        then((Object) fastJsonCodec.decode(null, null, ResponseBodyContent.of(null), null))
                .isEqualTo(null);

        Person person = new Person("LiMing", "boy");
        byte[] bytes = JSON.toJSONBytes(person);
        then((Object) fastJsonCodec.decode(null, null, ResponseBodyContent.of(bytes), Person.class))
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

/*
 * Copyright 2020 OPPO ESA Stack Project
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
package io.esastack.httpclient.core;

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpUriTest {

    @Test
    void testConstructor0() {
        assertThrows(IllegalArgumentException.class, () -> new HttpUri(null));
        assertThrows(IllegalArgumentException.class, () -> new HttpUri(""));
        assertThrows(IllegalArgumentException.class, () -> new HttpUri((String) null, null));
        assertThrows(IllegalArgumentException.class, () -> new HttpUri("", null));
    }

    @Test
    void testConstructor1() {
        final MultiValueMap<String, String> params = new HashMultiValueMap<>();
        final HttpUri uri = new HttpUri("/abc", params);
        then(params).isNotSameAs(uri.params());
        params.add("a", "b");
        params.add("c", "d");
        then(params.size()).isEqualTo(2);
        then(uri.params().isEmpty()).isTrue();
    }

    @Test
    void testOperateParam() {
        final MultiValueMap<String, String> params = new HashMultiValueMap<>();
        params.add("a", "b");
        params.add("c", "d");
        final HttpUri uri = new HttpUri("/abc", params);
        then(uri.params().size()).isEqualTo(2);
        then(uri.getParam("a")).isEqualTo("b");
        then(uri.getParam("c")).isEqualTo("d");

        then(uri.getParam("xx")).isNull();
        uri.addParam("x", "y");
        uri.addParam("y", "z");
        final List<String> qValues = new LinkedList<>();
        qValues.add("a");
        qValues.add("b");
        qValues.add("c");
        uri.addParams("q", qValues);
        then(uri.params().size()).isEqualTo(5);
        then(uri.getParam("q")).isEqualTo("a");
        then(uri.params("a").size()).isEqualTo(1);
        then(uri.params("a").get(0)).isEqualTo("b");
        then(uri.params("c").size()).isEqualTo(1);
        then(uri.params("c").get(0)).isEqualTo("d");
        then(uri.params("x").size()).isEqualTo(1);
        then(uri.params("x").get(0)).isEqualTo("y");
        then(uri.params("y").size()).isEqualTo(1);
        then(uri.params("y").get(0)).isEqualTo("z");
        then(uri.params("q").size()).isEqualTo(3);
        then(uri.params("q").get(0)).isEqualTo("a");
        then(uri.params("q").get(1)).isEqualTo("b");
        then(uri.params("q").get(2)).isEqualTo("c");

        then(uri.paramNames().size()).isEqualTo(5);
        then(uri.paramNames().contains("a")).isTrue();
        then(uri.paramNames().contains("c")).isTrue();
        then(uri.paramNames().contains("x")).isTrue();
        then(uri.paramNames().contains("y")).isTrue();
        then(uri.paramNames().contains("q")).isTrue();

        assertThrows(NullPointerException.class, () -> uri.addParam(null, "xx"));
        assertThrows(NullPointerException.class, () -> uri.addParam("xx", null));
        assertThrows(NullPointerException.class, () -> uri.addParams(null, Collections.singletonList("xx")));
        assertThrows(NullPointerException.class, () -> uri.addParams("xx", null));
        assertThrows(NullPointerException.class, () -> uri.params(null));
        assertThrows(NullPointerException.class, () -> uri.getParam(null));

        assertThrows(UnsupportedOperationException.class, () -> uri.unmodifiableParams().add("m", "n"));
    }

    @Test
    void testRelative0() {
        assertThrows(IllegalArgumentException.class, () ->
                new HttpUri("http://127.0.0.1:8080/abc/def?a=b&c=d").relative(true));
    }

    @Test
    void testRelative() {
        // Case1: with query
        assertThrows(IllegalArgumentException.class,
                () -> new HttpUri("http://127.0.0.1:8080/abc/def?a=b&c=d").relative(true));

        // Case2: without query
        final HttpUri uri2 = new HttpUri("http://127.0.0.1:9999/def?");
        assertNull(uri2.getParam("x"));
        assertNull(uri2.getParam("y"));
        assertTrue(uri2.paramNames().isEmpty());

        uri2.addParam("a", "b");
        uri2.addParam("x", "y");
        then(uri2.relative(true)).isEqualTo("/def?a=b&x=y");

        final HttpUri uri3 = new HttpUri("http://127.0.0.1:9999/def?");
        then(uri3.relative(true)).isEqualTo("/def");

        final HttpUri uri4 = new HttpUri("http://127.0.0.1:9999/def");
        uri4.addParam("session", "xxx=");
        uri4.addParam("name", "???");
        uri4.addParam("a", "b=c");
        then(uri4.relative(true))
                .isEqualTo("/def?a=b%3Dc&session=xxx%3D&name=%3F%3F%3F");
    }

    @Test
    void testSpliceRelativeDirectly() {
        // Case1: with query
        final HttpUri uri1 = new HttpUri("http://127.0.0.1:8080/abc/def?a=b&c=d");
        then(uri1.relative(false)).isEqualTo("/abc/def?a=b&c=d");

        // Case2: with query and params
        final HttpUri uri2 = new HttpUri("http://127.0.0.1:8080/abc/def?a=b&c=d");
        uri2.addParam("a", "b=c");
        then(uri2.relative(false)).isEqualTo("/abc/def?a=b&c=d&a=b=c");

        final HttpUri uri3 = new HttpUri("http://127.0.0.1:8080/?a=b&c=d");
        uri3.addParam("a", "b=c");
        then(uri3.relative(false)).isEqualTo("/?a=b&c=d&a=b=c");

        final HttpUri uri4 = new HttpUri("http://127.0.0.1:8080?a=b&c=d");
        uri4.addParam("a", "b=c");
        then(uri4.relative(false)).isEqualTo("?a=b&c=d&a=b=c");

        // Case5: without query
        final HttpUri uri5 = new HttpUri("http://127.0.0.1:8080/abc/def?");
        then(uri5.relative(false)).isEqualTo("/abc/def");

        final HttpUri uri6 = new HttpUri("http://127.0.0.1:8080/abc/def?");
        uri6.addParam("a", "b=c");
        then(uri6.relative(false)).isEqualTo("/abc/def?a=b=c");

        final HttpUri uri7 = new HttpUri("http://127.0.0.1:8080/?");
        uri7.addParam("a", "b=c");
        then(uri7.relative(false)).isEqualTo("/?a=b=c");

        final HttpUri uri8 = new HttpUri("http://127.0.0.1:8080?");
        uri8.addParam("a", "b=c");
        then(uri8.relative(false)).isEqualTo("?a=b=c");
    }

    @Test
    void testToString() {
        final String rawUri = "http://127.0.0.1:8080/abc?a=b&c=d";
        final HttpUri uri = new HttpUri(rawUri);
        then(uri.toString()).isSameAs(rawUri);
    }

    @Test
    void testNetURI() {
        final String rawUri = "http://127.0.0.1:8080/abc?a=b&c=d";
        final HttpUri uri = new HttpUri(rawUri);
        then(uri.host()).isEqualTo("127.0.0.1");
        then(uri.port()).isEqualTo(8080);
        then(uri.netURI()).isEqualTo(URI.create("http://127.0.0.1:8080/abc?a=b&c=d"));
    }

}

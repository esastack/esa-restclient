/*
 * Copyright 2021 OPPO ESA Stack Project
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
package io.esastack.httpclient.core.util;

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MultiValueMapUtilsTest {

    @Test
    void testUnmodifiableMap() {
        final MultiValueMap<String, String> underlying = new HashMultiValueMap<>(8);
        underlying.add("a", "b");
        underlying.add("a", "c");
        underlying.add("x", "n");

        final MultiValueMap<String, String> map = MultiValueMapUtils.unmodifiableMap(underlying);
        assertThrows(UnsupportedOperationException.class, () -> map.add("x", ""));
        assertThrows(UnsupportedOperationException.class, () -> map.addAll("x", Collections.emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> map.addFirst("x", ""));
        assertThrows(UnsupportedOperationException.class, () -> map.putSingle("x", ""));
        assertThrows(UnsupportedOperationException.class, () -> map.put("x", Collections.emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("x"));
        assertThrows(UnsupportedOperationException.class, () -> map.putAll(new HashMultiValueMap<>()));
        assertThrows(UnsupportedOperationException.class, map::clear);
        assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((key, values) -> null));
        assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent("x", Collections.emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> map.replace("a", Collections.emptyList(),
                Collections.emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent("xx", (key) -> null));
        assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent("xx", (key, v) -> null));
        assertThrows(UnsupportedOperationException.class, () -> map.compute("a", (key, value) -> null));
        assertThrows(UnsupportedOperationException.class, () -> map.replace("a", Collections.emptyList()));
        assertThrows(UnsupportedOperationException.class, () -> map.merge("a", Collections.emptyList(),
                (key, value) -> null));

        assertEquals("b", map.getFirst("a"));
        assertEquals(underlying.toSingleValueMap().size(), map.toSingleValueMap().size());
        assertEquals(underlying.size(), map.size());
        assertEquals(underlying.isEmpty(), map.isEmpty());
        assertEquals(underlying.containsKey("a"), map.containsKey("a"));
        assertEquals(underlying.containsValue("a"), map.containsValue("a"));
        assertEquals(underlying.containsValue("b"), map.containsValue("b"));
        assertNull(map.get("n"));
        assertThrows(UnsupportedOperationException.class, () -> map.get("a").add("d"));
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().add("d"));
        assertThrows(UnsupportedOperationException.class, () -> map.values().add(Collections.singletonList("d")));
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().clear());
        assertNull(map.getOrDefault("xxx", null));
        assertThrows(UnsupportedOperationException.class, () -> map.getOrDefault("a", null).clear());

        final MultiValueMap<String, String> underlying0 = mock(MultiValueMap.class);
        final MultiValueMap<String, String> map0 = MultiValueMapUtils.unmodifiableMap(underlying0);

        final BiConsumer<String, List<String>> action = (k, v) -> { };
        verify(underlying0, never()).forEach(action);
        map0.forEach(action);
        verify(underlying0).forEach(action);
    }

}


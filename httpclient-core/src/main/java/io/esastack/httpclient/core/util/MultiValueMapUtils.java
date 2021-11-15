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

import esa.commons.collection.MultiValueMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class MultiValueMapUtils {

    private MultiValueMapUtils() {
    }

    public static <K, V> MultiValueMap<K, V> unmodifiableMap(MultiValueMap<K, V> underlying) {
        return underlying == null ? null : new UnmodifiableMultiValueMap<>(underlying);
    }

    private static class UnmodifiableMultiValueMap<K, V> implements MultiValueMap<K, V> {

        private static final UnsupportedOperationException UNSUPPORTED = new UnsupportedOperationException();

        private final MultiValueMap<K, V> underlying;

        private transient Set<K> keySet;
        private transient Set<Map.Entry<K, List<V>>> entrySet;
        private transient Collection<List<V>> values;

        private UnmodifiableMultiValueMap(MultiValueMap<K, V> underlying) {
            this.underlying = underlying;
        }

        @Override
        public void add(K key, V value) {
            throw UNSUPPORTED;
        }

        @Override
        public void addAll(K key, Iterable<? extends V> values) {
            throw UNSUPPORTED;
        }

        @Override
        public void addFirst(K key, V value) {
            throw UNSUPPORTED;
        }

        @Override
        public V getFirst(K key) {
            return underlying.getFirst(key);
        }

        @Override
        public void putSingle(K key, V value) {
            throw UNSUPPORTED;
        }

        @Override
        public Map<K, V> toSingleValueMap() {
            return underlying.toSingleValueMap();
        }

        @Override
        public int size() {
            return underlying.size();
        }

        @Override
        public boolean isEmpty() {
            return underlying.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return underlying.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return underlying.containsValue(value);
        }

        @Override
        public List<V> get(Object key) {
            List<V> values = underlying.get(key);
            return values == null ? null : Collections.unmodifiableList(values);
        }

        @Override
        public List<V> put(K key, List<V> value) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> remove(Object key) {
            throw UNSUPPORTED;
        }

        @Override
        public void putAll(Map<? extends K, ? extends List<V>> m) {
            throw UNSUPPORTED;
        }

        @Override
        public void clear() {
            throw UNSUPPORTED;
        }

        @Override
        public Set<K> keySet() {
            if (keySet == null) {
                keySet = Collections.unmodifiableSet(underlying.keySet());
            }
            return keySet;
        }

        @Override
        public Collection<List<V>> values() {
            if (values == null) {
                values = Collections.unmodifiableCollection(underlying.values());
            }

            return values;
        }

        @Override
        public Set<Entry<K, List<V>>> entrySet() {
            if (entrySet == null) {
                entrySet = Collections.unmodifiableSet(underlying.entrySet());
            }

            return entrySet;
        }

        @Override
        public List<V> getOrDefault(Object key, List<V> defaultValue) {
            List<V> values = underlying.getOrDefault(key, defaultValue);
            return values == null ? null : Collections.unmodifiableList(values);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super List<V>> action) {
            underlying.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super List<V>, ? extends List<V>> function) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> putIfAbsent(K key, List<V> value) {
            throw UNSUPPORTED;
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw UNSUPPORTED;
        }

        @Override
        public boolean replace(K key, List<V> oldValue, List<V> newValue) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> replace(K key, List<V> value) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> computeIfAbsent(K key, Function<? super K, ? extends List<V>> mappingFunction) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> computeIfPresent(K key,
                                        BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> compute(K key, BiFunction<? super K, ? super List<V>, ? extends List<V>> remappingFunction) {
            throw UNSUPPORTED;
        }

        @Override
        public List<V> merge(K key, List<V> value,
                             BiFunction<? super List<V>, ? super List<V>, ? extends List<V>> remappingFunction) {
            throw UNSUPPORTED;
        }
    }

}


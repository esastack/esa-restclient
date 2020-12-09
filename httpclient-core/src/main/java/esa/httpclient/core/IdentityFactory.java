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
package esa.httpclient.core;

@FunctionalInterface
public interface IdentityFactory<T> {

    /**
     * Generates a {@link Identified} of {@code value}.
     *
     * @param value origin value
     * @return identified
     */
    Identified<T> generate(T value);

    class Identified<T> implements Identifiable {

        private final T origin;
        private final String id;

        public Identified(T origin, String id) {
            this.origin = origin;
            this.id = id;
        }

        public T origin() {
            return origin;
        }

        @Override
        public String identity() {
            return id;
        }
    }

}

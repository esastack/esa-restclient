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
package io.esastack.restclient.utils;

public final class Constants {

    private Constants() {
    }

    public static final class Order {
        public static final int FAST_JSON = -2048;
        public static final int GSON = -2048;
        public static final int JACKSON = 0;
        public static final int NORMAL = 0;
        public static final int BYTE_TO_BYTE_CODEC = -4096;
        public static final int STRING_CODEC = -4096;
    }
}

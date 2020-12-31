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
package esa.httpclient.core.util;

import esa.commons.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class HttpClientVer {

    private HttpClientVer() {
    }

    private static final String VERSION = getVersion();

    /**
     * Gets the code version of HttpClient.
     *
     * @return version
     */
    public static String version() {
        return VERSION;
    }

    private static String getVersion() {
        try (InputStream is = HttpClientVer.class.getClassLoader()
                .getResourceAsStream("META-INF/httpclient/HttpClient-Ver.txt")) {
            if (is == null) {
                throw new IllegalStateException("Could not find out version of ESA HttpClient.");
            }
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
                return scanner.hasNext() ? scanner.next() : StringUtils.empty();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

}

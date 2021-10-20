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
package io.esastack.httpclient.core;

import esa.commons.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MultipartFileItemTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () ->
                new MultipartFileItem(null, "abc", mock(File.class),
                        HttpHeaderValues.APPLICATION_JSON, true));

        assertThrows(NullPointerException.class, () ->
                new MultipartFileItem("abc", "abc", null,
                        HttpHeaderValues.APPLICATION_JSON, true));
    }

    @Test
    void testGetters() {
        final String name = "name";
        final String fileName = "fileName";
        final File file = mock(File.class);
        final String contentType = "contentType";
        final boolean isText = ThreadLocalRandom.current().nextBoolean();
        final MultipartFileItem item = new MultipartFileItem(name, fileName, file, contentType, isText);
        then(item.name()).isEqualTo(name);
        then(item.fileName()).isEqualTo(fileName);
        then(item.file()).isSameAs(file);
        then(item.contentType()).isEqualTo(contentType);
        then(item.isText()).isEqualTo(isText);
    }

}


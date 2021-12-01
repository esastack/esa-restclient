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

import io.esastack.commons.net.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import static io.esastack.httpclient.core.MultipartFileItem.DEFAULT_BINARY_CONTENT_TYPE;
import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MultipartFileItemTest {

    @Test
    void testConstructor() {
        File file1 = new File("/abc");
        MultipartFileItem item1 = new MultipartFileItem("item1", file1);
        assertEquals("item1", item1.name());
        assertEquals(file1.getName(), item1.fileName());
        assertSame(file1, item1.file());
        assertEquals(DEFAULT_BINARY_CONTENT_TYPE, item1.contentType());
        assertFalse(item1.isText());

        File file2 = new File("/mn");
        MultipartFileItem item2 = new MultipartFileItem("item2", file2, HttpHeaderValues.APPLICATION_JSON);
        assertEquals("item2", item2.name());
        assertEquals(file2.getName(), item2.fileName());
        assertSame(file2, item2.file());
        assertEquals(HttpHeaderValues.APPLICATION_JSON, item2.contentType());
        assertFalse(item2.isText());

        File file3 = new File("/xyz");
        MultipartFileItem item3 = new MultipartFileItem("item3", file3, HttpHeaderValues.S_MAXAGE, true);
        assertEquals("item3", item3.name());
        assertEquals(file3.getName(), item3.fileName());
        assertSame(file3, item3.file());
        assertEquals(HttpHeaderValues.S_MAXAGE, item3.contentType());
        assertTrue(item3.isText());

        assertThrows(NullPointerException.class, () ->
                new MultipartFileItem(null, "abc", mock(File.class),
                        HttpHeaderValues.APPLICATION_JSON, true));

        assertThrows(NullPointerException.class, () ->
                new MultipartFileItem("abc", null, mock(File.class),
                        HttpHeaderValues.APPLICATION_JSON, true));

        assertThrows(NullPointerException.class, () ->
                new MultipartFileItem("abc", "abc", null,
                        HttpHeaderValues.APPLICATION_JSON, true));

        assertThrows(NullPointerException.class, () ->
                new MultipartFileItem("abc", "abc", mock(File.class),
                        null, true));

        assertDoesNotThrow(() ->
                new MultipartFileItem("abc", "abc", mock(File.class),
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


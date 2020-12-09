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
package esa.httpclient.core.netty;

import esa.commons.collection.HashMultiValueMap;
import esa.commons.collection.MultiValueMap;
import esa.httpclient.core.MultipartFileItem;
import esa.httpclient.core.MultipartRequest;
import esa.httpclient.core.RequestOptions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipartRequestImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new MultipartRequestImpl(null));

        new MultipartRequestImpl(mock(RequestOptions.class));
    }

    @Test
    void testGetAttributesAndFiles() {
        final MultiValueMap<String, String> attributes = new HashMultiValueMap<>();
        attributes.add("A", "B");
        attributes.add("X", "Y");

        final List<MultipartFileItem> files = new ArrayList<>();
        files.add(new MultipartFileItem("file1", "file1", mock(File.class),
                "xxx", true));
        files.add(new MultipartFileItem("file2", "file2", mock(File.class),
                "xxx", true));

        RequestOptions options = mock(RequestOptions.class);
        when(options.attributes()).thenReturn(attributes);
        when(options.files()).thenReturn(files);

        MultipartRequest request = new MultipartRequestImpl(options);
        then(request.attributes()).isSameAs(attributes);
        then(request.files()).isSameAs(files);
    }
}

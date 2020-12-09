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

import esa.httpclient.core.PlainRequest;
import esa.httpclient.core.RequestOptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlainRequestImplTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class, () -> new PlainRequestImpl(null));

        new PlainRequestImpl(mock(RequestOptions.class));
    }

    @Test
    void testGetBody() {
        final byte[] data = "Hello World!".getBytes();
        RequestOptions options = mock(RequestOptions.class);
        when(options.body()).thenReturn(data);

        PlainRequest request = new PlainRequestImpl(options);
        then(request.body()).isSameAs(data);
    }
}

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
package io.esastack.httpclient.core.filter;

import esa.commons.collection.AttributeKey;
import io.esastack.httpclient.core.Context;
import io.esastack.httpclient.core.mock.MockFilterContext;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FilterContextTest {

    @Test
    void testConstructor() {
        assertThrows(NullPointerException.class,
                () -> new FilterContext(null));
        new FilterContext(mock(Context.class));
    }

    @Test
    void testAttrOperation() {
        final Context ctx0 = mock(Context.class);
        final MockFilterContext ctx = new MockFilterContext(ctx0);

        final Object value1 = new Object();
        ctx.attrs().attr(AttributeKey.valueOf("A")).set(value1);
        then(ctx.attrs().attr(AttributeKey.valueOf("A")).get()).isSameAs(value1);

        ctx.attrs().attr(AttributeKey.valueOf("A")).remove();
        then(ctx.attrs().attr(AttributeKey.valueOf("A")).get()).isNull();

        ctx.attrs().attr(AttributeKey.valueOf("A")).set(value1);
        final Object value2 = new Object();
        ctx.attrs().attr(AttributeKey.valueOf("A")).set(value2);
        then(ctx.attrs().attr(AttributeKey.valueOf("A")).get()).isSameAs(value2);
        ctx.clear();
        then(ctx.attrs().attr(AttributeKey.valueOf("A")).get()).isNull();

        ctx.clear();
        BDDAssertions.then(ctx.parent()).isSameAs(ctx0);
    }
}

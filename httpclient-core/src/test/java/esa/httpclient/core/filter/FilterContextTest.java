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
package esa.httpclient.core.filter;

import esa.httpclient.core.Context;
import esa.httpclient.core.mock.MockFilterContext;
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
    void testIllegalArgument() {
        final FilterContext ctx = new FilterContext(mock(Context.class));
        assertThrows(NullPointerException.class, () -> ctx.setAttr(null, "a"));
        assertThrows(NullPointerException.class, () -> ctx.setAttr("a", null));
        assertThrows(NullPointerException.class, () -> ctx.removeAttr(null));
        assertThrows(NullPointerException.class, () -> ctx.getAttr(null));
    }

    @Test
    void testAttrOperation() {
        final Context ctx0 = mock(Context.class);
        final MockFilterContext ctx = new MockFilterContext(ctx0);

        final Object value1 = new Object();
        ctx.setAttr("A", value1);
        then((Object) ctx.getAttr("A")).isSameAs(value1);

        ctx.removeAttr("A");
        then((Object) ctx.getAttr("A")).isNull();

        ctx.setAttr("A", value1);
        final Object value2 = new Object();
        ctx.setAttr("A", value2);
        then((Object) ctx.getAttr("A")).isSameAs(value2);
        ctx.clear();
        then((Object) ctx.getAttr("A")).isNull();

        ctx.clear();
        ctx.setAttr("A", true);
        then((Boolean) ctx.getAttr("A")).isEqualTo(true);

        ctx.clear();
        ctx.setAttr("B", false);
        then((Boolean) ctx.getAttr("B")).isEqualTo(false);

        ctx.clear();
        ctx.setAttr("A", 3);
        then((Integer) ctx.getAttr("A")).isEqualTo(3);
        then((Integer) ctx.getAttr("B")).isNull();

        then(ctx.attrNames().size()).isEqualTo(1);
        then(ctx.attrNames().contains("A")).isTrue();

        ctx.clear();
        then(ctx.parent()).isSameAs(ctx0);
    }
}

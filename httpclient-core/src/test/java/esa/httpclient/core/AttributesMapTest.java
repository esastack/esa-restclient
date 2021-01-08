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
package esa.httpclient.core;

import esa.httpclient.core.mock.MockContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttributesMapTest {

    @Test
    void testIllegalArgument() {
        final Context ctx = new Context();
        assertThrows(NullPointerException.class, () -> ctx.setAttr(null, "a"));
        assertThrows(NullPointerException.class, () -> ctx.setAttr("a", null));
        assertThrows(NullPointerException.class, () -> ctx.removeAttr(null));
        assertThrows(NullPointerException.class, () -> ctx.getAttr(null));
    }

    @Test
    void testAttrOperation() {
        final MockContext ctx = new MockContext();

        final Object value1 = new Object();
        ctx.setAttr("A", value1);
        then(ctx.getAttr("A")).isSameAs(value1);

        ctx.removeAttr("A");
        then(ctx.getAttr("A")).isNull();

        ctx.setAttr("A", value1);
        final Object value2 = new Object();
        ctx.setAttr("A", value2);
        then(ctx.getAttr("A")).isSameAs(value2);
        ctx.clear();
        then(ctx.getAttr("A")).isNull();

        ctx.clear();
        ctx.setAttr("A", true);
        then(ctx.getAttr("A")).isEqualTo(true);

        ctx.clear();
        ctx.setAttr("B", false);
        then(ctx.getAttr("B")).isEqualTo(false);

        ctx.clear();
        ctx.setAttr("A", 3);
        then(ctx.getAttr("A")).isEqualTo(3);
        then(ctx.getAttr("B")).isNull();

        then(ctx.attrNames().size()).isEqualTo(1);
        then(ctx.attrNames().contains("A")).isTrue();
    }
}

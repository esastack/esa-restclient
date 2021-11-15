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
package io.esastack.httpclient.core.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

class OrderedComparatorTest {

    @Test
    void testSort() {
        OrderedComparator.sort(null);

        OrderedComparator.sort(Collections.singletonList(new Ordered() {
        }));

        final List<Ordered> items = new LinkedList<>();
        items.add(new Ordered() {
            @Override
            public int getOrder() {
                return 100;
            }
        });

        items.add(new Ordered() {
            @Override
            public int getOrder() {
                return 0;
            }
        });

        items.add(new Ordered() {
            @Override
            public int getOrder() {
                return -100;
            }
        });

        OrderedComparator.sort(items);
        then(items.get(0).getOrder()).isEqualTo(-100);
        then(items.get(1).getOrder()).isEqualTo(0);
        then(items.get(2).getOrder()).isEqualTo(100);
    }

}

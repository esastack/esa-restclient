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

import java.util.Comparator;
import java.util.List;

public final class OrderedComparator implements Comparator<Ordered> {

    private static final OrderedComparator INSTANCE = new OrderedComparator();

    public static void sort(List<? extends Ordered> list) {
        if (list == null) {
            return;
        }
        if (list.size() > 1) {
            list.sort(INSTANCE);
        }
    }

    @Override
    public int compare(Ordered o1, Ordered o2) {
        int i1 = getOrder(o1);
        int i2 = getOrder(o2);
        return Integer.compare(i1, i2);
    }

    private static int getOrder(Ordered obj) {
        return obj == null ? Ordered.LOWEST_PRECEDENCE : obj.getOrder();
    }

}

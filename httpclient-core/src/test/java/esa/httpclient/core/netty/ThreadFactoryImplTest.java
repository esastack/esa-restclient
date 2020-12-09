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

import esa.commons.concurrent.NettyInternalThread;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadFactory;

import static org.assertj.core.api.Java6BDDAssertions.then;

class ThreadFactoryImplTest {

    @Test
    void testNewThread() {
        final String prefix = "HttpClient-Callback";

        ThreadFactory factory = new ThreadFactoryImpl(prefix, true);
        Thread thread = factory.newThread(() -> {});
        then(thread.getName().startsWith("HttpClient-Callback")).isTrue();
        then(thread).isInstanceOf(NettyInternalThread.class);
    }
}

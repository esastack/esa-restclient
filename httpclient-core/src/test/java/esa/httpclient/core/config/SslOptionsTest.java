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
package esa.httpclient.core.config;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.BDDAssertions.then;

class SslOptionsTest {

    @Test
    void testCustom() {
        final String[] ciphers = new String[]{"a", "b", "c"};
        final String[] enabledProtocols = new String[]{"protocol1", "protocol2"};
        final long handshake = ThreadLocalRandom.current().nextLong(1000);
        final long sessionCacheSize = ThreadLocalRandom.current().nextLong(1000);
        final long sessionTimeout = ThreadLocalRandom.current().nextLong(1000);
        final File file = new File("abc");

        final SslOptions options = SslOptions.options()
                .ciphers(ciphers)
                .enabledProtocols(enabledProtocols)
                .handshakeTimeoutMillis(handshake)
                .sessionCacheSize(sessionCacheSize)
                .sessionTimeout(sessionTimeout)
                .trustCertificates(file)
                .build();

        then(Arrays.equals(options.ciphers(), ciphers)).isTrue();
        then(Arrays.equals(options.enabledProtocols(), enabledProtocols)).isTrue();
        then(options.handshakeTimeoutMillis()).isEqualTo(handshake);
        then(options.sessionCacheSize()).isEqualTo(sessionCacheSize);
        then(options.sessionTimeout()).isEqualTo(sessionTimeout);
        then(options.trustCertificates()).isSameAs(file);
        then(options.useInsecureTrustManager()).isFalse();
    }

    @Test
    void testCopy() {
        final String[] ciphers = new String[]{"a", "b", "c"};
        final String[] enabledProtocols = new String[]{"protocol1", "protocol2"};
        final long handshake = ThreadLocalRandom.current().nextLong(1000);
        final long sessionCacheSize = ThreadLocalRandom.current().nextLong(1000);
        final long sessionTimeout = ThreadLocalRandom.current().nextLong(1000);
        final File file = new File("abc");

        final SslOptions options = SslOptions.options()
                .ciphers(ciphers)
                .enabledProtocols(enabledProtocols)
                .handshakeTimeoutMillis(handshake)
                .sessionCacheSize(sessionCacheSize)
                .sessionTimeout(sessionTimeout)
                .trustCertificates(file)
                .build()
                .copy();

        then(options.ciphers()).isNotSameAs(ciphers);
        then(options.enabledProtocols()).isNotSameAs(enabledProtocols);
        then(Arrays.equals(options.ciphers(), ciphers)).isTrue();
        then(Arrays.equals(options.enabledProtocols(), enabledProtocols)).isTrue();
        then(options.handshakeTimeoutMillis()).isEqualTo(handshake);
        then(options.sessionCacheSize()).isEqualTo(sessionCacheSize);
        then(options.sessionTimeout()).isEqualTo(sessionTimeout);
        then(options.trustCertificates()).isSameAs(file);
        then(options.useInsecureTrustManager()).isFalse();
    }
}

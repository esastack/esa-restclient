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

import esa.httpclient.core.Reusable;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class SslOptions implements Reusable<SslOptions>, Serializable {

    private static final long serialVersionUID = 743144112670237615L;

    private static final String[] EMPTY_ARRAYS = new String[0];

    private final String[] ciphers;
    private final String[] enabledProtocols;
    private final File trustCertificates;
    private final boolean useInsecureTrustManager;
    private final long sessionTimeout;
    private final long sessionCacheSize;
    private final long handshakeTimeoutMillis;

    private SslOptions(String[] ciphers,
                       String[] enabledProtocols,
                       File trustCertificates,
                       boolean useInsecureTrustManager,
                       long sessionTimeout,
                       long sessionCacheSize,
                       long handshakeTimeoutMillis) {
        this.ciphers = (ciphers == null ? EMPTY_ARRAYS : ciphers);
        this.enabledProtocols = (enabledProtocols == null ? EMPTY_ARRAYS : enabledProtocols);
        this.sessionTimeout = sessionTimeout;
        this.sessionCacheSize = sessionCacheSize;
        this.handshakeTimeoutMillis = handshakeTimeoutMillis;
        this.trustCertificates = trustCertificates;
        this.useInsecureTrustManager = useInsecureTrustManager;
    }

    @Override
    public SslOptions copy() {
        return new SslOptions(ciphers == null ? EMPTY_ARRAYS : Arrays.copyOf(ciphers, ciphers.length),
                enabledProtocols == null ? EMPTY_ARRAYS : Arrays.copyOf(enabledProtocols, enabledProtocols.length),
                trustCertificates,
                useInsecureTrustManager,
                sessionTimeout,
                sessionCacheSize,
                handshakeTimeoutMillis);
    }

    public static SslOptionsBuilder options() {
        return new SslOptionsBuilder();
    }

    public String[] ciphers() {
        return ciphers;
    }

    public String[] enabledProtocols() {
        return enabledProtocols;
    }

    public long sessionTimeout() {
        return sessionTimeout;
    }

    public long sessionCacheSize() {
        return sessionCacheSize;
    }

    public long handshakeTimeoutMillis() {
        return handshakeTimeoutMillis;
    }

    public File trustCertificates() {
        return trustCertificates;
    }

    public boolean useInsecureTrustManager() {
        return useInsecureTrustManager;
    }

    public static class SslOptionsBuilder {

        private static final boolean DEFAULT_USE_INSECURE_TRUST_MANAGER =
                SystemPropertyUtil.getBoolean("esa.httpclient.sslOptions.useInsecureTrustManager", false);

        private String[] ciphers;
        private String[] enabledProtocols;
        private File trustCertificates;
        private long sessionTimeout;
        private long sessionCacheSize;
        private long handshakeTimeoutMillis;

        SslOptionsBuilder() {
        }

        public SslOptionsBuilder ciphers(String[] ciphers) {
            this.ciphers = ciphers;
            return this;
        }

        public SslOptionsBuilder enabledProtocols(String[] enabledProtocols) {
            this.enabledProtocols = enabledProtocols;
            return this;
        }

        public SslOptionsBuilder sessionTimeout(long sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        public SslOptionsBuilder sessionCacheSize(long sessionCacheSize) {
            this.sessionCacheSize = sessionCacheSize;
            return this;
        }

        public SslOptionsBuilder handshakeTimeoutMillis(long handshakeTimeoutMillis) {
            this.handshakeTimeoutMillis = handshakeTimeoutMillis;
            return this;
        }

        public SslOptionsBuilder trustCertificates(File trustCertificates) {
            this.trustCertificates = trustCertificates;
            return this;
        }

        public SslOptions build() {
            return new SslOptions(ciphers,
                    enabledProtocols,
                    trustCertificates,
                    DEFAULT_USE_INSECURE_TRUST_MANAGER,
                    sessionTimeout,
                    sessionCacheSize,
                    handshakeTimeoutMillis);
        }
    }

}

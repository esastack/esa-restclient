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
package io.esastack.httpclient.core.config;

import io.esastack.httpclient.core.Reusable;

import java.io.Serializable;
import java.util.StringJoiner;

public class NetOptions implements Reusable<NetOptions>, Serializable {

    private static final long serialVersionUID = 6352915710179113461L;

    private final int soSndBuf;
    private final int soRcvBuf;
    private final boolean soKeepAlive;
    private final boolean soReuseAddr;
    private final boolean tcpNoDelay;
    private final int soLinger;
    private final int writeBufferHighWaterMark;
    private final int writeBufferLowWaterMark;

    private NetOptions(int soSndBuf,
                       int soRcvBuf,
                       boolean soKeepAlive,
                       boolean soReuseAddr,
                       boolean tcpNoDelay,
                       int soLinger,
                       int writeBufferHighWaterMark,
                       int writeBufferLowWaterMark) {
        this.soSndBuf = soSndBuf;
        this.soRcvBuf = soRcvBuf;
        this.soKeepAlive = soKeepAlive;
        this.soReuseAddr = soReuseAddr;
        this.tcpNoDelay = tcpNoDelay;
        this.soLinger = soLinger;
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    @Override
    public NetOptions copy() {
        return new NetOptions(soSndBuf, soRcvBuf, soKeepAlive, soReuseAddr, tcpNoDelay, soLinger,
                writeBufferHighWaterMark, writeBufferLowWaterMark);
    }

    public static NetOptions ofDefault() {
        return new NetOptionsBuilder().build();
    }

    public static NetOptionsBuilder options() {
        return new NetOptionsBuilder();
    }

    public int soSndBuf() {
        return soSndBuf;
    }

    public int soRcvBuf() {
        return soRcvBuf;
    }

    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    public boolean isSoReuseAddr() {
        return soReuseAddr;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public int soLinger() {
        return soLinger;
    }

    public int writeBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public int writeBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NetOptions.class.getSimpleName() + "[", "]")
                .add("soSndBuf=" + soSndBuf)
                .add("soRcvBuf=" + soRcvBuf)
                .add("soKeepAlive=" + soKeepAlive)
                .add("soReuseAddr=" + soReuseAddr)
                .add("tcpNoDelay=" + tcpNoDelay)
                .add("soLinger=" + soLinger)
                .add("writeBufferHighWaterMark=" + writeBufferHighWaterMark)
                .add("writeBufferLowWaterMark=" + writeBufferLowWaterMark)
                .toString();
    }

    public static class NetOptionsBuilder {

        private int soSndBuf = -1;
        private int soRcvBuf = -1;
        private boolean soKeepAlive = true;
        private boolean soReuseAddr = false;
        private boolean tcpNoDelay = true;
        private int soLinger = -1;
        private int writeBufferHighWaterMark = -1;
        private int writeBufferLowWaterMark = -1;

        NetOptionsBuilder() {
        }

        public NetOptionsBuilder soSndBuf(int soSndBuf) {
            this.soSndBuf = soSndBuf;
            return this;
        }

        public NetOptionsBuilder soRcvBuf(int soRcvBuf) {
            this.soRcvBuf = soRcvBuf;
            return this;
        }

        public NetOptionsBuilder soKeepAlive(boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public NetOptionsBuilder soReuseAddr(boolean soReuseAddr) {
            this.soReuseAddr = soReuseAddr;
            return this;
        }

        public NetOptionsBuilder tcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        public NetOptionsBuilder soLinger(int soLinger) {
            this.soLinger = soLinger;
            return this;
        }

        public NetOptionsBuilder writeBufferHighWaterMark(int writeBufferHighWaterMark) {
            this.writeBufferHighWaterMark = writeBufferHighWaterMark;
            return this;
        }

        public NetOptionsBuilder writeBufferLowWaterMark(int writeBufferLowWaterMark) {
            this.writeBufferLowWaterMark = writeBufferLowWaterMark;
            return this;
        }

        public NetOptions build() {
            return new NetOptions(soSndBuf, soRcvBuf, soKeepAlive, soReuseAddr, tcpNoDelay, soLinger,
                    writeBufferHighWaterMark, writeBufferLowWaterMark);
        }

    }

}

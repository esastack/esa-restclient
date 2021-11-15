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
package io.esastack.httpclient.core.netty;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Http2ChunkedInputTest {

    @Test
    void testBasic() throws Exception {
        final File file = File.createTempFile("httpclient-", ".tmp");
        file.deleteOnExit();

        try {
            try (FileOutputStream out = new FileOutputStream(file)) {
                final byte[] data = new byte[1024 * 1024];
                ThreadLocalRandom.current().nextBytes(data);
                out.write(data);
            }

            final ChunkedFile origin = mock(ChunkedFile.class);
            final int streamId = 3;

            final Http2ChunkedInput wrapped = new Http2ChunkedInput(origin, streamId);
            verify(origin, never()).isEndOfInput();
            wrapped.isEndOfInput();
            verify(origin).isEndOfInput();

            final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
            when(ctx.alloc()).thenReturn(ByteBufAllocator.DEFAULT);
            verify(origin, never()).readChunk(ctx);
            wrapped.readChunk(ctx);
            verify(origin).readChunk(ByteBufAllocator.DEFAULT);

            verify(origin).readChunk(ByteBufAllocator.DEFAULT);
            wrapped.readChunk(ByteBufAllocator.DEFAULT);
            verify(origin, times(2)).readChunk(ByteBufAllocator.DEFAULT);

            verify(origin, never()).length();
            wrapped.length();
            verify(origin).length();

            verify(origin, never()).progress();
            wrapped.progress();
            verify(origin).progress();

            verify(origin, never()).close();
            wrapped.close();
            verify(origin).close();
        } finally {
            file.delete();
        }
    }
}

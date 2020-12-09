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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.stream.ChunkedInput;

final class Http2ChunkedInput implements ChunkedInput<Http2ChunkedInput.Content> {

    private final ChunkedInput<ByteBuf> input;
    private final int streamId;

    Http2ChunkedInput(ChunkedInput<ByteBuf> input,
                      int streamId) {
        this.input = input;
        this.streamId = streamId;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return input.isEndOfInput();
    }

    @Override
    public void close() throws Exception {
        input.close();
    }

    @Deprecated
    @Override
    public Content readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public Content readChunk(ByteBufAllocator allocator) throws Exception {
        ByteBuf buf = input.readChunk(allocator);
        if (buf == null) {
            return null;
        }

        if (input.isEndOfInput()) {
            return new Content(buf, streamId, true);
        }
        return new Content(buf, streamId, false);
    }

    @Override
    public long length() {
        return input.length();
    }

    @Override
    public long progress() {
        return input.progress();
    }

    static class Content extends DefaultHttpContent {

        final int streamId;
        final boolean endOfStream;

        Content(ByteBuf content,
                int streamId,
                boolean endOfStream) {
            super(content);
            this.streamId = streamId;
            this.endOfStream = endOfStream;
        }
    }
}

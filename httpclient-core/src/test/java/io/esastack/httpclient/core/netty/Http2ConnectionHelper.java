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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2LocalFlowController;
import io.netty.handler.codec.http2.Http2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.Http2StreamVisitor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Http2ConnectionHelper {

    static final int STREAM_ID = 3;

    EmbeddedChannel channel;

    @Mock
    private Http2Connection connection;

    @Mock
    private Http2Stream stream;

    @Mock
    private Http2ConnectionDecoder decoder;

    @Mock
    private Http2ConnectionEncoder encoder;

    @Mock
    private Http2FrameWriter frameWriter;

    @Mock
    private Http2RemoteFlowController remoteFlow;

    @Mock
    private Http2LocalFlowController localFlow;

    @Mock
    private Http2Connection.Endpoint<Http2RemoteFlowController> remote;

    @Mock
    private Http2RemoteFlowController remoteFlowController;

    @Mock
    private Http2Connection.Endpoint<Http2LocalFlowController> local;

    @Mock
    private Http2LocalFlowController localFlowController;

    void setUp() throws Http2Exception {
        HandleRegistry registry = new HandleRegistry(2, 1);
        setUp(registry);
        registry.put(mock(ResponseHandle.class));
    }

    void setUp(HandleRegistry registry) throws Http2Exception {
        MockitoAnnotations.initMocks(this);

        when(stream.open(anyBoolean())).thenReturn(stream);
        when(remote.flowController()).thenReturn(remoteFlowController);
        when(local.flowController()).thenReturn(localFlowController);

        when(connection.remote()).thenReturn(remote);
        when(connection.local()).thenReturn(local);
        when(connection.forEachActiveStream(any(Http2StreamVisitor.class)))
                .thenAnswer((Answer<Http2Stream>) in -> {
                    Http2StreamVisitor visitor = in.getArgument(0);
                    if (!visitor.visit(stream)) {
                        return stream;
                    }
                    return null;
                });
        when(connection.numActiveStreams()).thenReturn(1);
        when(connection.stream(STREAM_ID)).thenReturn(stream);
        when(connection.goAwaySent(anyInt(), anyLong(), any(ByteBuf.class))).thenReturn(true);

        when(encoder.connection()).thenReturn(connection);
        when(encoder.frameWriter()).thenReturn(frameWriter);
        when(encoder.flowController()).thenReturn(remoteFlow);
        when(encoder.writeSettings(any(ChannelHandlerContext.class),
                any(Http2Settings.class), any(ChannelPromise.class)))
                .thenAnswer(invocation -> {
                    ChannelPromise p = invocation.getArgument(2);
                    return p.setSuccess();
                });

        when(encoder.writeGoAway(any(ChannelHandlerContext.class),
                anyInt(), anyLong(), any(ByteBuf.class), any(ChannelPromise.class)))
                .thenAnswer(invocation -> {
                    ChannelPromise p = invocation.getArgument(4);
                    return p.setSuccess();
                });

        when(decoder.connection()).thenReturn(connection);
        when(decoder.flowController()).thenReturn(localFlow);

        when(frameWriter.writeGoAway(
                any(ChannelHandlerContext.class),
                anyInt(),
                anyLong(),
                any(ByteBuf.class),
                any(ChannelPromise.class)))
                .thenAnswer((Answer<ChannelFuture>) invocation -> {
                    ByteBuf buf = invocation.getArgument(3);
                    buf.release();
                    ChannelPromise p = invocation.getArgument(4);
                    return p.setSuccess();
                });

        channel = new EmbeddedChannel(new Http2ConnectionHandler(decoder,
                encoder,
                Http2Settings.defaultSettings(),
                false,
                registry));

        Helper.mockHeaderAndDataFrameWrite(encoder);
    }
}

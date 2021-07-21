package esa.restclient.codec;

import esa.commons.netty.core.Buffer;
import esa.httpclient.core.Handle;

import java.util.function.Consumer;

public interface SegmentDecoder {

    Handle onStart(Consumer<Void> h);

    Handle onData(Consumer<Buffer> h);

    Handle onEnd(Consumer<Void> h);
}

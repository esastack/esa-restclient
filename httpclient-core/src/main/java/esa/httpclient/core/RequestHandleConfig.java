package esa.httpclient.core;

import java.util.function.Consumer;

public interface RequestHandleConfig {

    RequestHandleConfig handle(Consumer<Handle> handle);

    RequestHandleConfig handler(Handler handler);
}

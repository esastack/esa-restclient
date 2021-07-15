package esa.httpclient.core.netty;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DelegatingResolverTest {

    @Test
    void testResolve() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
        DelegatingResolver resolver = new DelegatingResolver(
                GlobalEventExecutor.INSTANCE, inetHost -> CompletableFuture.completedFuture(inetAddress)
        );
        Promise<InetAddress> addressPromise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        resolver.doResolve("111", addressPromise);
        addressPromise.addListener((address) -> assertEquals(address.getNow(), inetAddress));

        assertThrows(UnsupportedOperationException.class, () -> resolver.doResolveAll(null, null));
    }

}

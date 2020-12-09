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
package esa.httpclient.core;

import esa.commons.logging.Logger;
import esa.commons.spi.SpiLoader;
import esa.httpclient.core.filter.FilterContext;
import esa.httpclient.core.util.LoggerUtils;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * The wrapper of {@link Listener} which holds many internal {@link #listeners} for execution.
 */
public class ListenerProxy implements Listener {

    private static final Logger logger = LoggerUtils.logger();

    public static final Listener DEFAULT;

    private final List<Listener> listeners;
    private final boolean listenersAbsent;

    static {
        List<Listener> ls = SpiLoader.getAll(Listener.class);
        if (ls == null || ls.isEmpty()) {
            DEFAULT = NoopListener.INSTANCE;
        } else {
            DEFAULT = new ListenerProxy(ls);
        }
    }

    public ListenerProxy(Listener delegate) {
        this.listeners = delegate == null
                ? Collections.emptyList() : Collections.singletonList(delegate);
        this.listenersAbsent = (delegate == null);
    }

    ListenerProxy(List<Listener> delegate) {
        this.listeners = delegate == null
                ? Collections.emptyList() : Collections.unmodifiableList(delegate);
        this.listenersAbsent = (delegate == null || delegate.isEmpty());
    }

    @Override
    public void onInterceptorsStart(HttpRequest request, Context ctx) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onInterceptorsStart(request, ctx);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onInterceptorsStart(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onInterceptorsEnd(HttpRequest request, Context ctx) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onInterceptorsEnd(request, ctx);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onInterceptorsEnd(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onFiltersStart(HttpRequest request, FilterContext ctx) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onFiltersStart(request, ctx);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onFiltersStart(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onFiltersEnd(HttpRequest request, Context ctx) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onFiltersEnd(request, ctx);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onFiltersEnd(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onConnectionPoolAttempt(HttpRequest request, Context ctx, SocketAddress address) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onConnectionPoolAttempt(request, ctx, address);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onConnectionPoolAttempt(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onConnectionPoolAcquired(HttpRequest request, Context ctx, SocketAddress address) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onConnectionPoolAcquired(request, ctx, address);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onConnectionPoolAcquired(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onAcquireConnectionPoolFailed(HttpRequest request, Context ctx, SocketAddress address,
                                              Throwable cause) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onAcquireConnectionPoolFailed(request, ctx, address, cause);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onAcquireConnectionPoolFailed(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onConnectionAttempt(HttpRequest request, Context ctx, SocketAddress address) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onConnectionAttempt(request, ctx, address);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onConnectionAttempt(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onConnectionAcquired(HttpRequest request, Context ctx, SocketAddress address) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onConnectionAcquired(request, ctx, address);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onConnectionAcquired(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onAcquireConnectionFailed(HttpRequest request, Context ctx, SocketAddress address, Throwable cause) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onAcquireConnectionFailed(request, ctx, address, cause);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onAcquireConnectionFailed(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onWriteAttempt(HttpRequest request, Context ctx, long readTimeout) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onWriteAttempt(request, ctx, readTimeout);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onWriteAttempt(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onWriteDone(HttpRequest request, Context ctx, long readTimeout) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onWriteDone(request, ctx, readTimeout);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onWriteDone(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onWriteFailed(HttpRequest request, Context ctx, Throwable cause) {
        if (!listenersAbsent) {
            for (Listener listener : listeners) {
                try {
                    listener.onWriteFailed(request, ctx, cause);
                } catch (Throwable th) {
                    logger.error("Failed to execute listener's onWriteFailed(), uri: {}",
                            request.uri(), th);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(HttpRequest request, Context ctx, HttpMessage message) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onMessageReceived(request, ctx, message);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onMessageReceived(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onCompleted(HttpRequest request, Context ctx, HttpResponse response) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onCompleted(request, ctx, response);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onCompleted(), uri: {}",
                        request.uri(), th);
            }
        }
    }

    @Override
    public void onError(HttpRequest request, Context ctx, Throwable cause) {
        if (listenersAbsent) {
            return;
        }

        for (Listener listener : listeners) {
            try {
                listener.onError(request, ctx, cause);
            } catch (Throwable th) {
                logger.error("Failed to execute listener's onError(), uri: {}",
                        request.uri(), th);
            }
        }
    }
}

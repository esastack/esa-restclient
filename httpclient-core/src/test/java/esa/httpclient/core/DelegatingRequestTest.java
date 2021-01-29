/*
 * Copyright 2021 OPPO ESA Stack Project
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

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DelegatingRequestTest {

    @Test
    void testBasic() {
        final HttpRequest underlying = mock(HttpRequest.class);
        final DelegatingRequest request = new DelegatingRequest(underlying);

        verify(underlying, never()).method();
        request.method();
        verify(underlying).method();

        verify(underlying, never()).scheme();
        request.scheme();
        verify(underlying).scheme();

        verify(underlying, never()).path();
        request.path();
        verify(underlying).path();

        verify(underlying, never()).uri();
        request.uri();
        verify(underlying).uri();

        verify(underlying, never()).addParam(anyString(), anyString());
        request.addParam("", "");
        verify(underlying).addParam(anyString(), anyString());

        verify(underlying, never()).getParam(anyString());
        request.getParam("");
        verify(underlying).getParam(anyString());

        verify(underlying, never()).getParams(anyString());
        request.getParams("");
        verify(underlying).getParams(anyString());

        verify(underlying, never()).paramNames();
        request.paramNames();
        verify(underlying).paramNames();

        verify(underlying, never()).headers();
        request.headers();
        verify(underlying).headers();

        verify(underlying, never()).addHeader(anyString(), anyString());
        request.addHeader("", "");
        verify(underlying).addHeader(anyString(), anyString());

        verify(underlying, never()).getHeader(anyString());
        request.getHeader("");
        verify(underlying).getHeader(anyString());

        verify(underlying, never()).setHeader(anyString(), anyString());
        request.setHeader("", "");
        verify(underlying).setHeader(anyString(), anyString());

        verify(underlying, never()).removeHeader(anyString());
        request.removeHeader("");
        verify(underlying).removeHeader(anyString());

        verify(underlying, never()).uriEncode();
        request.uriEncode();
        verify(underlying).uriEncode();

        verify(underlying, never()).readTimeout();
        request.readTimeout();
        verify(underlying).readTimeout();

        verify(underlying, never()).isSegmented();
        request.isSegmented();
        verify(underlying).isSegmented();

        verify(underlying, never()).isMultipart();
        request.isMultipart();
        verify(underlying).isMultipart();

        verify(underlying, never()).buffer();
        request.buffer();
        verify(underlying).buffer();

        verify(underlying, never()).file();
        request.file();
        verify(underlying).file();

        verify(underlying, never()).attrs();
        request.attrs();
        verify(underlying).attrs();

        verify(underlying, never()).files();
        request.files();
        verify(underlying).files();

        verify(underlying, never()).isFile();
        request.isFile();
        verify(underlying).isFile();

        verify(underlying, never()).multipartEncode();
        request.multipartEncode();
        verify(underlying).multipartEncode();
    }

}


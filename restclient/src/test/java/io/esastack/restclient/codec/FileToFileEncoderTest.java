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
package io.esastack.restclient.codec;

import io.esastack.restclient.RestRequestBase;
import io.esastack.restclient.codec.impl.EncodeChainImpl;
import io.esastack.restclient.codec.impl.FileEncoder;
import io.netty.handler.codec.CodecException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FileToFileEncoderTest {

    @Test
    void testEncode() throws Exception {
        FileEncoder fileEncoder = new FileEncoder();
        RestRequestBase request = mock(RestRequestBase.class);

        File file = new File("test");
        EncodeContext ctx = new EncodeChainImpl(
                request,
                file,
                String.class,
                String.class,
                mock(List.class),
                mock(List.class)
        );
        assertThrows(CodecException.class, () ->
                fileEncoder.encode(ctx));

        EncodeContext ctx1 = new EncodeChainImpl(
                request,
                file,
                File.class,
                File.class,
                mock(List.class),
                mock(List.class)
        );
        then(fileEncoder.encode(ctx1).value())
                .isEqualTo(file);
    }

}

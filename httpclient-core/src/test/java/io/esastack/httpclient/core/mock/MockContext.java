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
package io.esastack.httpclient.core.mock;

import io.esastack.httpclient.core.Context;

public class MockContext extends Context {

    @Override
    public void maxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    @Override
    public void maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public void useExpectContinue(boolean useExpectContinue) {
        this.useExpectContinue = useExpectContinue;
    }

    public void clear() {
        super.attributes.clear();
        useExpectContinue = true;
        maxRedirects = 0;
        maxRetries = 0;
    }
}


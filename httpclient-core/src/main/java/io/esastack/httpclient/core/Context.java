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
package io.esastack.httpclient.core;

import esa.commons.collection.AttributeMap;
import esa.commons.collection.Attributes;

public class Context {

    protected volatile int maxRedirects;
    protected volatile int maxRetries;
    protected volatile boolean useExpectContinue;
    private final Attributes attrs = new AttributeMap(8);

    public int maxRedirects() {
        return maxRedirects;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public boolean isUseExpectContinue() {
        return useExpectContinue;
    }

    protected void maxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    protected void maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    protected void useExpectContinue(boolean useExpectContinue) {
        this.useExpectContinue = useExpectContinue;
    }

    public Attributes attrs() {
        return attrs;
    }
}

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

public final class ContextNames {

    public static final String EXPECT_CONTINUE_ENABLED = "$expect.continue.enabled";
    public static final String MAX_RETRIES = "$maxRetries";
    public static final String MAX_REDIRECTS = "$maxRedirects";
    public static final String FILTER_CONTEXT = "$filterContext";

    private ContextNames() {
    }
}

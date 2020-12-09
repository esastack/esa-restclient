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
package esa.httpclient.core.metrics;

public interface IoThreadMetric {

    /**
     * Obtains the number of pending task
     *
     * @return pending task
     */
    int pendingTasks();

    /**
     * Obtains the max pending task
     *
     * @return max pending task
     */
    int maxPendingTasks();

    /**
     * Obtains the io ratio
     *
     * @return ratio
     */
    int ioRatio();

    /**
     * Obtains thread's name
     *
     * @return name
     */
    String name();

    /**
     * Obtains thread's priority
     *
     * @return priority
     */
    int priority();

    /**
     * Obtains thread's state
     *
     * @return state
     */
    String state();

}

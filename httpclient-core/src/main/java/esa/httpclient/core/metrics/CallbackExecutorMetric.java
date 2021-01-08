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

public interface CallbackExecutorMetric {

    /**
     * Obtains core size of current executor.
     *
     * @return core size
     */
    int coreSize();

    /**
     * Obtains max size of current executor.
     *
     * @return max size
     */
    int maxSize();

    /**
     * Obtains keep alive seconds
     *
     * @return keep alive seconds
     */
    long keepAliveSeconds();

    /**
     * Obtains active threads count
     *
     * @return active count
     */
    int activeCount();

    /**
     * Obtains pool size of current executor.
     *
     * @return pool size
     */
    int poolSize();

    /**
     * Obtains largest pool size of current executor.
     *
     * @return largest pool size
     */
    int largestPoolSize();

    /**
     * Obtains the count of task.
     *
     * @return the number of task
     */
    long taskCount();

    /**
     * Obtains the size of blocking queue.
     *
     * @return size of blocking queue
     */
    int queueSize();

    /**
     * Obtains the count of completed task.
     *
     * @return the number of completed task
     */
    long completedTaskCount();

    /**
     * Obtains the id of current executor.
     *
     * @return identify
     */
    String executorId();

}

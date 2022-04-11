/*
 * Copyright 2022 OPPO ESA Stack Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.esastack.restclient.ext.rule;

import esa.commons.StringUtils;
import io.esastack.httpclient.core.util.LoggerUtils;
import io.esastack.restclient.ext.spi.RedefineRulesSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileRulesSource implements RedefineRulesSource {

    private static final long AUTO_REFRESH_TIME_MS = 500L;
    private static final String DEFAULT_CONFIG_DIR = "./conf";
    public static final String REQUEST_REDEFINE_CONFIG_DIR = "request-redefine.config.dir";
    private static final String ABSOLUTE_PATH_PREFIX = "/";
    private static final String DEFAULT_CONFIG_NAME = "request-redefine.yaml";
    public static final String REQUEST_REDEFINE_CONFIG_NAME = "request-redefine.config.name";

    private volatile long lastModified = 0L;
    private final File configFile;
    private final Yaml yaml = new Yaml(new Constructor(RulesConfig.class));
    private volatile List<RedefineRule> rules;

    private final ScheduledThreadPoolExecutor scheduledRuleRefresher =
            new ScheduledThreadPoolExecutor(1, (r) -> {
                Thread thread = new Thread(r);
                thread.setName("ScheduledRuleRefresher-" + configPath() + "-task");
                thread.setDaemon(true);
                return thread;
            }, (r, executor) ->
                    LoggerUtils.logger().error("A ScheduledRuleRefresher-" + configPath() + "-task was rejected"));

    public FileRulesSource() {
        String configDir = configDir();
        String configName = configName();
        this.configFile = getConfig(configDir, configName);
        loadRules();
        initRuleRefreshTask();
    }

    private void initRuleRefreshTask() {
        String configPath = this.configFile.getPath();

        this.scheduledRuleRefresher.scheduleAtFixedRate(this::refreshRulesIfNeeded,
                AUTO_REFRESH_TIME_MS,
                AUTO_REFRESH_TIME_MS,
                TimeUnit.MILLISECONDS);

        LoggerUtils.logger().info("A scheduledRuleRefresher has been added to config: {} successfully",
                configPath);
    }

    @Override
    public List<RedefineRule> rules() {
        if (rules == null) {
            return Collections.emptyList();
        }
        return rules;
    }

    private void refreshRulesIfNeeded() {
        long currentModified = configFile.lastModified();
        if (currentModified != lastModified) {
            if (LoggerUtils.logger().isDebugEnabled()) {
                LoggerUtils.logger().debug("The " + configFile.getName() + " has updated!");
            }
            loadRules();
            this.lastModified = currentModified;
        }
    }

    private void loadRules() {
        try {
            RulesConfig config = yaml.load(new FileInputStream(configFile));
            if (config != null) {
                this.rules = config.build();
            }
        } catch (Throwable e) {
            LoggerUtils.logger().error("LoadRules error!", e);
        }
    }

    private File getConfig(String configDir, String configName) {
        try {
            File file = new File(configDir, configName);
            // Make sure the common file exists in the classpath, if not, just doCreate it.
            if (file.exists()) {
                return file;
            }

            if (file.createNewFile()) {
                LoggerUtils.logger().info("The file: {}  doesn't exist, a new one has been created", file.getName());
                return file;
            }
            throw new IllegalStateException("Failed to create file: " + file.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to get config file: "
                    + configDir + File.separator + configName, e);
        }
    }

    private static String configPath() {
        return configDir() + File.separator + configName();
    }

    private static String configDir() {
        // Default to ./conf
        String configDir = DEFAULT_CONFIG_DIR;

        final String systemDir = getFromEnvAndProp(REQUEST_REDEFINE_CONFIG_DIR);
        if (systemDir != null) {
            configDir = systemDir;
        }

        // Absolute path
        if (configDir.startsWith(ABSOLUTE_PATH_PREFIX)) {
            return configDir;
        }
        return System.getProperty("user.dir") + File.separator + configDir;
    }

    private static String configName() {
        String configName;
        if (StringUtils.isNotEmpty((configName = System.getenv(REQUEST_REDEFINE_CONFIG_NAME)))) {
            return configName;
        }

        if (StringUtils.isNotEmpty((configName = System.getProperty(REQUEST_REDEFINE_CONFIG_NAME)))) {
            return configName;
        }

        return DEFAULT_CONFIG_NAME;
    }

    private static String getFromEnvAndProp(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        final String envValue = System.getenv(key.replace(".", "_"));
        if (StringUtils.isNotBlank(envValue)) {
            return envValue;
        }

        return System.getProperty(key);
    }
}

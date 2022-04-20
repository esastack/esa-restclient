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

import io.esastack.httpclient.core.util.LoggerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RulesProvider {
    private boolean close;
    private List<RuleConfig> rules;

    public void setClose(boolean close) {
        this.close = close;
    }

    public void setRules(List<RuleConfig> rules) {
        this.rules = rules;
    }

    public List<TrafficSplitRule> get() {
        if (close || rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        List<TrafficSplitRule> ruleList = new ArrayList<>(8);
        for (RuleConfig rule : rules) {
            try {
                TrafficSplitRule tem = rule.build();
                if (tem != null) {
                    ruleList.add(tem);
                }
            } catch (Throwable e) {
                LoggerUtils.logger().error("Build RedefineRule error!ruleConfig:{}", rule, e);
            }
        }
        return ruleList;
    }

    @Override
    public String toString() {
        return "RulesConfig{" +
                "close=" + close +
                ", rules=" + rules +
                '}';
    }
}

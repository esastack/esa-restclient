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

import io.esastack.restclient.ext.action.TrafficSplitAction;
import io.esastack.restclient.ext.condition.TrafficSplitCondition;

import java.util.ArrayList;
import java.util.List;

public class RuleConfig {
    private String name;
    private String match;
    private List<TrafficSplitCondition> conditions;
    private ActionsConfig action;

    public void setName(String name) {
        this.name = name;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public void setConditions(List<ConditionConfig> conditions) {
        if (conditions != null && conditions.size() > 0) {
            this.conditions = new ArrayList<>(conditions.size());
            for (ConditionConfig config : conditions) {
                this.conditions.add(config.build());
            }
        }
    }

    public void setAction(ActionsConfig action) {
        this.action = action;
    }

    public TrafficSplitRule build() {
        TrafficSplitRule.MatchMechanism matchMechanism;
        if ("all".equalsIgnoreCase(match)) {
            matchMechanism = TrafficSplitRule.MatchMechanism.ALL;
        } else if ("any".equalsIgnoreCase(match)) {
            matchMechanism = TrafficSplitRule.MatchMechanism.ANY;
        } else if ("not".equalsIgnoreCase(match)) {
            matchMechanism = TrafficSplitRule.MatchMechanism.NOT;
        } else {
            throw new IllegalStateException("Illegal match:(" + match + ")");
        }

        return new RedefineRuleImpl(name,
                matchMechanism,
                conditions,
                action == null ? null : action.build());
    }

    private static final class RedefineRuleImpl implements TrafficSplitRule {

        private final String name;
        private final MatchMechanism match;
        private final List<TrafficSplitCondition> conditions;
        private final List<TrafficSplitAction> actions;

        public RedefineRuleImpl(String name,
                                MatchMechanism match,
                                List<TrafficSplitCondition> conditions,
                                List<TrafficSplitAction> actions) {
            this.name = name;
            this.match = match;
            this.conditions = conditions;
            this.actions = actions;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public MatchMechanism matchMechanism() {
            return match;
        }

        @Override
        public List<TrafficSplitCondition> conditions() {
            return conditions;
        }

        @Override
        public List<TrafficSplitAction> actions() {
            return actions;
        }
    }

    @Override
    public String toString() {
        return "RuleConfig{" +
                "name='" + name + '\'' +
                ", match='" + match + '\'' +
                ", conditions=" + conditions +
                ", action=" + action +
                '}';
    }
}

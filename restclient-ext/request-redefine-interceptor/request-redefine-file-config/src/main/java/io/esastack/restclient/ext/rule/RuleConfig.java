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

import io.esastack.restclient.ext.action.RequestRedefineAction;
import io.esastack.restclient.ext.condition.RequestRedefineCondition;

import java.util.List;

public class RuleConfig {
    private String name;
    private String match;
    private ConditionsConfig condition;
    private ActionsConfig action;

    public void setName(String name) {
        this.name = name;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public void setCondition(ConditionsConfig condition) {
        this.condition = condition;
    }

    public void setAction(ActionsConfig action) {
        this.action = action;
    }

    public RedefineRule build() {
        RedefineRule.MatchMechanism matchMechanism;
        if ("all".equalsIgnoreCase(match)) {
            matchMechanism = RedefineRule.MatchMechanism.ALL;
        } else if ("any".equalsIgnoreCase(match)) {
            matchMechanism = RedefineRule.MatchMechanism.ANY;
        } else if ("not".equalsIgnoreCase(match)) {
            matchMechanism = RedefineRule.MatchMechanism.NOT;
        } else {
            throw new IllegalStateException("Ill legal match:(" + match + ")");
        }

        return new RedefineRuleImpl(name,
                matchMechanism,
                condition == null ? null : condition.build(),
                action == null ? null : action.build());
    }

    private static final class RedefineRuleImpl implements RedefineRule {

        private String name;
        private MatchMechanism match;
        private List<RequestRedefineCondition> conditions;
        private List<RequestRedefineAction> actions;

        public RedefineRuleImpl(String name,
                                MatchMechanism match,
                                List<RequestRedefineCondition> conditions,
                                List<RequestRedefineAction> actions) {
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
        public List<RequestRedefineCondition> conditions() {
            return conditions;
        }

        @Override
        public List<RequestRedefineAction> actions() {
            return actions;
        }
    }

    @Override
    public String toString() {
        return "RuleConfig{" +
                "name='" + name + '\'' +
                ", match='" + match + '\'' +
                ", condition=" + condition +
                ", action=" + action +
                '}';
    }
}

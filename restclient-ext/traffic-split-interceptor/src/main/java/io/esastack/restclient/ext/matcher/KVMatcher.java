package io.esastack.restclient.ext.matcher;

import java.util.List;
import java.util.function.Function;

public class KVMatcher {
    private String name;
    private StringMatcher value;

    public KVMatcher() {
    }

    public MatchResult match(Function<String, String> kvSupplier) {
        if (name == null) {
            return MatchResult.success();
        }
        if (value == null) {
            if (kvSupplier.apply(name) != null) {
                return MatchResult.success();
            } else {
                return MatchResult.fail("There don't contain name:" + name);
            }
        }
        return value.match(kvSupplier.apply(name));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(StringMatcher value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public StringMatcher getValue() {
        return value;
    }

    static MatchResult multiMatch(Function<String, String> kvSupplier, List<KVMatcher> matchers) {
        if (matchers != null) {
            for (KVMatcher matcher : matchers) {
                MatchResult result = matcher.match(kvSupplier);
                if (!result.isMatch()) {
                    return result;
                }
            }
        }

        return MatchResult.success();
    }

    @Override
    public String toString() {
        return "KVMatcher{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}

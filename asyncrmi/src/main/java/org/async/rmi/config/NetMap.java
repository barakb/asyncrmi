package org.async.rmi.config;

import java.util.Collections;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/3/14.
 */
public class NetMap {
    private static final NetMap THE_EMPTY_NET_MAP = new NetMap(Collections.emptyList(), null);

    private List<Rule> rules;
    private ID id;

    @SuppressWarnings("UnusedDeclaration")
    public NetMap() {
    }

    public NetMap(List<Rule> rules, ID id) {
        this.rules = rules;
        this.id = id;
    }


    public List<Rule> getRules() {
        return rules;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static NetMap empty() {
        return THE_EMPTY_NET_MAP;
    }

    public ID getId() {
        return id;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void setId(ID id) {
        this.id = id;
    }
}



package org.async.rmi.config;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Barak Bar Orion
 * 12/12/14.
 */
public class Rule {
    private String match;
    private List<String> filters;
    private String auth;

    @SuppressWarnings("UnusedDeclaration")
    public Rule() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public Rule(String match, List<String> filters) {
        this.match = match;
        this.filters = filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getMatch() {
        return match;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMatch(String match) {
        this.match = match;
    }

    public List<String> getFilters() {
        return filters;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public boolean match(String hostName, String hostAddress) {
        Pattern pattern = Pattern.compile(match);
        Matcher m1 = pattern.matcher(hostName);
        Matcher m2 = pattern.matcher(hostAddress);
        return ((m1.find() || m2.find()));
    }


}

package org.async.rmi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Barak Bar Orion
 * 12/3/14.
 */
public class Netmap {
    private static Netmap theEmptyNetmap = new Netmap(Collections.emptyList());

    private final List<Rule> rules;

    public Netmap(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public static Netmap readNetMapFile(File file) throws IOException {
        return Util.readNetMapFile(file);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Netmap readNetMapStream(InputStream is){
        return Util.readNetMapStream(is);
    }
    public static Netmap readNetMapString(String content) throws IOException {
        return Util.readNetMapString(content);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Netmap empty() {
        return theEmptyNetmap;
    }

    public static class Rule {
        private final Match match;
        private final List<String> filters;

        public Rule(Match match, List<String> filters) {
            this.match = match;
            this.filters = filters;
        }

        public Match getMatch() {
            return match;
        }

        public List<String> getFilters() {
            return filters;
        }

        public static class Match {
            private final String host;

            public Match(String host) {
                this.host = host;
            }

            @SuppressWarnings("UnusedDeclaration")
            public String getHost() {
                return host;
            }

            public boolean match(String hostName, String hostAddress) {
                Pattern pattern =  Pattern.compile(host);
                Matcher m1 = pattern.matcher(hostName);
                Matcher m2 = pattern.matcher(hostAddress);
                return ((m1.find() || m2.find()));
            }
        }

    }

}



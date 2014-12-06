package org.async.rmi;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Barak Bar Orion
 * 12/3/14.
 */
public class ReadNetMapConfigurationTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(OneWayTest.class);
    @SuppressWarnings("FieldCanBeLocal")
    private final String NET_MAP_STRING = "---\n" +
            "rules:\n" +
            "    - match: .*\n" +
            "      filters: [encrypt, compress]\n" +
            "    - match: 192\\.168\\.2\\.106\n" +
            "      filters: [drop]\n" +
            "    - match: 192\\.168\\.2\\.106\n" +
            "      filters: [encrypt, compress]\n" +
            "...";

    @Test(timeout = 5000)
    public void testSlowOneWay() throws Exception {
        Netmap netmap = Netmap.readNetMapString(NET_MAP_STRING);
        assertThat(netmap.getRules().size(), is(3));
        Netmap.Rule rule = netmap.getRules().get(0);
        Netmap.Rule.Match match = rule.getMatch();
        List<String> filters = rule.getFilters();

        assertThat(match.getHost(), is(".*"));
        assertThat(filters.size(), is(2));
        assertThat(filters.get(0), is("encrypt"));
        assertThat(filters.get(1), is("compress"));
    }
}

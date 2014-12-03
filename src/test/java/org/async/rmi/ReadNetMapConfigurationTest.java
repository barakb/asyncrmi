package org.async.rmi;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

    @Test(timeout = 5000)
    public void testSlowOneWay() throws Exception {
        Netmap netmap = Util.readNetMapFile(new File("netmap.sample.yaml"));
        assertThat(netmap.getRules().size(), is(2));
        Netmap.Rule rule = netmap.getRules().get(0);
        Netmap.Rule.Match match = rule.getMatch();
        List<String> filters = rule.getFilters();

        assertThat(match.getHost(), is("192.168.2.106"));
        assertThat(match.getPort(), is("2021"));
        assertThat(filters.size(), is(1));
        assertThat(filters.get(0), is("drop"));
    }
}

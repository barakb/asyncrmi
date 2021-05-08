package org.async.rmi;

import org.async.rmi.config.*;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class UtilTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testReadFile() throws Exception {
        Map map = new HashMap<>();
        map.put("key", "foo");
        ID id = Util.read(map, new ID());
        assertThat("foo", equalTo(id.getKey().getName()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadListString() throws Exception {
        Map map = new HashMap<>();
        List filters = new ArrayList<>();
        filters.add("foo");
        map.put("filters", filters);
        Rule rule = Util.read(map, new Rule());
        assertThat(rule.getFilters(), hasItem(equalTo("foo")));
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testReadNonFinal() throws Exception {
        Map map = new HashMap<>();
        Map id = new HashMap<>();
        id.put("key", "foo");
        map.put("id", id);
        NetMap netMap = Util.read(map, new NetMap());
        assertThat(netMap.getId().getKey().getName(), equalTo("foo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReadListNonFinal() throws Exception {
        Map map = new HashMap<>();
        Map rule = new HashMap<>();
        rule.put("match", "foo");
        map.put("rules", Arrays.asList(rule));
        NetMap netMap = Util.read(map, new NetMap());
        assertThat(netMap.getRules().size(), is(1));
        assertThat(netMap.getRules().get(0).getMatch(), equalTo("foo"));
    }

    @Test
    public void testReadConfiguration() throws Exception {
        final String CONFIG = "---\n" +
                "configurePort: 1\n" +
                "clientConnectTimeout: \n" +
                "    time: 1\n" +
                "    unit: minutes\n" +
                "serverHostName: barak\n" +
                "netMap:\n" +
                "  rules:\n" +
                "    - match: .*\n" +
                "      filters: [encrypt]\n" +
                "      auth: client\n" +
                "\n" +
                "  id:\n" +
                "     key : src/main/keys/server.key\n" +
                "     key-password: password\n" +
                "     certificate: src/main/keys/server.pem\n" +
                "\n" +
                "  auth:\n" +
                "     - name: client\n" +
                "       certificate: src/main/keys/client.pem\n" +
                "...";
        Yaml yaml = new Yaml();
        try (InputStream is = new ByteArrayInputStream(CONFIG.getBytes("utf-8"))) {
            Map map = (Map) yaml.load(is);
            Configuration  config = PropertiesReader.read(map, new Configuration());
            assertThat(config.getConfigurePort(), is(1));
            assertThat(config.getClientConnectTimeout().getTime(), is(1L));
            assertThat(config.getClientConnectTimeout().getTimeUnit(), is(TimeUnit.MINUTES));
            assertThat(config.getServerHostName(), equalTo("barak"));
            assertThat(config.getNetMap(), is(notNullValue()));
            assertThat(config.getNetMap().getId().getKey().getName(), equalTo("server.key"));
            assertThat(config.getNetMap().getRules().size(), is(1));
            assertThat(config.getNetMap().getRules().get(0).getMatch(), equalTo(".*"));
            assertThat(config.getNetMap().getRules().get(0).getFilters(), hasItem(equalTo("encrypt")));
        }
    }


}
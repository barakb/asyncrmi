package org.async.rmi.netty;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FiltersTest {

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(FiltersTest.class);


    @Test
    public void testHasDrop() throws Exception {
        int filters = Filters.encode(Arrays.asList("drop", "compress", "encrypt"));
        assertThat(Filters.hasDrop(filters), is(true));
        filters = Filters.encode(Arrays.asList("compress", "encrypt"));
        assertThat(Filters.hasDrop(filters), is(false));
    }

    @Test
    public void testHasCompress() throws Exception {
        int filters = Filters.encode(Arrays.asList("drop", "compress", "encrypt"));
        assertThat(Filters.hasCompress(filters), is(true));
        filters = Filters.encode(Arrays.asList("drop", "encrypt"));
        assertThat(Filters.hasCompress(filters), is(false));
    }

    @Test
    public void testHasEncrypt() throws Exception {
        int filters = Filters.encode(Arrays.asList("drop", "compress", "encrypt"));
        assertThat(Filters.hasEncrypt(filters), is(true));
        filters = Filters.encode(Arrays.asList("compress", "drop"));
        assertThat(Filters.hasEncrypt(filters), is(false));
    }

    @Test
    public void testDecode() throws Exception {
        int filters = Filters.encode(Arrays.asList("drop", "compress", "encrypt"));
        assertThat(Filters.decode(filters).size(), is(3));
    }

}
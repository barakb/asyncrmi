package org.async.rmi.server;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class LoaderHandlerTest {


    @Test
    public void testGetClassAnnotation() throws Exception {
        System.setProperty("java.rmi.server.codebase", "http://localhost/foo");
        LoaderHandler.loadCodeBaseProperty();
        assertThat(LoaderHandler.getClassAnnotation(LoaderHandlerTest.class), is("http://localhost/foo"));
    }

    @Test
    public void testGetMultipleClassAnnotation() throws Exception {
        System.setProperty("java.rmi.server.codebase", " http://localhost/foo  http://localhost/foo ");
        LoaderHandler.loadCodeBaseProperty();
        assertThat(LoaderHandler.getClassAnnotation(LoaderHandlerTest.class), is("http://localhost/foo http://localhost/foo"));
    }

    @Test
    public void testGetClassAnnotationEmpty() throws Exception {
        System.clearProperty("java.rmi.server.codebase");
        LoaderHandler.loadCodeBaseProperty();
        assertThat(LoaderHandler.getClassAnnotation(LoaderHandlerTest.class), is(nullValue()));
    }


}
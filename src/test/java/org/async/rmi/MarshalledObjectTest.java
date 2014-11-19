package org.async.rmi;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by Barak Bar Orion
 * 29/10/14.
 */
public class MarshalledObjectTest {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger logger = LoggerFactory.getLogger(MarshalledObjectTest.class);

    @Test(timeout = 5000)
    public void writeReadPrimitive() throws Exception {
        int value = 5;
        MarshalledObject<Integer> mo1 = new MarshalledObject<>(value);
        MarshalledObject<Integer> mo2 = new MarshalledObject<>(5);
        MarshalledObject<Integer> mo3 = new MarshalledObject<>(4);
        assertThat(mo1, equalTo(mo2));
        assertThat(mo1, not(equalTo(mo3)));
        assertThat(mo1.get(), equalTo(5));
    }

    @Test(timeout = 5000)
    public void writeReadObject() throws Exception {
        Value value1 = new Value("value1");
        Value value2 = new Value("value1");
        Value value3 = new Value("value2");
        MarshalledObject<Value> mo1 = new MarshalledObject<>(value1);
        MarshalledObject<Value> mo2 = new MarshalledObject<>(value2);
        MarshalledObject<Value> mo3 = new MarshalledObject<>(value3);
        assertThat(mo1, equalTo(mo2));
        assertThat(mo1, not(equalTo(mo3)));
        assertThat(mo1.get(), equalTo(value1));
        assertThat(mo1.get().getName(), equalTo(value1.getName()));
    }

    private static class Value implements Serializable {
        private final String name;

        public Value(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Value value = (Value) o;

            return name.equals(value.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

}

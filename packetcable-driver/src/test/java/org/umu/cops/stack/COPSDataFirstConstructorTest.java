package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the first (default) constructor of the COPSData class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSHandle had been
 * released and my assumptions may be incorrect.
 */
public class COPSDataFirstConstructorTest {

    @Test
    public void defaultConstructor() {
        final COPSData data = new COPSData();
        Assert.assertEquals(null, data.getData());
        Assert.assertEquals(0, data.length());
        Assert.assertEquals("", data.str());
        Assert.assertTrue(data.equals(new COPSData()));
    }
}

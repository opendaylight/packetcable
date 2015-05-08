package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the first constructor of the COPSHandle class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSHandle had been
 * released and my assumptions may be incorrect.
 */
public class COPSHandleFirstConstructorTest {

    @Test (expected = IllegalArgumentException.class)
    public void constructWithNullId() {
        final COPSData id = null;
        new COPSHandle(id);
    }

    @Test
    public void constructWithDefaultId() {
        final COPSData id = new COPSData();
        final COPSHandle handle = new COPSHandle(id);
        Assert.assertEquals(0, handle.getDataLength());
        Assert.assertTrue(id.equals(handle.getId()));

        final COPSHandle eqHash = new COPSHandle(id);
        Assert.assertTrue(handle.equals(eqHash));
        Assert.assertEquals(handle.hashCode(), eqHash.hashCode());
    }

    @Test
    public void constructWithData() {
        final COPSData id = new COPSData("12345");
        final COPSHandle handle = new COPSHandle(id);

        // TODO - need to determine if 12 is indeed correct given the value "123456778"
        Assert.assertEquals(8, handle.getDataLength());
        Assert.assertTrue(id.equals(handle.getId()));

        final COPSHandle eqHash = new COPSHandle(id);
        Assert.assertTrue(handle.equals(eqHash));
        Assert.assertEquals(handle.hashCode(), eqHash.hashCode());
    }

}

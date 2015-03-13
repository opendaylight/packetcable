package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the second constructor of the COPSHandle class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSHandle had been
 * released and my assumptions may be incorrect.
 */
public class COPSHandleSecondConstructorTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullBytes() {
        final byte[] bytes = null;
        new COPSHandle(bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyBytes() {
        new COPSHandle(new byte[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneByte() {
        new COPSHandle(new byte[]{(byte)1});
    }

//    @Test
    // TODO - Determine what values this byte array should contain??? As written, an exception is thrown in COPSData
    // TODO - when attempting to set the _len attribute.
    public void fourBytes() {
        final byte[] bytes = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5};
        final COPSHandle handle = new COPSHandle(bytes);
        Assert.assertEquals(4, handle.getDataLength());
        Assert.fail("Implement me");
    }

    // TODO - implement more tests once we can determine how exactly to properly instantiate a COPSHandle object

}

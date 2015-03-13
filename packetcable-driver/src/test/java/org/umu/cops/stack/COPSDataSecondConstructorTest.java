package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests for the second constructor of the COPSData class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSHandle had been
 * released and my assumptions may be incorrect.
 */
public class COPSDataSecondConstructorTest {

    final byte[] bytes = ("hello world").getBytes();

    @Test(expected = IllegalArgumentException.class)
    public void nullBytes() {
        new COPSData(null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyBytesWithNegOffsetZeroLength() {
        new COPSData(new byte[]{}, -1, 0);
    }

    @Test
    public void emptyBytesWithZeroOffsetZeroLength() {
        final COPSData data = new COPSData(new byte[]{}, 0, 0);
        Assert.assertEquals(0, data.length());
        Assert.assertEquals(0, data.getData().length);
        Assert.assertEquals("", data.str());

        final COPSData eqHash = new COPSData(bytes, 1, 0);
        final COPSData neHash = new COPSData(bytes, 2, 2);

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyBytesWithPosOffsetZeroLength() {
        new COPSData(new byte[]{}, 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validBytesWithNegOffsetZeroLength() {
        new COPSData(bytes, -1, 0);
    }

    @Test
    public void validBytesWithZeroOffsetZeroLength() {
        final COPSData data = new COPSData(bytes, 0, 0);
        Assert.assertEquals(0, data.length());
        Assert.assertEquals(0, data.getData().length);
        Assert.assertEquals("", data.str());

        final COPSData eqHash = new COPSData(bytes, 1, 0);
        final COPSData neHash = new COPSData(bytes, 2, 2);

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validBytesWithPosOffsetNegLength() {
        new COPSData(bytes, 1, -1);
    }

    @Test
    public void validBytesWithPosOffsetZeroLength() {
        final COPSData data = new COPSData(bytes, 1, 0);
        Assert.assertEquals(0, data.length());
        Assert.assertEquals(0, data.getData().length);
        Assert.assertEquals("", data.str());

        final COPSData eqHash = new COPSData(bytes, 1, 0);
        final COPSData neHash = new COPSData(bytes, 2, 2);

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }

    @Test
    public void validBytesWithPosOffsetPosLength() {
        final COPSData data = new COPSData(bytes, 1, 1);
        Assert.assertEquals(1, data.length());
        Assert.assertEquals(1, data.getData().length);
        Assert.assertEquals(bytes[1], data.getData()[0]);
        Assert.assertEquals("e", data.str());

        final COPSData eqHash = new COPSData(bytes, 1, 1);
        final COPSData neHash = new COPSData(bytes, 2, 2);

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validBytesWithPosOffsetLengthTooLarge() {
        new COPSData(bytes, 5, 8);
    }

    @Test
    public void likelyScenario() {
        final String expectedData = "lo wo";

        final COPSData data = new COPSData(bytes, 3, 5);
        Assert.assertTrue(Arrays.equals(expectedData.getBytes(), data.getData()));
        Assert.assertEquals(expectedData.length(), data.length());
        Assert.assertEquals(expectedData, data.str());

        final COPSData eqHash = new COPSData(bytes, 3, 5);
        final COPSData neHash = new COPSData(bytes, 2, 4);

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }
}

package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests for the third constructor of the COPSData class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSHandle had been
 * released and my assumptions may be incorrect.
 */
public class COPSDataThirdConstructorTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullString() {
        new COPSData(null);
    }

    @Test
    public void emptyString() {
        final COPSData data = new COPSData("");
        Assert.assertEquals(0, data.length());
        Assert.assertEquals(0, data.getData().length);
        Assert.assertEquals("", data.str());

        final COPSData eqHash = new COPSData("");
        final COPSData neHash = new COPSData("foo");

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }

    @Test
    public void likelyScenario() {
        final String theData = "Hello World";

        final COPSData data = new COPSData(theData);
        Assert.assertTrue(Arrays.equals(theData.getBytes(), data.getData()));
        Assert.assertEquals(theData.length(), data.length());
        Assert.assertEquals(theData, data.str());

        final COPSData eqHash = new COPSData(theData);
        final COPSData neHash = new COPSData("foo");

        Assert.assertTrue(data.equals(eqHash));
        Assert.assertEquals(data.hashCode(), eqHash.hashCode());
        Assert.assertFalse(data.equals(neHash));
        Assert.assertNotEquals(data.hashCode(), neHash.hashCode());
    }
}

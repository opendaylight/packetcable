package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for the proper construction of the COPSObjHeader class as well as changes to the transient state.
 */
public class COPSObjHeaderTest {

    @Test
    public void defaultConstructor() {
        final COPSObjHeader header = new COPSObjHeader();
        Assert.assertEquals(0, header.getCNum());
        Assert.assertEquals(0, header.getCType());
        Assert.assertEquals(4, header.getDataLength());

        final COPSObjHeader eqHash = new COPSObjHeader();
        Assert.assertTrue(header.equals(eqHash));
        Assert.assertEquals(header.hashCode(), eqHash.hashCode());
    }

    @Test
    public void settersWithDefaultConstructor() {
        final COPSObjHeader header = new COPSObjHeader();
        header.setDataLength((short)5);
        header.setCNum((byte) 8);
        header.setCType((byte) 7);
        Assert.assertEquals(9, header.getDataLength());
        Assert.assertEquals(8, header.getCNum());
        Assert.assertEquals(7, header.getCType());

        final COPSObjHeader eqHash = new COPSObjHeader((byte)8, (byte)7);
        eqHash.setDataLength((short)5);
        Assert.assertTrue(header.equals(eqHash));
        Assert.assertEquals(header.hashCode(), eqHash.hashCode());
    }

    @Test
    public void cNumTypeConstructor() {
        final COPSObjHeader header = new COPSObjHeader((byte)5, (byte)6);
        Assert.assertEquals(5, header.getCNum());
        Assert.assertEquals(6, header.getCType());
        Assert.assertEquals(4, header.getDataLength());

        final COPSObjHeader eqHash = new COPSObjHeader((byte)5, (byte)6);
        Assert.assertTrue(header.equals(eqHash));
        Assert.assertEquals(header.hashCode(), eqHash.hashCode());

        header.setCNum((byte)7);
        header.setCType((byte)8);
        Assert.assertEquals(7, header.getCNum());
        Assert.assertEquals(8, header.getCType());
    }
}

package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * Test for the proper construction of the COPSObjHeader class as well as changes to the transient state.
 */
public class COPSObjHeaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullCNum() {
        new COPSObjHeader(null, CType.DEF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullCType() {
        new COPSObjHeader(CNum.ACCT_TIMER, null);
    }

    @Test
    public void validConstruction() {
        final COPSObjHeader header = new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF);
        Assert.assertEquals(CNum.ACCT_TIMER, header.getCNum());
        Assert.assertEquals(CType.DEF, header.getCType());
        Assert.assertEquals(4, header.getHdrLength());

        final COPSObjHeader eqHash = new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF);
        Assert.assertTrue(header.equals(eqHash));
        Assert.assertEquals(header.hashCode(), eqHash.hashCode());
    }

}

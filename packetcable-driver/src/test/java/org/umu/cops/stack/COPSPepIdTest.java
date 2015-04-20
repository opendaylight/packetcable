package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSPepId class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSPepIdTest {


    @Test(expected = IllegalArgumentException.class)
    public void nullData() {
        new COPSPepId(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSPepId(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSPepId(new COPSObjHeader(CNum.PEPID, CType.CSI), new COPSData());
    }

    @Test
    public void emptyData() throws Exception {
        final COPSPepId pepId = new COPSPepId(new COPSData());
        Assert.assertEquals(CNum.PEPID, pepId.getHeader().getCNum());
        Assert.assertEquals(new COPSData(), pepId.getData());
        Assert.assertEquals(0, pepId.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        pepId.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**PEP-id**", lines[0]);
        Assert.assertEquals("C-num: PEPID", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("PEPID: ", lines[3]);
    }

    @Test
    public void unpaddedData() throws Exception {
        final COPSPepId pepId = new COPSPepId(new COPSData("1234"));
        Assert.assertEquals(CNum.PEPID, pepId.getHeader().getCNum());
        Assert.assertEquals(new COPSData("1234"), pepId.getData());
        Assert.assertEquals(4, pepId.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        pepId.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**PEP-id**", lines[0]);
        Assert.assertEquals("C-num: PEPID", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("PEPID: 1234", lines[3]);
    }

    @Test
    public void paddedData() throws Exception {
        final COPSPepId pepId = new COPSPepId(new COPSData("12345"));
        Assert.assertEquals(CNum.PEPID, pepId.getHeader().getCNum());
        Assert.assertEquals(new COPSData("12345"), pepId.getData());
        Assert.assertEquals(8, pepId.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        pepId.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**PEP-id**", lines[0]);
        Assert.assertEquals("C-num: PEPID", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("PEPID: 12345", lines[3]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSClientSI class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSClientSITest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidCSItype() {
        new COPSClientSI(CSIType.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullData() {
        new COPSClientSI(CSIType.SIGNALED, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSClientSI(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSClientSI(new COPSObjHeader(CNum.ACCT_TIMER, CType.CSI), new COPSData());
    }

    @Test
    public void csiSignaledEmptyData() {
        final COPSClientSI clientSI = new COPSClientSI(CSIType.SIGNALED, new COPSData());
        Assert.assertEquals(CSIType.SIGNALED, clientSI.getCsiType());
        Assert.assertEquals(CNum.CSI, clientSI.getHeader().getCNum());
        Assert.assertEquals(CSIType.SIGNALED.ordinal(), clientSI.getHeader().getCType().ordinal());
        Assert.assertEquals(new COPSData(), clientSI.getData());
        Assert.assertEquals(0, clientSI.getDataLength());
    }

    @Test
    public void csiSignaledUnPaddedData() {
        final COPSClientSI clientSI = new COPSClientSI(CSIType.SIGNALED, new COPSData("1234"));
        Assert.assertEquals(CSIType.SIGNALED, clientSI.getCsiType());
        Assert.assertEquals(CNum.CSI, clientSI.getHeader().getCNum());
        Assert.assertEquals(CSIType.SIGNALED.ordinal(), clientSI.getHeader().getCType().ordinal());
        Assert.assertEquals(new COPSData("1234"), clientSI.getData());
        Assert.assertEquals(4, clientSI.getDataLength());
    }

    @Test
    public void csiSignaledPaddedData() {
        final COPSClientSI clientSI = new COPSClientSI(CSIType.SIGNALED, new COPSData("12345"));
        Assert.assertEquals(CSIType.SIGNALED, clientSI.getCsiType());
        Assert.assertEquals(CNum.CSI, clientSI.getHeader().getCNum());
        Assert.assertEquals(CSIType.SIGNALED.ordinal(), clientSI.getHeader().getCType().ordinal());
        Assert.assertEquals(new COPSData("12345"), clientSI.getData());
        Assert.assertEquals(8, clientSI.getDataLength());
    }

    @Test
    public void csiNamedPaddedData() {
        final COPSClientSI clientSI = new COPSClientSI(CSIType.NAMED, new COPSData("12345"));
        Assert.assertEquals(CSIType.NAMED, clientSI.getCsiType());
        Assert.assertEquals(CNum.CSI, clientSI.getHeader().getCNum());
        Assert.assertEquals(CSIType.NAMED.ordinal(), clientSI.getHeader().getCType().ordinal());
        Assert.assertEquals(new COPSData("12345"), clientSI.getData());
        Assert.assertEquals(8, clientSI.getDataLength());
    }

    @Test
    public void csiNamedDumpPadded() throws Exception {
        final COPSClientSI clientSI = new COPSClientSI(CSIType.NAMED, new COPSData("12345"));
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        clientSI.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Client-SI**", lines[0]);
        Assert.assertEquals("C-num: CSI", lines[1]);
        Assert.assertEquals("C-type: STATELESS", lines[2]);
        Assert.assertEquals("CSI-type: NAMED", lines[3]);
        Assert.assertEquals("client-SI: 12345", lines[4]);
    }

    @Test
    public void csiSignaledDumpUnpadded() throws Exception {
        final COPSClientSI clientSI = new COPSClientSI(CSIType.SIGNALED, new COPSData("1234"));
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        clientSI.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Client-SI**", lines[0]);
        Assert.assertEquals("C-num: CSI", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("CSI-type: SIGNALED", lines[3]);
        Assert.assertEquals("client-SI: 1234", lines[4]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

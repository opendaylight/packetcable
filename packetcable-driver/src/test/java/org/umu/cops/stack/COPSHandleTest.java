package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSHandle class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSHandle had been
 * released and my assumptions may be incorrect.
 */
public class COPSHandleTest {

    private final static COPSObjHeader defaultHeader = new COPSObjHeader(CNum.HANDLE, CType.DEF);

    @Test (expected = IllegalArgumentException.class)
    public void constructWithNullId() {
        final COPSData id = null;
        new COPSHandle(id);
    }

    @Test
    public void constructWithDefaultId() {
        final COPSData id = new COPSData();
        final COPSHandle handle = new COPSHandle(id);

        Assert.assertEquals(defaultHeader, handle.getHeader());
        Assert.assertEquals(0, handle.getDataLength());
        Assert.assertTrue(id.equals(handle.getId()));

        final COPSHandle eqHash = new COPSHandle(id);
        Assert.assertTrue(handle.equals(eqHash));
        Assert.assertEquals(handle.hashCode(), eqHash.hashCode());
    }

    @Test
    public void constructWithDataNoPadding() {
        final COPSData id = new COPSData("12345678");
        final COPSHandle handle = new COPSHandle(id);

        Assert.assertEquals(defaultHeader, handle.getHeader());
        Assert.assertEquals(8, handle.getDataLength());
        Assert.assertTrue(id.equals(handle.getId()));

        final COPSHandle eqHash = new COPSHandle(id);
        Assert.assertTrue(handle.equals(eqHash));
        Assert.assertEquals(handle.hashCode(), eqHash.hashCode());
    }

    @Test
    public void constructWithDataWithPadding() {
        final COPSData id = new COPSData("123456789");
        final COPSHandle handle = new COPSHandle(id);

        Assert.assertEquals(defaultHeader, handle.getHeader());
        Assert.assertEquals(12, handle.getDataLength());
        Assert.assertTrue(id.equals(handle.getId()));

        final COPSHandle eqHash = new COPSHandle(id);
        Assert.assertTrue(handle.equals(eqHash));
        Assert.assertEquals(handle.hashCode(), eqHash.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullHeader() {
        new COPSHandle(null, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithHeaderInvalidCNum() {
        new COPSHandle(new COPSObjHeader(CNum.KA, CType.DEF), new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithHeaderInvalidCType() {
        new COPSHandle(new COPSObjHeader(CNum.HANDLE, CType.REPL), new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullData() {
        new COPSHandle(new COPSObjHeader(CNum.HANDLE, CType.DEF), null);
    }

    @Test
    public void constructWithHeaderValidCTypeAndDump() throws Exception {
        final COPSHandle handle = new COPSHandle(new COPSObjHeader(CNum.HANDLE, CType.DEF), new COPSData());
        Assert.assertEquals(new COPSObjHeader(CNum.HANDLE, CType.DEF), handle.getHeader());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        handle.dump(os);
        final String out = new String(os.toByteArray());
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Client-handle**", lines[0]);
        Assert.assertEquals("C-num: HANDLE", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("client-handle: ", lines[3]);
        System.out.println(out);
        os.close();
    }

    @Test
    public void dumpNoPadding() throws Exception {
        final COPSHandle handle = new COPSHandle(new COPSData("12345678"));

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        handle.dump(os);
        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Client-handle**", lines[0]);
        Assert.assertEquals("C-num: HANDLE", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("client-handle: 12345678", lines[3]);
        os.close();
    }

    @Test
    public void dumpWithPadding() throws Exception {
        final COPSData id = new COPSData("123456789");
        final COPSHandle handle = new COPSHandle(id);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        handle.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Client-handle**", lines[0]);
        Assert.assertEquals("C-num: HANDLE", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("client-handle: 123456789", lines[3]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

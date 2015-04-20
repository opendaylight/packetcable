package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSKATimer class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSKATimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSKATimerTest {

    private final static COPSObjHeader defaultHeader = new COPSObjHeader(CNum.KA, CType.DEF);

    @Test
    public void testValidTimeAndDefaultHeader() {
        final COPSKATimer timer = new COPSKATimer((short)10);
        Assert.assertEquals((short)10, timer.getTimerVal());
        Assert.assertEquals(defaultHeader, timer.getHeader());
    }

    @Test
    public void testValidTimeReservedAndDefaultHeader() {
        final COPSKATimer timer = new COPSKATimer((short) 4, (short)10);
        Assert.assertEquals((short)10, timer.getTimerVal());
        Assert.assertEquals(defaultHeader, timer.getHeader());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCNum() {
        new COPSKATimer(new COPSObjHeader(CNum.CONTEXT, CType.DEF), (short) 4, (short)10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCType() {
        new COPSKATimer(new COPSObjHeader(CNum.KA, CType.REPL), (short) 4, (short)10);
    }

    @Test
    public void testValidCustomHeader() {
        final COPSKATimer timer = new COPSKATimer(new COPSObjHeader(CNum.KA, CType.DEF), (short) 4, (short)10);
        Assert.assertEquals((short)10, timer.getTimerVal());
        Assert.assertEquals(new COPSObjHeader(CNum.KA, CType.DEF), timer.getHeader());
    }

    @Test
    public void testDumpDefaultHeader() throws Exception {
        final COPSKATimer timer = new COPSKATimer((short) 4, (short)10);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        timer.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**KA-timer**", lines[0]);
        Assert.assertEquals("C-num: KA", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Timer val: 10", lines[3]);
    }

    @Test
    public void testDumpCustomHeader() throws Exception {
        final COPSKATimer timer = new COPSKATimer(new COPSObjHeader(CNum.KA, CType.DEF),
                (short) 4, (short)100);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        timer.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**KA-timer**", lines[0]);
        Assert.assertEquals("C-num: KA", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Timer val: 100", lines[3]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

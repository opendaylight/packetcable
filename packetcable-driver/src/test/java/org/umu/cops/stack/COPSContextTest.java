package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSContext class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullRType() {
        new COPSContext(null, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSContext(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), RType.CONFIG, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSContext(new COPSObjHeader(CNum.CONTEXT, CType.NA), RType.CONFIG, (short)0);
    }

    @Test
    public void dumpConfig0() throws Exception {
        final COPSContext context = new COPSContext(RType.CONFIG, (short)0);
        Assert.assertEquals(4, context.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        context.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Context**", lines[0]);
        Assert.assertEquals("C-num: CONTEXT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("context: Configuration,0", lines[3]);
    }

    @Test
    public void dumpInAdmin99() throws Exception {
        final COPSContext context = new COPSContext(RType.IN_ADMIN, (short)99);
        Assert.assertEquals(4, context.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        context.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Context**", lines[0]);
        Assert.assertEquals("C-num: CONTEXT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("context: Incoming Message/Admission Control,99", lines[3]);
    }

    @Test
    public void dumpResAlloc25() throws Exception {
        final COPSContext context = new COPSContext(RType.RES_ALLOC, (short)25);
        Assert.assertEquals(4, context.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        context.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Context**", lines[0]);
        Assert.assertEquals("C-num: CONTEXT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("context: Resource allocation,25", lines[3]);
    }

    @Test
    public void dumpOut15() throws Exception {
        final COPSContext context = new COPSContext(RType.OUT, (short)15);
        Assert.assertEquals(4, context.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        context.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Context**", lines[0]);
        Assert.assertEquals("C-num: CONTEXT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("context: Outgoing message,15", lines[3]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

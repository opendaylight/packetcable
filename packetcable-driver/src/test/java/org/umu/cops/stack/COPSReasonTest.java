package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSReason.ReasonCode;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSReason class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSReasonTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullReasonCode() {
        new COPSReason(null, ReasonCode.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidReasonCode() {
        new COPSReason(ReasonCode.NA, ReasonCode.MALFORMED_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSubCode() {
        new COPSReason(ReasonCode.MANAGEMENT, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSReason(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), ReasonCode.ROUTE_CHANGE, ReasonCode.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSReason(new COPSObjHeader(CNum.REASON_CODE, CType.CSI), ReasonCode.PREEMPTED, ReasonCode.SYNC_HANDLE);
    }

    @Test
    public void valid1() throws Exception {
        final COPSReason reason = new COPSReason(ReasonCode.TRANS_HANDLE, ReasonCode.NA);
        Assert.assertEquals(ReasonCode.TRANS_HANDLE, reason.getReasonCode());
        Assert.assertEquals(ReasonCode.NA, reason.getReasonSubCode());
        Assert.assertEquals(4, reason.getDataLength());
        Assert.assertEquals("Transient handle.:", reason.getDescription());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        reason.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Reason**", lines[0]);
        Assert.assertEquals("C-num: REASON_CODE", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Reason Code: TRANS_HANDLE", lines[3]);
        Assert.assertEquals("Reason Sub Code: NA", lines[4]);
    }

    @Test
    public void valid2() throws Exception {
        final COPSReason reason = new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.TEAR);
        Assert.assertEquals(ReasonCode.INSUFF_RESOURCES, reason.getReasonCode());
        Assert.assertEquals(ReasonCode.TEAR, reason.getReasonSubCode());
        Assert.assertEquals(4, reason.getDataLength());
        Assert.assertEquals("Insufficient Resources.:", reason.getDescription());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        reason.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Reason**", lines[0]);
        Assert.assertEquals("C-num: REASON_CODE", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Reason Code: INSUFF_RESOURCES", lines[3]);
        Assert.assertEquals("Reason Sub Code: TEAR", lines[4]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

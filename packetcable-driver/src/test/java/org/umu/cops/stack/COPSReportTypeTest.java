package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSReportType class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSReportTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullReportType() {
        new COPSReportType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidReportType() {
        new COPSReportType(ReportType.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSReportType(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), ReportType.ACCOUNTING, (short)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSReportType(new COPSObjHeader(CNum.RPT, CType.CSI), ReportType.FAILURE, (short)0);
    }

    @Test
    public void validSuccessRpt() throws Exception {
        final COPSReportType reason = new COPSReportType(ReportType.SUCCESS);
        Assert.assertEquals(ReportType.SUCCESS, reason.getReportType());
        Assert.assertEquals(4, reason.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        reason.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Report**", lines[0]);
        Assert.assertEquals("C-num: RPT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Report: Success.", lines[3]);
    }

    @Test
    public void validFailureRpt() throws Exception {
        final COPSReportType reason = new COPSReportType(ReportType.FAILURE);
        Assert.assertEquals(ReportType.FAILURE, reason.getReportType());
        Assert.assertEquals(4, reason.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        reason.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Report**", lines[0]);
        Assert.assertEquals("C-num: RPT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Report: Failure.", lines[3]);
    }

    @Test
    public void validAccountingRpt() throws Exception {
        final COPSReportType reason = new COPSReportType(ReportType.ACCOUNTING);
        Assert.assertEquals(ReportType.ACCOUNTING, reason.getReportType());
        Assert.assertEquals(4, reason.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        reason.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertEquals("**Report**", lines[0]);
        Assert.assertEquals("C-num: RPT", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Report: Accounting.", lines[3]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

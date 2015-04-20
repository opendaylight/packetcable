package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSError class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSErrorTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullErrorCode() {
        new COPSError(null, ErrorTypes.AUTH_FAILURE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidErrorCode() {
        new COPSError(ErrorTypes.NA, ErrorTypes.AUTH_FAILURE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullErrorSubCode() {
        new COPSError(ErrorTypes.AUTH_FAILURE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSError(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), ErrorTypes.AUTH_FAILURE, ErrorTypes.BAD_HANDLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSError(new COPSObjHeader(CNum.ERROR, CType.CSI), ErrorTypes.NA, ErrorTypes.BAD_HANDLE);
    }

    @Test
    public void valid1() throws Exception {
        final COPSError error = new COPSError(ErrorTypes.BAD_HANDLE, ErrorTypes.NA);
        Assert.assertEquals(ErrorTypes.BAD_HANDLE, error.getErrCode());
        Assert.assertEquals(ErrorTypes.NA, error.getErrSubCode());
        Assert.assertEquals(4, error.getDataLength());
        Assert.assertEquals("Bad handle.:", error.getDescription());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        error.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Error**", lines[0]);
        Assert.assertEquals("C-num: ERROR", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Error Code: BAD_HANDLE", lines[3]);
        Assert.assertEquals("Error Sub Code: NA", lines[4]);
    }

    @Test
    public void valid2() throws Exception {
        final COPSError error = new COPSError(ErrorTypes.AUTH_REQUIRED, ErrorTypes.FAIL_PROCESS);
        Assert.assertEquals(ErrorTypes.AUTH_REQUIRED, error.getErrCode());
        Assert.assertEquals(ErrorTypes.FAIL_PROCESS, error.getErrSubCode());
        Assert.assertEquals(4, error.getDataLength());
        Assert.assertEquals("Authentication required.:", error.getDescription());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        error.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Error**", lines[0]);
        Assert.assertEquals("C-num: ERROR", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Error Code: AUTH_REQUIRED", lines[3]);
        Assert.assertEquals("Error Sub Code: FAIL_PROCESS", lines[4]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

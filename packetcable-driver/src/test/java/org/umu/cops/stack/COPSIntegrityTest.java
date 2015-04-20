package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSError class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSIntegrityTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidCNum() {
        new COPSIntegrity(new COPSObjHeader(CNum.NA, CType.DEF), 0, 0, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCType() {
        new COPSIntegrity(new COPSObjHeader(CNum.MSG_INTEGRITY, CType.NA), 0, 0, new COPSData());
    }

    @Test
    public void defaultConstructor() throws Exception {
        final COPSIntegrity integrity = new COPSIntegrity();
        Assert.assertEquals(0, integrity.getKeyId());
        Assert.assertEquals(0, integrity.getSeqNum());
        Assert.assertEquals(new COPSData(), integrity.getKeyDigest());
        Assert.assertEquals(8, integrity.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        integrity.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Message-Integrity**", lines[0]);
        Assert.assertEquals("C-num: MSG_INTEGRITY", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Key Id: 0", lines[3]);
        Assert.assertEquals("Sequence: 0", lines[4]);
        Assert.assertEquals("Key digest: ", lines[5]);
    }

    @Test
    public void nullData() {
        final COPSIntegrity integrity = new COPSIntegrity(new COPSObjHeader(CNum.MSG_INTEGRITY, CType.DEF), 1, 2, null);
        Assert.assertEquals(1, integrity.getKeyId());
        Assert.assertEquals(2, integrity.getSeqNum());
        Assert.assertEquals(new COPSData(), integrity.getKeyDigest());
    }

    @Test
    public void protectedConstructorNoPadding() throws Exception {
        final COPSIntegrity integrity = new COPSIntegrity(new COPSObjHeader(CNum.MSG_INTEGRITY, CType.DEF), 5, 6,
                new COPSData("1234"));
        Assert.assertEquals(5, integrity.getKeyId());
        Assert.assertEquals(6, integrity.getSeqNum());
        Assert.assertEquals(new COPSData("1234"), integrity.getKeyDigest());
        Assert.assertEquals(12, integrity.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        integrity.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Message-Integrity**", lines[0]);
        Assert.assertEquals("C-num: MSG_INTEGRITY", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Key Id: 5", lines[3]);
        Assert.assertEquals("Sequence: 6", lines[4]);
        Assert.assertEquals("Key digest: 1234", lines[5]);
    }

    @Test
    public void protectedConstructorWithPadding() throws Exception {
        final COPSIntegrity integrity = new COPSIntegrity(new COPSObjHeader(CNum.MSG_INTEGRITY, CType.DEF), 5, 6,
                new COPSData("12345"));
        Assert.assertEquals(5, integrity.getKeyId());
        Assert.assertEquals(6, integrity.getSeqNum());
        Assert.assertEquals(new COPSData("12345"), integrity.getKeyDigest());
        Assert.assertEquals(16, integrity.getDataLength());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        integrity.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Message-Integrity**", lines[0]);
        Assert.assertEquals("C-num: MSG_INTEGRITY", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Key Id: 5", lines[3]);
        Assert.assertEquals("Sequence: 6", lines[4]);
        Assert.assertEquals("Key digest: 12345", lines[5]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

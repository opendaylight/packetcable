package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.pcmm.rcd.IPCMMClient;

/**
 * Tests the public static COPSMsgParser methods
 */
public class COPSMsgParserTest {

    @Test
    public void testCombineTwoBytesToOne() {

        byte byte1 = (byte)2;
        byte byte2 = (byte)15;
        byte combined = (byte)((byte1) * 16 + (byte2));

        System.out.println("Combined value =" + combined + " Hex value = " + Integer.toHexString(combined) );

    }

    @Test
    public void testCombineAndSplitForAllValidCombinations() {
        for (byte byte1 = 0; byte1 < 16; byte1++ ) {
            for (byte byte2 = 0; byte2 < 16; byte2++ ) {
                int combined = COPSMsgParser.combineNibbles(byte1, byte2);
                System.out.println("byte1 = " + byte1 + " byte2 = " + byte2 + " Combined value =" + combined + " Hex value = " + Integer.toHexString(combined) );

                Assert.assertTrue(combined >= 0 && combined < 256);
                byte[] nibbles = COPSMsgParser.splitByteToNibbles((byte) combined);

                Assert.assertEquals("Nibble 1 value = " + nibbles[0], byte1, nibbles[0]);
                Assert.assertEquals("Nibble 2 value = " + nibbles[1], byte2, nibbles[1]);
            }
        }
    }

    @Test
    public void testBytesToShortMin() {
        final byte byte1 = (byte)0;
        final byte byte2 = (byte)0;
        final short val = COPSMsgParser.bytesToShort(byte1, byte2);
        final byte[] outBytes = COPSMsgParser.shortToBytes(val);
        Assert.assertEquals(byte1, outBytes[0]);
        Assert.assertEquals(byte2, outBytes[1]);
        Assert.assertEquals(0, val);
    }

    @Test
    public void testBytesToShortMax() {
        final byte byte1 = (byte)255;
        final byte byte2 = (byte)255;
        final short val = COPSMsgParser.bytesToShort(byte1, byte2);
        final byte[] outBytes = COPSMsgParser.shortToBytes(val);
        Assert.assertEquals(byte1, outBytes[0]);
        Assert.assertEquals(byte2, outBytes[1]);
    }

    @Test
    public void testBytesToShortPCMMClientType() {
        final byte[] outBytes = COPSMsgParser.shortToBytes(IPCMMClient.CLIENT_TYPE);
        final short val = COPSMsgParser.bytesToShort(outBytes[0], outBytes[1]);
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, val);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCombineByte1TooBig() {
        COPSMsgParser.combineNibbles((byte)16, (byte)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCombineByte2TooBig() {
        COPSMsgParser.combineNibbles((byte)0, (byte)16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCombineByte1TooSmall() {
        COPSMsgParser.combineNibbles((byte)-1, (byte)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCombineByte2TooSmall() {
        COPSMsgParser.combineNibbles((byte)0, (byte)-1);
    }

    // TODO - determine if tests for marshalling & un should be done with this class or implicitly via the COPSMsg objects

}

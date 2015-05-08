package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pcmm.rcd.IPCMMClient;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Tests for the first constructor of the COPSKAMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSKAMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSKAMsgTest {

    TestCOPSServer server;
    Socket outSocket;

    @Before
    public void setup() throws Exception {
        server = new TestCOPSServer();
        server.start();
        outSocket = new Socket(InetAddress.getLocalHost(), server.getPort());
    }

    @After
    public void tearDown() throws Exception {
        outSocket.close();
        server.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void version0() {
        new COPSKAMsg(0, Flag.SOLICITED, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlag() {
        new COPSKAMsg(1, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() {
        final COPSHeader hdr = null;
        new COPSKAMsg(hdr, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeader() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.NA, IPCMMClient.CLIENT_TYPE);
        new COPSKAMsg(hdr, null);
    }

    @Test
    public void validMinimal() {
        final COPSKAMsg msg = new COPSKAMsg(1, Flag.SOLICITED, null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals((short)0, msg.getHeader().getClientType());
        Assert.assertNull(msg.getIntegrity());
    }

    @Test
    public void validAll() throws Exception {
        final COPSKAMsg msg = new COPSKAMsg(1, Flag.SOLICITED, new COPSIntegrity());

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals((short)0, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSIntegrity(), msg.getIntegrity());
    }

    /**
     * This test is responsible for creating a COPSKAMsg object without any nulls or empty collections
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpAll() throws Exception {
        final COPSKAMsg msg = new COPSKAMsg(1, Flag.SOLICITED, new COPSIntegrity());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(11, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: SOLICITED", lines[2]);
        Assert.assertEquals("OpCode: KA", lines[3]);
        Assert.assertEquals("Client-type: 0", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSKAMsg object with the minimal necessary attributes to make
     * it valid. It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteMinimal() throws Exception {
        final COPSKAMsg msg = new COPSKAMsg(1, Flag.SOLICITED, null);

        msg.writeData(outSocket);

        final long start = System.currentTimeMillis();
        while (server.copsMsgs.size() < 1) {
            Thread.sleep(5);
            if (System.currentTimeMillis() - start > 2000) break;
        }

        Assert.assertEquals(1, server.copsMsgs.size());
        Assert.assertEquals(msg, server.copsMsgs.get(0));
    }

    /**
     * This test is responsible for creating a COPSKAMsg object without any nulls or empty collections
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAll() throws Exception {
        final COPSKAMsg msg = new COPSKAMsg(1, Flag.SOLICITED, new COPSIntegrity(8, 9, new COPSData("12345")));

        msg.writeData(outSocket);

        final long start = System.currentTimeMillis();
        while (server.copsMsgs.size() < 1) {
            Thread.sleep(5);
            if (System.currentTimeMillis() - start > 2000) break;
        }

        Assert.assertEquals(1, server.copsMsgs.size());
        Assert.assertEquals(msg, server.copsMsgs.get(0));
    }

}

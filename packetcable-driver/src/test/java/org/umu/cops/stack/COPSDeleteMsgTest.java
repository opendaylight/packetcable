package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.umu.cops.stack.COPSHeader.ClientType;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSReason.ReasonCode;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Tests for the first constructor of the COPSDeleteMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSDeleteMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSDeleteMsgTest {

    private final static int testPort = 7777;
    TestCOPSServer server;
    Socket outSocket;

    @Before
    public void setup() throws Exception {
        server = new TestCOPSServer(testPort);
        server.start();
        outSocket = new Socket(InetAddress.getLocalHost(), testPort);
    }

    @After
    public void tearDown() throws Exception {
        outSocket.close();
        server.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void version0() {
        new COPSDeleteMsg(0, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlag() {
        new COPSDeleteMsg(1, null, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClientType() {
        new COPSDeleteMsg(1, Flag.SOLICITED, null, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandle() {
        new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, null,
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullReasonType() {
        new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() {
        final COPSHeader hdr = null;
        new COPSDeleteMsg(hdr, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeader() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.CAT, ClientType.TYPE_1);
        new COPSDeleteMsg(hdr, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);
    }

    @Test
    public void validMinimal() {
        final COPSDeleteMsg msg = new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(ClientType.TYPE_1, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData()), msg.getClientHandle());
        Assert.assertEquals(new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), msg.getReason());
        Assert.assertNull(msg.getIntegrity());
    }

    @Test
    public void validAll() {
        final COPSDeleteMsg msg = new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA),
                new COPSIntegrity(4, 5, new COPSData("123456")));

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(ClientType.TYPE_1, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData()), msg.getClientHandle());
        Assert.assertEquals(new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), msg.getReason());
        Assert.assertEquals(new COPSIntegrity(4, 5, new COPSData("123456")), msg.getIntegrity());
    }

    /**
     * This test is responsible for creating a COPSDeleteMsg object without any nulls or empty collections
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpAll() throws Exception {
        final COPSDeleteMsg msg = new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA),
                new COPSIntegrity(4, 5, new COPSData("123456")));

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(20, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: SOLICITED", lines[2]);
        Assert.assertEquals("OpCode: DRQ", lines[3]);
        Assert.assertEquals("Client-type: TYPE_1", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSDeleteMsg object with the minimal necessary attributes to make
     * it valid. It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteMinimal() throws Exception {
        final COPSDeleteMsg msg = new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA), null);

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
     * This test is responsible for creating a COPSDeleteMsg object without any nulls or empty collections
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAll() throws Exception {
        final COPSDeleteMsg msg = new COPSDeleteMsg(1, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSReason(ReasonCode.INSUFF_RESOURCES, ReasonCode.NA),
                new COPSIntegrity(4, 5, new COPSData("123456")));

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

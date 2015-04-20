package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.ClientType;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Tests for the first constructor of the COPSClientCloseMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSClientCloseMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSClientCloseMsgTest {

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
        new COPSClientCloseMsg(0, Flag.SOLICITED, ClientType.TYPE_1,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlag() {
        new COPSClientCloseMsg(1, null, ClientType.TYPE_1,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClientType() {
        new COPSClientCloseMsg(1, Flag.SOLICITED, null,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullError() {
        new COPSClientCloseMsg(1, Flag.SOLICITED, ClientType.TYPE_1, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() {
        final COPSHeader hdr = null;
        new COPSClientCloseMsg(hdr, new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeader() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.NA, ClientType.TYPE_1);
        new COPSClientCloseMsg(hdr, new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);
    }

    @Test
    public void validMinimal() {
        final COPSClientCloseMsg msg = new COPSClientCloseMsg(1, Flag.SOLICITED, ClientType.TYPE_1,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(ClientType.TYPE_1, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), msg.getError());
        Assert.assertNull(msg.getRedirAddr());
        Assert.assertNull(msg.getIntegrity());
    }

    @Test
    public void validAll() throws Exception {
        final COPSClientCloseMsg msg = new COPSClientCloseMsg(1, Flag.SOLICITED, ClientType.TYPE_1,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED),
                new COPSIpv4PdpRedirectAddress("localhost", 7777, (short)0),
                new COPSIntegrity());

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(ClientType.TYPE_1, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), msg.getError());
        Assert.assertEquals(new COPSIpv4PdpRedirectAddress("localhost", 7777, (short) 0), msg.getRedirAddr());
        Assert.assertEquals(new COPSIntegrity(), msg.getIntegrity());
    }

    /**
     * This test is responsible for creating a COPSClientCloseMsg object without any nulls or empty collections
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpAll() throws Exception {
        final COPSClientCloseMsg msg = new COPSClientCloseMsg(1, Flag.SOLICITED, ClientType.TYPE_1,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED),
                new COPSIpv4PdpRedirectAddress("localhost", 7777, (short)0),
                new COPSIntegrity());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(22, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: SOLICITED", lines[2]);
        Assert.assertEquals("OpCode: CC", lines[3]);
        Assert.assertEquals("Client-type: TYPE_1", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSClientCloseMsg object with the minimal necessary attributes to make
     * it valid. It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteMinimal() throws Exception {
        final COPSClientCloseMsg msg = new COPSClientCloseMsg(1, Flag.SOLICITED, ClientType.TYPE_1,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.AUTH_REQUIRED), null, null);

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
     * This test is responsible for creating a COPSClientCloseMsg object without any nulls or empty collections
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAll() throws Exception {
        final COPSClientCloseMsg msg = new COPSClientCloseMsg(1, Flag.SOLICITED, ClientType.TYPE_1,
                new COPSError(ErrorTypes.BAD_HANDLE_REF, ErrorTypes.MA),
                new COPSIpv4PdpRedirectAddress("localhost", 7777, (short)0),
                new COPSIntegrity(8, 9, new COPSData("12345")));

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

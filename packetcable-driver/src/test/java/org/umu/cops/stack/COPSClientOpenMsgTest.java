package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pcmm.rcd.IPCMMClient;
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Tests for the first constructor of the COPSClientOpenMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSClientOpenMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSClientOpenMsgTest {

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
        new COPSClientOpenMsg(0, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSPepId(new COPSData()),
                null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlag() {
        new COPSClientOpenMsg(1, null, IPCMMClient.CLIENT_TYPE, new COPSPepId(new COPSData()), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPepId() {
        new COPSClientOpenMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() {
        final COPSHeader hdr = null;
        new COPSClientOpenMsg(hdr, new COPSPepId(new COPSData()), null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeader() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.CAT, IPCMMClient.CLIENT_TYPE);
        new COPSClientOpenMsg(hdr, new COPSPepId(new COPSData()), null, null, null);
    }

    @Test
    public void validMinimal() {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")), null, null, null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSPepId(new COPSData("12345")), msg.getPepId());
        Assert.assertNull(msg.getClientSI());
        Assert.assertNull(msg.getPdpAddress());
        Assert.assertNull(msg.getIntegrity());
    }

    @Test
    public void validAllIpv4() throws Exception {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.UNSOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")),
                new COPSClientSI(CSIType.NAMED, new COPSData("123456")),
                new COPSIpv4LastPdpAddr("localhost", 7777, (short)0),
                new COPSIntegrity(3, 4, new COPSData("1234567")));

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.UNSOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSPepId(new COPSData("12345")), msg.getPepId());
        Assert.assertEquals(new COPSClientSI(CSIType.NAMED, new COPSData("123456")), msg.getClientSI());
        Assert.assertEquals(new COPSIpv4LastPdpAddr("localhost", 7777, (short) 0), msg.getPdpAddress());
        Assert.assertEquals(new COPSIntegrity(3, 4, new COPSData("1234567")), msg.getIntegrity());
    }

    @Test
    public void validAllIpv6() throws Exception {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.UNSOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")),
                new COPSClientSI(CSIType.NAMED, new COPSData("123456")),
                new COPSIpv6LastPdpAddr("localhost", 7777, (short)0),
                new COPSIntegrity(3, 4, new COPSData("1234567")));

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.UNSOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSPepId(new COPSData("12345")), msg.getPepId());
        Assert.assertEquals(new COPSClientSI(CSIType.NAMED, new COPSData("123456")), msg.getClientSI());
        Assert.assertEquals(new COPSIpv6LastPdpAddr("localhost", 7777, (short)0), msg.getPdpAddress());
        Assert.assertEquals(new COPSIntegrity(3, 4, new COPSData("1234567")), msg.getIntegrity());
    }

    /**
     * This test is responsible for creating a COPSClientOpenMsg object without any nulls or empty collections
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpAll() throws Exception {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.UNSOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")),
                new COPSClientSI(CSIType.NAMED, new COPSData("123456")),
                new COPSIpv4LastPdpAddr("localhost", 7777, (short)0),
                new COPSIntegrity(3, 4, new COPSData("1234567")));

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(26, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: UNSOLICITED", lines[2]);
        Assert.assertEquals("OpCode: OPN", lines[3]);
        Assert.assertEquals("Client-type: -32758", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSClientOpenMsg object with the minimal necessary attributes to make
     * it valid. It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteMinimal() throws Exception {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")), null, null, null);

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
     * This test is responsible for creating a COPSClientOpenMsg object without any nulls for IPv4 addresses
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAllIpv4() throws Exception {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.UNSOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")),
                new COPSClientSI(CSIType.NAMED, new COPSData("123456")),
                new COPSIpv4LastPdpAddr("localhost", 7777, (short)0),
                new COPSIntegrity(3, 4, new COPSData("1234567")));

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
     * This test is responsible for creating a COPSClientOpenMsg object without any nulls for IPv6 addresses
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAllIpv6() throws Exception {
        final COPSClientOpenMsg msg = new COPSClientOpenMsg(1, Flag.UNSOLICITED, IPCMMClient.CLIENT_TYPE,
                new COPSPepId(new COPSData("12345")),
                new COPSClientSI(CSIType.NAMED, new COPSData("123456")),
                new COPSIpv6LastPdpAddr("localhost", 7777, (short)0),
                new COPSIntegrity(3, 4, new COPSData("1234567")));

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

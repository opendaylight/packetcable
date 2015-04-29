package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pcmm.rcd.IPCMMClient;
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSReportType.ReportType;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Tests for the first constructor of the COPSReportMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSReportMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSReportMsgTest {

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
        new COPSReportMsg(0, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlag() {
        new COPSReportMsg(1, null, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandle() {
        new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, null,
                new COPSReportType(ReportType.ACCOUNTING), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullReportType() {
        new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() {
        final COPSHeader hdr = null;
        new COPSReportMsg(hdr, new COPSHandle(new COPSData()), new COPSReportType(ReportType.ACCOUNTING), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeader() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.CAT, IPCMMClient.CLIENT_TYPE);
        new COPSReportMsg(hdr, new COPSHandle(new COPSData()), new COPSReportType(ReportType.ACCOUNTING), null, null);
    }

    @Test
    public void validMinimal() {
        final COPSReportMsg msg = new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), null, null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData()), msg.getClientHandle());
        Assert.assertEquals(new COPSReportType(ReportType.ACCOUNTING), msg.getReport());
        Assert.assertNull(msg.getClientSI());
        Assert.assertNull(msg.getIntegrity());
    }

    @Test
    public void validAll() {
        final COPSReportMsg msg = new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), new COPSClientSI(CSIType.NAMED, new COPSData("1")),
                new COPSIntegrity(4, 5, new COPSData("123456")));

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData()), msg.getClientHandle());
        Assert.assertEquals(new COPSReportType(ReportType.ACCOUNTING), msg.getReport());
        Assert.assertEquals(new COPSClientSI(CSIType.NAMED, new COPSData("1")), msg.getClientSI());
        Assert.assertEquals(new COPSIntegrity(4, 5, new COPSData("123456")), msg.getIntegrity());
    }

    /**
     * This test is responsible for creating a COPSReportMsg object without any nulls or empty collections
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpAll() throws Exception {
        final COPSReportMsg msg = new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), new COPSClientSI(CSIType.NAMED, new COPSData("1")),
                new COPSIntegrity(4, 5, new COPSData("123456")));

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(24, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: SOLICITED", lines[2]);
        Assert.assertEquals("OpCode: RPT", lines[3]);
        Assert.assertEquals("Client-type: -32758", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSReportMsg object with the minimal necessary attributes to make
     * it valid. It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteMinimal() throws Exception {
        final COPSReportMsg msg = new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), null, null);

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
     * This test is responsible for creating a COPSReportMsg object without any nulls or empty collections
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAll() throws Exception {
        final COPSReportMsg msg = new COPSReportMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSReportType(ReportType.ACCOUNTING), new COPSClientSI(CSIType.NAMED, new COPSData("1")),
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

package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pcmm.rcd.IPCMMClient;
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSHeader.Flag;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for the first constructor of the COPSReqMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSReqMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSReqMsgTest {

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
        new COPSReqMsg(0, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSContext(RType.CONFIG, (short)0), null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlag() {
        new COPSReqMsg(1, null, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSContext(RType.CONFIG, (short)0), null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandle() {
        new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, null,
                new COPSContext(RType.CONFIG, (short)0), null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullContext() {
        new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                null, null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeader() {
        final COPSHeader hdr = null;
        new COPSReqMsg(hdr, new COPSHandle(new COPSData()), new COPSContext(RType.CONFIG, (short)0),
                null, null, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeader() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.CAT, IPCMMClient.CLIENT_TYPE);
        new COPSReqMsg(hdr, new COPSHandle(new COPSData()), new COPSContext(RType.CONFIG, (short)0),
                null, null, null, null, null);
    }

    @Test
    public void validMinimal() {
        final COPSReqMsg msg = new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSContext(RType.CONFIG, (short)0), null, null, null, null, null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData()), msg.getClientHandle());
        Assert.assertEquals(new COPSContext(RType.CONFIG, (short) 0), msg.getContext());
        Assert.assertNull(msg.getIntegrity());
        Assert.assertNull(msg.getInInterface());
        Assert.assertNull(msg.getOutInterface());
        Assert.assertTrue(msg.getClientSI().isEmpty());
        Assert.assertTrue(msg.getDecisions().isEmpty());
    }

    @Test
    public void validAllNonCollections() throws Exception {
        final COPSReqMsg msg = new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSContext(RType.CONFIG, (short)0), new COPSIntegrity(),
                new COPSIpv4InInterface(new COPSIpv4Address("localhost"), 0),
                new COPSIpv4OutInterface(new COPSIpv4Address("localhost"), 0),
                null, null);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.SOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData()), msg.getClientHandle());
        Assert.assertEquals(new COPSContext(RType.CONFIG, (short)0), msg.getContext());
        Assert.assertEquals(new COPSIntegrity(), msg.getIntegrity());
        Assert.assertEquals(new COPSIpv4InInterface(new COPSIpv4Address("localhost"), 0), msg.getInInterface());
        Assert.assertEquals(new COPSIpv4OutInterface(new COPSIpv4Address("localhost"), 0), msg.getOutInterface());
        Assert.assertTrue(msg.getClientSI().isEmpty());
        Assert.assertTrue(msg.getDecisions().isEmpty());
    }

    @Test
    public void validNonEmptyCollections() {
        final Set<COPSClientSI> clientSIs = new HashSet<>();
        clientSIs.add(new COPSClientSI(CSIType.NAMED, new COPSData("1")));
        clientSIs.add(new COPSClientSI(CSIType.SIGNALED, new COPSData("2")));

        final Map<COPSContext, Set<COPSLPDPDecision>> decisions = new HashMap<>();
        final COPSContext context1 = new COPSContext(RType.CONFIG, (short)1);
        final Set<COPSLPDPDecision> decisions1 = new HashSet<>();
        decisions1.add(new COPSLPDPDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR, new COPSData("12345")));
        decisions1.add(new COPSLPDPDecision(CType.NAMED, Command.NULL, DecisionFlag.REQSTATE, new COPSData("123456")));
        decisions.put(context1, decisions1);

        final COPSContext context2 = new COPSContext(RType.IN_ADMIN, (short)2);
        final Set<COPSLPDPDecision> decisions2 = new HashSet<>();
        decisions2.add(new COPSLPDPDecision(CType.STATELESS, Command.REMOVE, DecisionFlag.REQERROR, new COPSData("1234567")));
        decisions.put(context2, decisions2);

        final COPSReqMsg msg = new COPSReqMsg(1, Flag.UNSOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData("123")),
                new COPSContext(RType.IN_ADMIN, (short)2), null, null, null, clientSIs, decisions);

        Assert.assertEquals(1, msg.getHeader().getPcmmVersion());
        Assert.assertEquals(Flag.UNSOLICITED, msg.getHeader().getFlag());
        Assert.assertEquals(IPCMMClient.CLIENT_TYPE, msg.getHeader().getClientType());
        Assert.assertEquals(new COPSHandle(new COPSData("123")), msg.getClientHandle());
        Assert.assertEquals(new COPSContext(RType.IN_ADMIN, (short) 2), msg.getContext());
        Assert.assertNull(msg.getIntegrity());
        Assert.assertNull(msg.getInInterface());
        Assert.assertNull(msg.getOutInterface());
        Assert.assertEquals(clientSIs, msg.getClientSI());
        Assert.assertNotSame(clientSIs, msg.getClientSI());
        Assert.assertEquals(decisions, msg.getDecisions());
        Assert.assertNotSame(decisions, msg.getDecisions());
    }

    /**
     * This test is responsible for creating a COPSReqMsg object without any nulls or empty collections
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpAll() throws Exception {
        final Set<COPSClientSI> clientSIs = new HashSet<>();
        clientSIs.add(new COPSClientSI(CSIType.NAMED, new COPSData("1")));
        clientSIs.add(new COPSClientSI(CSIType.SIGNALED, new COPSData("2")));

        final Map<COPSContext, Set<COPSLPDPDecision>> decisions = new HashMap<>();
        final COPSContext context1 = new COPSContext(RType.CONFIG, (short)1);
        final Set<COPSLPDPDecision> decisions1 = new HashSet<>();
        decisions1.add(new COPSLPDPDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR, new COPSData("12345")));
        decisions1.add(new COPSLPDPDecision(CType.NAMED, Command.NULL, DecisionFlag.REQSTATE, new COPSData("123456")));
        decisions.put(context1, decisions1);

        final COPSContext context2 = new COPSContext(RType.IN_ADMIN, (short)2);
        final Set<COPSLPDPDecision> decisions2 = new HashSet<>();
        decisions2.add(new COPSLPDPDecision(CType.STATELESS, Command.REMOVE, DecisionFlag.REQERROR, new COPSData("1234567")));
        decisions.put(context2, decisions2);

        final COPSReqMsg msg = new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData()),
                new COPSContext(RType.CONFIG, (short)0), new COPSIntegrity(),
                new COPSIpv4InInterface(new COPSIpv4Address("localhost"), 0),
                new COPSIpv4OutInterface(new COPSIpv4Address("localhost"), 0),
                clientSIs, decisions);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(52, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: SOLICITED", lines[2]);
        Assert.assertEquals("OpCode: REQ", lines[3]);
        Assert.assertEquals("Client-type: -32758", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSReqMsg object with the minimal necessary attributes to make
     * it valid. It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteMinimal() throws Exception {
        final COPSReqMsg msg = new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData("12345")),
                new COPSContext(RType.CONFIG, (short)5), null,
                null, null, null, null);

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
     * This test is responsible for creating a COPSReqMsg object without any nulls or empty collections
     * and then is streamed over a socket (unmarshalled) then reassembled (marshalled)
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteAll() throws Exception {
        final Set<COPSClientSI> clientSIs = new HashSet<>();
        clientSIs.add(new COPSClientSI(CSIType.NAMED, new COPSData("12345")));
        clientSIs.add(new COPSClientSI(CSIType.SIGNALED, new COPSData("123456")));

        final Map<COPSContext, Set<COPSLPDPDecision>> decisions = new HashMap<>();
        final COPSContext context1 = new COPSContext(RType.CONFIG, (short)1);
        final Set<COPSLPDPDecision> decisions1 = new HashSet<>();
        decisions1.add(new COPSLPDPDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR, new COPSData("12345")));
        decisions1.add(new COPSLPDPDecision(CType.NAMED, Command.NULL, DecisionFlag.REQSTATE, new COPSData("123456")));
        decisions.put(context1, decisions1);

        final COPSContext context2 = new COPSContext(RType.IN_ADMIN, (short)2);
        final Set<COPSLPDPDecision> decisions2 = new HashSet<>();
        decisions2.add(new COPSLPDPDecision(CType.STATELESS, Command.REMOVE, DecisionFlag.REQERROR, new COPSData("1234567")));
        decisions.put(context2, decisions2);

        final COPSReqMsg msg = new COPSReqMsg(1, Flag.SOLICITED, IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData("12345")),
                new COPSContext(RType.CONFIG, (short)5), new COPSIntegrity(3, 4, new COPSData("12345")),
                new COPSIpv4InInterface(new COPSIpv4Address("localhost"), 5),
                new COPSIpv4OutInterface(new COPSIpv4Address("localhost"), 6),
                clientSIs, decisions);

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

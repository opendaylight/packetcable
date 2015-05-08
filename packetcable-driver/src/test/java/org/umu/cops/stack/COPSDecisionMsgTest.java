package org.umu.cops.stack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.ClientType;
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
 * Tests for the first constructor of the COPSDecisionMsg class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSDecisionMsg had been
 * released and my assumptions may be incorrect.
 */
public class COPSDecisionMsgTest {

    private final static int testPort = 7777;
    TestCOPSServer server;
    Socket outSocket;

    final static Map<COPSContext, Set<COPSDecision>> staticDecisions = new HashMap<>();
    static {
        final Set<COPSDecision> decisions1 = new HashSet<>();
        decisions1.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR, new COPSData("12345")));
        staticDecisions.put(new COPSContext(RType.CONFIG, (short)1), decisions1);
    }

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
    public void version0WithError() {
        new COPSDecisionMsg(0, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void version0WithDecisions() {
        new COPSDecisionMsg(0, Flag.SOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlagWithError() {
        new COPSDecisionMsg(1, null, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFlagWithDecisions() {
        new COPSDecisionMsg(1, null, ClientType.TYPE_1, new COPSHandle(new COPSData()),
                staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClientTypeDeprecatedWithError() {
        new COPSDecisionMsg(null, new COPSHandle(new COPSData()),
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClientTypeDeprecatedWithDecisions() {
        new COPSDecisionMsg(null, new COPSHandle(new COPSData()),
                staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClientTypeWithError() {
        new COPSDecisionMsg(1, Flag.UNSOLICITED, null, new COPSHandle(new COPSData()),
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClientTypeWithDecisions() {
        new COPSDecisionMsg(1, Flag.UNSOLICITED, null, new COPSHandle(new COPSData()),
                staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandleDeprecatedWithError() {
        new COPSDecisionMsg(ClientType.TYPE_1, null,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandleDeprecatedWithDecisions() {
        new COPSDecisionMsg(ClientType.TYPE_1, null, staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandleWithError() {
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, null,
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHandleWithDecisions() {
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, null, staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullErrorDeprecated() {
        final COPSError error = null;
        new COPSDecisionMsg(ClientType.TYPE_1, new COPSHandle(new COPSData()), error, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullError() {
        final COPSError error = null;
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()), error, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDecisionsDeprecated() {
        final Map<COPSContext, Set<COPSDecision>> decisions = null;
        new COPSDecisionMsg(ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDecisions() {
        final Map<COPSContext, Set<COPSDecision>> decisions = null;
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDecisionsMapDeprecated() {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        new COPSDecisionMsg(ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDecisionsMap() {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDecisionsSetDeprecated() {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        decisions.put(new COPSContext(RType.CONFIG, (short)1), null);
        new COPSDecisionMsg(ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDecisionsSet() {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        decisions.put(new COPSContext(RType.CONFIG, (short)1), null);
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDecisionsSetDeprecated() {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        decisions.put(new COPSContext(RType.CONFIG, (short)1), new HashSet<COPSDecision>());
        new COPSDecisionMsg(ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDecisionsSet() {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        decisions.put(new COPSContext(RType.CONFIG, (short)1), new HashSet<COPSDecision>());
        new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1, new COPSHandle(new COPSData()), decisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeaderErrorOnly() {
        final COPSHeader hdr = null;
        new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()), new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA),
                null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullHeaderDecisionsOnly() {
        final COPSHeader hdr = null;
        new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()), null, staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHdrErrorOnly() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.CC, ClientType.TYPE_1);
        new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()), new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA),
                null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHdrDecisionsOnly() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.CC, ClientType.TYPE_1);
        new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()), null, staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withErrorAndDecisions() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.DEC, ClientType.TYPE_1);
        new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()), new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA),
                staticDecisions, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullErrorAndDecisions() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.DEC, ClientType.TYPE_1);
        new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()), null, null, null);
    }

    @Test
    public void validProtectedError() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.DEC, ClientType.TYPE_1);
        final COPSDecisionMsg msg = new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()),
                new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), null, null);
        Assert.assertEquals(OPCode.DEC, msg.getHeader().getOpCode());
        Assert.assertEquals(ClientType.TYPE_1, msg.getHeader().getClientType());
        Assert.assertEquals(hdr, msg.getHeader());
        Assert.assertEquals(new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA), msg.getError());
        Assert.assertNotNull(msg.getDecisions());
        Assert.assertTrue(msg.getDecisions().isEmpty());
        Assert.assertNull(msg.getIntegrity());
    }

    @Test
    public void validProtectedDecisions() {
        final COPSHeader hdr = new COPSHeader(1, Flag.UNSOLICITED, OPCode.DEC, ClientType.TYPE_1);
        final COPSDecisionMsg msg = new COPSDecisionMsg(hdr, new COPSHandle(new COPSData()),
                null, staticDecisions, null);
        Assert.assertEquals(OPCode.DEC, msg.getHeader().getOpCode());
        Assert.assertEquals(ClientType.TYPE_1, msg.getHeader().getClientType());
        Assert.assertEquals(hdr, msg.getHeader());
        Assert.assertNull(msg.getError());
        Assert.assertNotNull(msg.getDecisions());
        Assert.assertFalse(msg.getDecisions().isEmpty());
        Assert.assertNotSame(staticDecisions, msg.getDecisions());
        Assert.assertEquals(staticDecisions, msg.getDecisions());
        Assert.assertNull(msg.getIntegrity());
    }

    /**
     * This test is responsible for creating a COPSDecisionMsg object with error and no decisions
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpErrorAll() throws Exception {
        final COPSDecisionMsg msg = new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1,
                new COPSHandle(new COPSData()), new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.NA),
                new COPSIntegrity());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(20, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: UNSOLICITED", lines[2]);
        Assert.assertEquals("OpCode: DEC", lines[3]);
        Assert.assertEquals("Client-type: TYPE_1", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSDecisionMsg object with error and no decisions
     * and then is dumped to an OutputStream.
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testDumpDecisionsAll() throws Exception {
        final COPSDecisionMsg msg = new COPSDecisionMsg(1, Flag.UNSOLICITED, ClientType.TYPE_1,
                new COPSHandle(new COPSData()), staticDecisions, new COPSIntegrity());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        msg.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(24, lines.length);

        // Only checking COPSMsg elements as the COPSObjectMsg elements have already been validated in their own tests
        Assert.assertEquals("**MSG HEADER**", lines[0]);
        Assert.assertEquals("Version: 1", lines[1]);
        Assert.assertEquals("Flags: UNSOLICITED", lines[2]);
        Assert.assertEquals("OpCode: DEC", lines[3]);
        Assert.assertEquals("Client-type: TYPE_1", lines[4]);
    }

    /**
     * This test is responsible for creating a COPSDecisionMsg error object with the minimal necessary attributes.
     * It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteErrorMin() throws Exception {
        final COPSDecisionMsg msg = new COPSDecisionMsg(2, Flag.SOLICITED, ClientType.TYPE_2,
                new COPSHandle(new COPSData("12345")), new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.COMM_FAILURE),
                null);

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
     * This test is responsible for creating a COPSDecisionMsg error object with all valid attributes.
     * It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteErrorWithIntegrity() throws Exception {
        final COPSDecisionMsg msg = new COPSDecisionMsg(2, Flag.SOLICITED, ClientType.TYPE_2,
                new COPSHandle(new COPSData("12345")), new COPSError(ErrorTypes.AUTH_FAILURE, ErrorTypes.COMM_FAILURE),
                new COPSIntegrity(2, 3, new COPSData("123456")));

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
     * This test is responsible for creating a COPSDecisionMsg error object with all valid attributes.
     * It is then streamed over a socket (unmarshalled) then reassembled (marshalled).
     * @throws Exception - Test should fail if any exception is thrown
     */
    @Test
    public void testWriteDecisionsWithIntegrity() throws Exception {
        final Map<COPSContext, Set<COPSDecision>> decisions = new HashMap<>();
        final COPSContext context1 = new COPSContext(RType.CONFIG, (short)1);
        final Set<COPSDecision> decisions1 = new HashSet<>();
        decisions1.add(new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.REQERROR, new COPSData("12345")));
        decisions1.add(new COPSDecision(CType.NAMED, Command.NULL, DecisionFlag.REQSTATE, new COPSData("123456")));
        decisions.put(context1, decisions1);

        final COPSContext context2 = new COPSContext(RType.IN_ADMIN, (short)2);
        final Set<COPSDecision> decisions2 = new HashSet<>();
        decisions2.add(new COPSDecision(CType.STATELESS, Command.REMOVE, DecisionFlag.REQERROR, new COPSData("1234567")));
        decisions.put(context2, decisions2);

        final COPSDecisionMsg msg = new COPSDecisionMsg(2, Flag.UNSOLICITED, ClientType.TYPE_3,
                new COPSHandle(new COPSData("12345")), decisions, new COPSIntegrity(4, 5, new COPSData("123456")));

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

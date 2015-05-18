/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.opendaylight.controller.packetcable.provider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.TpProtocol;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.traffic.profile.TrafficProfile;
import org.pcmm.PCMMPdpAgent;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.rcd.IPCMMClient;
import org.pcmm.rcd.impl.CMTS;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests the PCMMService's ability to connect to a CMTS. Gate additions will not properly work as there is currently
 * not any other means to receive acknowledgements. This functionality must be tested by the PCMMService's client
 * PacketcableProvider.
 */
public class PCMMServiceTest {

    private final static String ccapId = "ccap-1";
    private final static String gatePath = "testGatePath";

    /**
     * Denotes whether or not a real CMTS is being tested against.
     * Ensure the checked-in value is always false else tests will most likely fail.
     */
    private final static boolean realCmts = false;


    // The test objects/values to use that will be instantiated in @Before

    /**
     * The mock CMTS running on localhost with a dynamic port assigned.
     */
    private CMTS icmts;

    /**
     * The IP address object for the CMTS to test against
     */
    private Ipv4Address cmtsAddr;

    /**
     * The gate classifier's srcIp value, any valid IP should work.
     */
    private Ipv4Address srcAddr;

    /**
     * The gate classifier's dstIp value, any valid IP should work.
     */
    private Ipv4Address dstAddr;

    /**
     * Defines the CMTS to add to the PCMMService
     */
    private Ccaps ccap;

    /**
     * The class under test
     */
    private PCMMService service;

    /**
     * The cable modem IP address to which a gate should be set
     */
    private InetAddress cmAddrInet;
    private InetAddress invalidCmAddrInet;

    @Before
    public void setup() throws IOException {
        srcAddr = new Ipv4Address("10.10.10.0");
        dstAddr = new Ipv4Address("10.32.99.99");

        if (realCmts) {
            cmAddrInet = InetAddress.getByAddress(new byte[] {10, 32, 110, (byte)180});
            invalidCmAddrInet = InetAddress.getByAddress(new byte[] {99, 99, 99, 99});

            // Use me when testing against a CMTS or emulator not running in the same JVM
            cmtsAddr = new Ipv4Address("10.32.10.3");
            ccap = makeCcapsObj(PCMMPdpAgent.WELL_KNOWN_PDP_PORT, cmtsAddr, ccapId);
        } else {
            cmAddrInet = InetAddress.getByAddress(new byte[] {10, 32, 110, (byte)180});
            invalidCmAddrInet = InetAddress.getByAddress(new byte[] {99, 99, 99, 99});

            // Use me for automated testing and the CMTS emulator running in the same JVM
            cmtsAddr = new Ipv4Address("127.0.0.1");

            final Set<String> upGates = new HashSet<>();
            upGates.add("extrm_up");
            final Set<String> dnGates = new HashSet<>();
            dnGates.add("extrm_dn");
            final Map<Direction, Set<String>> gates = new HashMap<>();
            gates.put(Direction.UPSTREAM, upGates);
            gates.put(Direction.DOWNSTREAM, dnGates);

            final Map<String, Boolean> cmStatus = new HashMap<>();
            cmStatus.put(cmAddrInet.getHostAddress(), true);
            cmStatus.put(invalidCmAddrInet.getHostAddress(), false);
            icmts = new CMTS(gates, cmStatus);
            icmts.startServer();

            ccap = makeCcapsObj(icmts.getPort(), cmtsAddr, ccapId);
        }

        service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
    }

    @After
    public void tearDown() {
        if (icmts != null) icmts.stopServer();
        service.disconect();
    }

    @Test
    public void testAddCcap() {
        connectToCmts(service);
    }

    @Test
    public void testAddInvalidCcapBadPort() {
        ccap = makeCcapsObj((icmts.getPort() + 1), cmtsAddr, ccapId);
        service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
        final String message = service.addCcap();
        Assert.assertNotNull(message);
        final String expectedMsg = "404 Not Found - CCAP " + ccapId + " failed to connect @ " + cmtsAddr.getValue()
                + ':' + (icmts.getPort() + 1) + " - ";
        Assert.assertTrue(expectedMsg, message.startsWith(expectedMsg));
    }

    @Test
    public void testAddValidUpGateTwice() throws Exception {
        connectToCmts(service);
        final String expectedMsg1 = "200 OK - sendGateSet for " + ccapId + '/' + gatePath + " returned GateId";
        addAndValidateGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet, gatePath,
                expectedMsg1);

        final String expectedMsg2 = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath + " already exists";
        addAndValidateGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet, gatePath,
                expectedMsg2);

        Assert.assertTrue(deleteGate(service, gatePath));
    }

    @Test
    public void testAddTwoValidUpGates() throws Exception {
        connectToCmts(service);

        final String gatePath1 = "gatePath1";
        final String expectedMsg1 = "200 OK - sendGateSet for " + ccapId + '/' + gatePath1 + " returned GateId";
        addAndValidateGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet, gatePath1,
                expectedMsg1);

        final String gatePath2 = "gatePath2";
        final String expectedMsg2 = "200 OK - sendGateSet for " + ccapId + '/' + gatePath2 + " returned GateId";
        addAndValidateGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet, gatePath2,
                expectedMsg2);

        Assert.assertTrue(deleteGate(service, gatePath1));
        Assert.assertTrue(deleteGate(service, gatePath2));
    }

    @Test
    public void testAddValidDownGateTwice() throws Exception {
        connectToCmts(service);
        final String expectedMsg1 = "200 OK - sendGateSet for " + ccapId + '/' + gatePath + " returned GateId";
        addAndValidateGate(service, "extrm_dn", srcAddr, dstAddr, ServiceFlowDirection.Ds, cmAddrInet, gatePath,
                expectedMsg1);

        final String expectedMsg2 = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath + " already exists";
        addAndValidateGate(service, "extrm_dn", srcAddr, dstAddr, ServiceFlowDirection.Ds, cmAddrInet, gatePath,
                expectedMsg2);

        Assert.assertTrue(deleteGate(service, gatePath));
    }

    @Test
    public void testDeleteNonExistentGate() throws Exception {
        connectToCmts(service);
        Assert.assertFalse(deleteGate(service, gatePath));
    }

    @Test
    public void testAddAndRemoveValidUpGate() throws Exception {
        final String expectedMsgStart = "200 OK - sendGateSet for " + ccapId + '/' + gatePath + " returned GateId";
        addRemoveValidateGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet, gatePath,
                expectedMsgStart);
    }

    @Test
    public void testAddAndRemoveValidDownGate() throws Exception {
        final String expectedMsgStart = "200 OK - sendGateSet for " + ccapId + '/' + gatePath + " returned GateId";
        addRemoveValidateGate(service, "extrm_dn", srcAddr, dstAddr, ServiceFlowDirection.Ds, cmAddrInet, gatePath,
                expectedMsgStart);
    }

    @Test
    public void testAddAndRemoveInvalidCmAddrUpGate() throws Exception {
        // TODO - fix cmts emulator
        final String expectedMsgStart = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath
                + " returned error - Error Code: 13 Error Subcode : 0  Invalid SubscriberID";
        addRemoveValidateGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, invalidCmAddrInet,
                gatePath, expectedMsgStart);
    }

    @Test
    public void testAddInvalidScnUpGate() throws Exception {
        final String expectedMsgStart = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath
                + " returned error - Error Code: 11 Error Subcode : 0  Undefined Service Class Name";
        addRemoveValidateGate(service, "extrm_up_invalid", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet,
                gatePath, expectedMsgStart);
    }

    @Test
    public void testAddInvalidScnDownGate() throws Exception {
        final String expectedMsgStart = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath
                + " returned error - Error Code: 11 Error Subcode : 0  Undefined Service Class Name";
        addRemoveValidateGate(service, "extrm_dn_invalid", srcAddr, dstAddr, ServiceFlowDirection.Ds, cmAddrInet,
                gatePath, expectedMsgStart);
    }

    /**
     * This tests the instantiation of a COPSDecisionMsg object that is responsible for setting a gate request,
     * streams it over a mock Socket object and parses the bytes into a new COPSDecisionMsg object which should
     * be equivalent
     * @throws Exception - test will fail should any exception be thrown during execution
     */
    @Test
    public void testGateRequestDecisionMsg() throws Exception {
        final Socket socket = new MockSocket();

        final ServiceFlowDirection direction = ServiceFlowDirection.Us;
        final Gates gate = makeGateObj("extrm_up", cmtsAddr, direction, new Ipv4Address("127.0.0.1"));
        final IPCMMGate gateReq = makeGateRequest(ccap, gate, InetAddress.getByName("localhost"), direction);
        final byte[] data = gateReq.getData();

        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.DEF, Command.INSTALL, DecisionFlag.REQERROR));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short) 0), decisionSet);

        final COPSClientSI clientSD = new COPSClientSI(CNum.DEC, CType.CSI, new COPSData(data, 0, data.length));
        final COPSDecisionMsg decisionMsg = new COPSDecisionMsg(IPCMMClient.CLIENT_TYPE, new COPSHandle(new COPSData("123")),
                decisionMap, null, clientSD);
        decisionMsg.writeData(socket);

        final COPSMsg msg = COPSMsgParser.parseMessage(socket);
        Assert.assertNotNull(msg);
        Assert.assertEquals(decisionMsg, msg);
    }

    /**
     * Attempts to create a gate against a CMTS, validates the results then attempts to delete it.
     * @param service - the service used to connect to a CMTS for issuing requests
     * @param scnName - the service class name (aka. gate name)
     * @param srcAddr - the address to the CMTS subnet?
     * @param dstAddr - the destination address
     * @param direction - the gate direction
     * @param cmAddrInet - the address to the cable modem to which the gate will be assigned
     * @param gatePath - the path to the gate
     * @param expGateSetMsgStart - the expected start of the gate set return message to be validated against
     */
    private void addRemoveValidateGate(final PCMMService service, final String scnName, final Ipv4Address srcAddr,
                                       final Ipv4Address dstAddr, final ServiceFlowDirection direction,
                                       final InetAddress cmAddrInet, final String gatePath,
                                       final String expGateSetMsgStart) {
        connectToCmts(service);
        addAndValidateGate(service, scnName, srcAddr, dstAddr, direction, cmAddrInet, gatePath, expGateSetMsgStart);
        deleteGate(service, gatePath);
    }

    private void connectToCmts(final PCMMService service) {
        final String message = service.addCcap();
        Assert.assertNotNull(message);
        final String expectedMsg = "200 OK - CCAP " + ccapId + " connected @ "
                + ccap.getConnection().getIpAddress().getIpv4Address().getValue()
                + ":" + ccap.getConnection().getPort().getValue();
        Assert.assertEquals(expectedMsg, message);
        Assert.assertNotNull(service.ccapClient.pcmmPdp.getClientHandle());
    }

    /**
     * Attempts to create a gate against a CMTS and validates the results.
     * @param service - the service used to connect to a CMTS for issuing requests
     * @param scnName - the service class name (aka. gate name)
     * @param srcAddr - the address to the CMTS subnet?
     * @param dstAddr - the destination address
     * @param direction - the gate direction
     * @param cmAddrInet - the address to the cable modem to which the gate will be assigned
     * @param gatePath - the path to the gate
     * @param expGateSetMsgStart - the expected start of the gate set return message to be validated against
     */
    private void addAndValidateGate(final PCMMService service, final String scnName, final Ipv4Address srcAddr,
                                    final Ipv4Address dstAddr, final ServiceFlowDirection direction,
                                    final InetAddress cmAddrInet, final String gatePath,
                                    final String expGateSetMsgStart) {
        final Gates gate = makeGateObj(scnName, srcAddr, direction, dstAddr);

        final String gateSetMsg = service.sendGateSet(gatePath, cmAddrInet, gate, direction);
        Assert.assertNotNull(gateSetMsg);
        Assert.assertTrue(gateSetMsg, gateSetMsg.startsWith(expGateSetMsgStart));

        // TODO - add validation to the PCMMGateReq contained within the map
        Assert.assertNotNull(service.gateRequests.get(gatePath));
    }

    /**
     * Attempts to delete a gate
     * @param service - the service used to connect to a CMTS for issuing requests
     * @param gatePath - the path to the gate
     */
    private boolean deleteGate(final PCMMService service, final String gatePath) {
        final boolean out = service.sendGateDelete(gatePath);

        // Wait up to 1 sec for response to be processed
        final long start = System.currentTimeMillis();
        while (1000 < System.currentTimeMillis() - start) {
            if (service.gateRequests.size() == 0) break;
        }
        Assert.assertNull(service.gateRequests.get(gatePath));
        return out;
    }

    /**
     * Creates a mock Ccaps object that can be used for connecting to a CMTS
     * @param inPort - the CMTS port number
     * @param ipAddr - the CMTS IPv4 address string
     * @param ccapId - the ID of the CCAP
     * @return - the mock Ccaps object
     */
    private Ccaps makeCcapsObj(final int inPort, final Ipv4Address ipAddr, final String ccapId) {
        final Ccaps ccap = Mockito.mock(Ccaps.class);
        final Connection conn = Mockito.mock(Connection.class);
        Mockito.when(ccap.getConnection()).thenReturn(conn);
        final PortNumber port = Mockito.mock(PortNumber.class);
        Mockito.when(conn.getPort()).thenReturn(port);
        Mockito.when(port.getValue()).thenReturn(inPort);

        final IpAddress addr = Mockito.mock(IpAddress.class);
        Mockito.when(conn.getIpAddress()).thenReturn(addr);
        Mockito.when(addr.getIpv4Address()).thenReturn(ipAddr);

        Mockito.when(ccap.getCcapId()).thenReturn(ccapId);
        final AmId amid = Mockito.mock(AmId.class);
        Mockito.when(ccap.getAmId()).thenReturn(amid);
        Mockito.when(amid.getAmTag()).thenReturn(0xcada);
        Mockito.when(amid.getAmType()).thenReturn(1);

        return ccap;
    }

    /**
     * Creates a mock Gates object
     * @param scnValue - the service class name defined on the CMTS
     * @param dstAddr - the CM address this gate should be set against
     * @return - the gate request
     */
    private Gates makeGateObj(final String scnValue, final Ipv4Address srcAddr, final ServiceFlowDirection direction,
                              final Ipv4Address dstAddr) {
        final Gates gate = Mockito.mock(Gates.class);
        final GateSpec gateSpec = Mockito.mock(GateSpec.class);
        Mockito.when(gate.getGateSpec()).thenReturn(gateSpec);
        Mockito.when(gateSpec.getDirection()).thenReturn(direction);
        // TODO - make sure to write a test when this value is not null
        Mockito.when(gateSpec.getDscpTosOverwrite()).thenReturn(null);
        final TrafficProfile trafficProfile = Mockito.mock(TrafficProfile.class);
        final ServiceClassName scn = Mockito.mock(ServiceClassName.class);
        Mockito.when(scn.getValue()).thenReturn(scnValue);
        Mockito.when(trafficProfile.getServiceClassName()).thenReturn(scn);
        Mockito.when(gate.getTrafficProfile()).thenReturn(trafficProfile);

        // TODO - write tests when this is null and ExtClassifier or Ipv6Classifier objects are not null
        final Classifier classifier = Mockito.mock(Classifier.class);

        // This is the address of the CM
        Mockito.when(classifier.getDstIp()).thenReturn(dstAddr);

        final PortNumber dstPort = new PortNumber(4321);
        Mockito.when(classifier.getDstPort()).thenReturn(dstPort);
        final TpProtocol protocol = new TpProtocol(0);
        Mockito.when(classifier.getProtocol()).thenReturn(protocol);
        Mockito.when(classifier.getSrcIp()).thenReturn(srcAddr);
        final PortNumber srcPort = new PortNumber(1234);
        Mockito.when(classifier.getSrcPort()).thenReturn(srcPort);
        final TosByte tosByte = new TosByte((short)160);
        Mockito.when(classifier.getTosByte()).thenReturn(tosByte);
        final TosByte tosMask = new TosByte((short)224);
        Mockito.when(classifier.getTosMask()).thenReturn(tosMask);

        // TODO - enhance to test support of the other classifier types
        Mockito.when(gate.getClassifier()).thenReturn(classifier);
        Mockito.when(gate.getExtClassifier()).thenReturn(null);
        Mockito.when(gate.getIpv6Classifier()).thenReturn(null);
        return gate;
    }

    private IPCMMGate makeGateRequest(final Ccaps ccap, final Gates gateReq, final InetAddress addrSubId,
                                     final ServiceFlowDirection direction) {
        final PCMMGateReqBuilder gateBuilder = new PCMMGateReqBuilder();
        gateBuilder.build(ccap.getAmId());
        gateBuilder.build(addrSubId);
        // force gateSpec.Direction to align with SCN direction
        final ServiceClassName scn = gateReq.getTrafficProfile().getServiceClassName();
        if (scn != null) {
            gateBuilder.build(gateReq.getGateSpec(), direction);
        } else {
            // not an SCN gate
            gateBuilder.build(gateReq.getGateSpec(), null);
        }
        gateBuilder.build(gateReq.getTrafficProfile());

        // pick a classifier type (only one for now)
        if (gateReq.getClassifier() != null) {
            gateBuilder.build(gateReq.getClassifier());
        } else if (gateReq.getExtClassifier() != null) {
            gateBuilder.build(gateReq.getExtClassifier());
        } else if (gateReq.getIpv6Classifier() != null) {
            gateBuilder.build(gateReq.getIpv6Classifier());
        }
        // assemble the final gate request
        return gateBuilder.getGateReq();
    }

    private class MockSocket extends Socket {

        private ByteArrayOutputStream os = new ByteArrayOutputStream();
        private ByteArrayInputStream is;

        @Override
        public OutputStream getOutputStream() {
            return os;
        }

        @Override
        public InputStream getInputStream() {
            if (is == null) is = new ByteArrayInputStream(os.toByteArray());
            return is;
        }
    }

}

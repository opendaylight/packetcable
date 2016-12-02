/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.opendaylight.controller.packetcable.provider;

import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.TpProtocol;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ccaps.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.Classifiers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.ClassifierContainer;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.classifier.choice.QosClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.traffic.profile.TrafficProfile;
import org.pcmm.PCMMPdpAgent;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.rcd.IPCMMClient;
import org.pcmm.rcd.impl.CMTS;
import org.pcmm.rcd.impl.CMTSConfig;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSMsgParser;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * Tests the PCMMService's ability to connect to a CMTS. Gate additions will not properly work as there is currently
 * not any other means to receive acknowledgements. This functionality must be tested by the PCMMService's client
 * PacketcableProvider.
 */
public class PCMMServiceTest {

    private static final String ccapId = "ccap-1";
    private static final String gatePath = "testGatePath";

    /**
     * Denotes whether or not a real CMTS is being tested against.
     * Ensure the checked-in value is always false else tests will most likely fail.
     */
    private static final boolean realCmts = false;


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
    private Ccap ccap;

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
        invalidCmAddrInet = InetAddress.getByAddress(new byte[] {99, 99, 99, 99});

        if (realCmts) {
            cmAddrInet = InetAddress.getByAddress(new byte[] {10, 32, 110, (byte)172});


            // Use me when testing against a CMTS or emulator not running in the same JVM
            cmtsAddr = new Ipv4Address("10.32.10.3");
            ccap = makeCcapObj(PCMMPdpAgent.WELL_KNOWN_PDP_PORT, cmtsAddr, ccapId);
        } else {
            cmAddrInet = InetAddress.getByAddress(new byte[] {10, 32, 110, (byte)180});

            // Use me for automated testing and the CMTS emulator running in the same JVM
            cmtsAddr = new Ipv4Address("127.0.0.1");

            final Set<String> upSCN = new HashSet<>();
            upSCN.add("extrm_up");
            final Set<String> dnSCN = new HashSet<>();
            dnSCN.add("extrm_dn");

            final Map<String, Boolean> cmStatus = new HashMap<>();
            cmStatus.put(cmAddrInet.getHostAddress(), true);
            cmStatus.put(invalidCmAddrInet.getHostAddress(), false);

            CMTSConfig config = new CMTSConfig(0, (short)4, upSCN, dnSCN, cmStatus);

            icmts = new CMTS(config);
            icmts.startServer();

            ccap = makeCcapObj(icmts.getPort(), cmtsAddr, ccapId);
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
        final int port;
        if (icmts != null) port = icmts.getPort() + 1;
        else port = PCMMPdpAgent.WELL_KNOWN_PDP_PORT + 1;
        ccap = makeCcapObj(port, cmtsAddr, ccapId);
        service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
        final String message = service.addCcap();
        Assert.assertNotNull(message);
        final String expectedMsg = "404 Not Found - CCAP " + ccapId + " failed to connect @ " + cmtsAddr.getValue()
                + ':' + port + " - ";
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
                + " returned error - Error Code: 13 Subcode: 0  Invalid SubscriberID";
        addInvalidGate(service, "extrm_up", srcAddr, dstAddr, ServiceFlowDirection.Us, invalidCmAddrInet, gatePath,
                expectedMsgStart);
    }

    @Test
    public void testAddInvalidScnUpGate() throws Exception {
        final String expectedMsgStart = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath
                + " returned error - Error Code: 11 Subcode: 0  Undefined Service Class Name";
        addInvalidGate(service, "extrm_up_invalid", srcAddr, dstAddr, ServiceFlowDirection.Us, cmAddrInet, gatePath,
                expectedMsgStart);
    }

    @Test
    public void testAddInvalidScnDownGate() throws Exception {
        final String expectedMsgStart = "404 Not Found - sendGateSet for " + ccapId + '/' + gatePath
                + " returned error - Error Code: 11 Subcode: 0  Undefined Service Class Name";
        addInvalidGate(service, "extrm_dn_invalid", srcAddr, dstAddr, ServiceFlowDirection.Ds, cmAddrInet, gatePath,
                expectedMsgStart);
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
        final Gate gate = makeGateObj("extrm_up", cmtsAddr, direction, new Ipv4Address("127.0.0.1"));
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

    private void addInvalidGate(final PCMMService service, final String scnName, final Ipv4Address srcAddr,
            final Ipv4Address dstAddr, final ServiceFlowDirection direction,
            final InetAddress cmAddrInet, final String gatePath,
            final String expGateSetMsgStart) {
        connectToCmts(service);
        final int numRequestsBefore = service.gateRequests.size();
        addAndValidateGate(service, scnName, srcAddr, dstAddr, direction, cmAddrInet, gatePath, expGateSetMsgStart);
        assertEquals(numRequestsBefore, service.gateRequests.size());
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
        final Gate gate = makeGateObj(scnName, srcAddr, direction, dstAddr);

//        final String gateSetMsg = service.sendGateSet(gatePath, cmAddrInet, gate, direction);
//        Assert.assertNotNull(gateSetMsg);
//        Assert.assertTrue(gateSetMsg, gateSetMsg.startsWith(expGateSetMsgStart));

        // TODO update this method for the new GateSendStatus object
        PCMMService.GateSendStatus status = service.sendGateSet(gatePath, cmAddrInet, gate);
        Assert.assertNotNull(status);
        assertThat(status.getMessage(), startsWith(expGateSetMsgStart));


        // TODO - add validation to the PCMMGateReq contained within the map
        if (status.didSucceed()) {
            Assert.assertTrue((service.gateRequests.containsKey(gatePath)));
            Assert.assertNotNull(service.gateRequests.get(gatePath));
        }
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
     * Creates a mock Ccap object that can be used for connecting to a CMTS
     * @param inPort - the CMTS port number
     * @param ipAddr - the CMTS IPv4 address string
     * @param ccapId - the ID of the CCAP
     * @return - the mock Ccap object
     */
    private Ccap makeCcapObj(final int inPort, final Ipv4Address ipAddr, final String ccapId) {
        final Ccap ccap = mock(Ccap.class);
        final Connection conn = mock(Connection.class);
        when(ccap.getConnection()).thenReturn(conn);
        final PortNumber port = mock(PortNumber.class);
        when(conn.getPort()).thenReturn(port);
        when(port.getValue()).thenReturn(inPort);

        final IpAddress addr = mock(IpAddress.class);
        when(conn.getIpAddress()).thenReturn(addr);
        when(addr.getIpv4Address()).thenReturn(ipAddr);

        when(ccap.getCcapId()).thenReturn(ccapId);
        final AmId amid = mock(AmId.class);
        when(ccap.getAmId()).thenReturn(amid);
        when(amid.getAmTag()).thenReturn(0xcada);
        when(amid.getAmType()).thenReturn(1);

        return ccap;
    }

    /**
     * Creates a mock Gate object
     * @param scnValue - the service class name defined on the CMTS
     * @param dstAddr - the CM address this gate should be set against
     * @return - the gate request
     */
    private Gate makeGateObj(final String scnValue, final Ipv4Address srcAddr, final ServiceFlowDirection direction,
                              final Ipv4Address dstAddr) {
        final Gate gate = mock(Gate.class);
        final GateSpec gateSpec = mock(GateSpec.class);
        when(gate.getGateSpec()).thenReturn(gateSpec);
        when(gateSpec.getDirection()).thenReturn(direction);
        // TODO - make sure to write a test when this value is not null
        when(gateSpec.getDscpTosOverwrite()).thenReturn(null);
        final TrafficProfile trafficProfile = mock(TrafficProfile.class);
        final ServiceClassName scn = mock(ServiceClassName.class);
        when(scn.getValue()).thenReturn(scnValue);
        when(trafficProfile.getServiceClassName()).thenReturn(scn);
        when(gate.getTrafficProfile()).thenReturn(trafficProfile);

        // TODO - write tests when this is null and ExtClassifier or Ipv6Classifier objects are not null
        final Classifier classifier = mock(Classifier.class);

        // This is the address of the CM
        when(classifier.getDstIp()).thenReturn(dstAddr);

        final PortNumber dstPort = new PortNumber(4321);
        when(classifier.getDstPort()).thenReturn(dstPort);
        final TpProtocol protocol = new TpProtocol(0);
        when(classifier.getProtocol()).thenReturn(protocol);
        when(classifier.getSrcIp()).thenReturn(srcAddr);
        final PortNumber srcPort = new PortNumber(1234);
        when(classifier.getSrcPort()).thenReturn(srcPort);

        // TODO - Can this value be any other value than 0 or 1 (See TosByte enumeration)
        final TosByte tosByte = new TosByte((short)0);
        when(classifier.getTosByte()).thenReturn(tosByte);
        final TosByte tosMask = new TosByte((short)224);
        when(classifier.getTosMask()).thenReturn(tosMask);

        final QosClassifierChoice classifierChoice = mock(QosClassifierChoice.class);
        when(classifierChoice.getClassifier()).thenReturn(classifier);

        ClassifierContainer classifierContainer = mock(ClassifierContainer.class);
        when(classifierContainer.getClassifierChoice()).thenReturn(classifierChoice);

        final List<ClassifierContainer> containerList = Collections.singletonList(classifierContainer);

        Classifiers classifiers = mock(Classifiers.class);
        when(classifiers.getClassifierContainer()).thenReturn(containerList);

        when(gate.getClassifiers()).thenReturn(classifiers);

        return gate;
    }

    private IPCMMGate makeGateRequest(final Ccap ccap, final Gate gateReq, final InetAddress addrSubId,
                                     final ServiceFlowDirection direction) {
        final PCMMGateReqBuilder gateBuilder = new PCMMGateReqBuilder();
        gateBuilder.setAmId(ccap.getAmId());
        gateBuilder.setSubscriberId(addrSubId);
        // force gateSpec.Direction to align with SCN direction
        final ServiceClassName scn = gateReq.getTrafficProfile().getServiceClassName();
        if (scn != null) {
            gateBuilder.setGateSpec(gateReq.getGateSpec(), direction);
        } else {
            // not an SCN gate
            gateBuilder.setGateSpec(gateReq.getGateSpec(), null);
        }
        gateBuilder.setTrafficProfile(gateReq.getTrafficProfile());

        gateBuilder.setClassifiers(gateReq.getClassifiers().getClassifierContainer());

        // assemble the final gate request
        return gateBuilder.build();
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

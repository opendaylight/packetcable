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
import org.pcmm.gates.IPCMMGate;
import org.pcmm.rcd.IPCMMClient;
import org.pcmm.rcd.impl.CMTS;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

    /**
     * The mock CMTS running on localhost with a dynamic port assigned.
     */
    private CMTS icmts;

    /**
     * Represents the IP address of the CM the gate should be set against.
     */
    private byte[] cmAddr = new byte[4];

    @Before
    public void setup() {
        icmts = new CMTS();
        icmts.startServer();

        cmAddr[0] = 10;
        cmAddr[1] = 32;
        cmAddr[2] = 110;
        cmAddr[3] = (byte)180;
    }

    @After
    public void tearDown() {
        if (icmts != null) icmts.stopServer();
    }

    @Test
    public void testAddCcap() {

        // TODO - comment out for testing
        //        final Ipv4Address cmtsAddr = new Ipv4Address("10.32.10.3");
//        final Ccaps ccap = makeCcapsObj(PCMMPdpAgent.WELL_KNOWN_PDP_PORT, cmtsAddr.getValue(), "ccap-1");

        // TODO - Use this instead for automated testing
        final Ipv4Address cmtsAddr = new Ipv4Address("127.0.0.1");
        final Ccaps ccap = makeCcapsObj(icmts.getPort(), cmtsAddr.getValue(), "ccap-1");

        final PCMMService service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
        final String message = service.addCcap();
        Assert.assertNotNull(message);
        Assert.assertTrue(message, message.startsWith("200"));
        Assert.assertNotNull(service.ccapClient.pcmmPdp.getClientHandle());
        service.disconect();
    }

    @Test
    public void testAddAndRemoveUpGate() throws Exception {

        // TODO - Use this block to test against a real CMTS
//        final Ipv4Address cmtsAddr = new Ipv4Address("10.32.10.3");
//        final Ccaps ccap = makeCcapsObj(PCMMPdpAgent.WELL_KNOWN_PDP_PORT, cmtsAddr.getValue(), "ccap-1");

        // TODO - Use this block for automated testing
        final Ipv4Address cmtsAddr = new Ipv4Address("127.0.0.1");
        final Ccaps ccap = makeCcapsObj(icmts.getPort(), cmtsAddr.getValue(), "ccap-1");

        final PCMMService service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
        service.addCcap();

        final Gates gate = makeGateObj("extrm_up", cmtsAddr, ServiceFlowDirection.Us);
        final String gatePath = "testGatePath";

        // Add gate
        final String msg = service.sendGateSet(gatePath, InetAddress.getByAddress(cmAddr), gate,
                ServiceFlowDirection.Us);
        Assert.assertTrue(msg, msg.startsWith("200"));

        // TODO - add validation to the PCMMGateReq contained within the map
        Assert.assertEquals(1, service.gateRequests.size());

        // Remove gate
        service.sendGateDelete(gatePath);

        // Wait up to 1 sec for response to be processed
        final long start = System.currentTimeMillis();
        while (1000 < System.currentTimeMillis() - start) {
            if (service.gateRequests.size() == 0) break;
        }
        Assert.assertEquals(0, service.gateRequests.size());
        service.disconect();
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
        final Ccaps ccap = makeCcapsObj(icmts.getPort(), "127.0.0.1", "ccap-1");
        final Ipv4Address cmtsAddr = new Ipv4Address("127.0.0.1");
        final Gates gate = makeGateObj("extrm_up", cmtsAddr, ServiceFlowDirection.Us);
        final IPCMMGate gateReq = makeGateRequest(ccap, gate, InetAddress.getByName("localhost"),
                ServiceFlowDirection.Us);
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
     * Creates a mock Ccaps object that can be used for connecting to a CMTS
     * @param inPort - the CMTS port number
     * @param ipAddr - the CMTS IPv4 address string
     * @param ccapId - the ID of the CCAP
     * @return - the mock Ccaps object
     */
    private Ccaps makeCcapsObj(final int inPort, final String ipAddr, final String ccapId) {
        final Ccaps ccap = Mockito.mock(Ccaps.class);
        final Connection conn = Mockito.mock(Connection.class);
        Mockito.when(ccap.getConnection()).thenReturn(conn);
        final PortNumber port = Mockito.mock(PortNumber.class);
        Mockito.when(conn.getPort()).thenReturn(port);
        Mockito.when(port.getValue()).thenReturn(inPort);

        final IpAddress addr = Mockito.mock(IpAddress.class);
        Mockito.when(conn.getIpAddress()).thenReturn(addr);
        final Ipv4Address ipv4 = new Ipv4Address(ipAddr);
        Mockito.when(addr.getIpv4Address()).thenReturn(ipv4);

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
    private Gates makeGateObj(final String scnValue, final Ipv4Address dstAddr, final ServiceFlowDirection direction) {
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
        final Ipv4Address srcAddr = new Ipv4Address("127.0.0.1");
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

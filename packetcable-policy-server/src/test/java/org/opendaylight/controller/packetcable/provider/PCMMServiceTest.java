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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.traffic.profile.TrafficProfile;
import org.pcmm.rcd.IPCMMClient;
import org.pcmm.rcd.impl.CMTS;

import java.net.InetAddress;

/**
 * Tests the PCMMService's ability to connect to a CMTS. Gate additions will not properly work as there is currently
 * not any other means to receive acknowledgements. This functionality must be tested by the PCMMService's client
 * PacketcableProvider
 */
public class PCMMServiceTest {

    private Ccaps ccap;
    private CMTS icmts;

    @Before
    public void setup() {
        icmts = new CMTS();
        icmts.startServer();

        ccap = Mockito.mock(Ccaps.class);
        final Connection conn = Mockito.mock(Connection.class);
        Mockito.when(ccap.getConnection()).thenReturn(conn);
        final PortNumber port = Mockito.mock(PortNumber.class);
        Mockito.when(conn.getPort()).thenReturn(port);
        Mockito.when(port.getValue()).thenReturn(icmts.getPort());
        final IpAddress addr = Mockito.mock(IpAddress.class);
        Mockito.when(conn.getIpAddress()).thenReturn(addr);
        final Ipv4Address ipv4 = Mockito.mock(Ipv4Address.class);
        Mockito.when(addr.getIpv4Address()).thenReturn(ipv4);

        // Switch the value to a real CMTS here to test against it
        // Ensure the code is checked in with 127.0.0.1
        Mockito.when(ipv4.getValue()).thenReturn("127.0.0.1");

        // Following line has been commented out for testing against a real Arris CMTS in the Louisville development lab
//                Mockito.when(ipv4.getValue()).thenReturn("10.32.10.3");

        Mockito.when(ccap.getCcapId()).thenReturn("ccap-1");
        final AmId amid = Mockito.mock(AmId.class);
        Mockito.when(ccap.getAmId()).thenReturn(amid);
        Mockito.when(amid.getAmTag()).thenReturn(0xcada);
        Mockito.when(amid.getAmType()).thenReturn(1);
    }

    @After
    public void tearDown() {
        if (icmts != null) icmts.stopServer();
    }

    @Test
    public void testAddCcap() {
        final PCMMService service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
        final String message = service.addCcap();
        Assert.assertTrue(message.startsWith("200"));
        Assert.assertNotNull(service.ccapClient.pcmmPdp.getClientHandle());

        // TODO - remove this sleep
/*
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
    }

//    @Test
    public void testAddGate() throws Exception {
        final PCMMService service = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
        service.addCcap();
        final byte[] addr = new byte[4];
        addr[0] = 10;
        addr[1] = 32;
        addr[2] = 110;
        addr[3] = (byte)180;

        final Gates gate = Mockito.mock(Gates.class);
        final GateSpec gateSpec = Mockito.mock(GateSpec.class);
        Mockito.when(gate.getGateSpec()).thenReturn(gateSpec);
        Mockito.when(gateSpec.getDirection()).thenReturn(ServiceFlowDirection.Us);
        // TODO - make sure to write a test when this value is not null
        Mockito.when(gateSpec.getDscpTosOverwrite()).thenReturn(null);
        final TrafficProfile trafficProfile = Mockito.mock(TrafficProfile.class);
        final ServiceClassName scn = Mockito.mock(ServiceClassName.class);
        Mockito.when(scn.getValue()).thenReturn("extrm_up");
        Mockito.when(trafficProfile.getServiceClassName()).thenReturn(scn);
        Mockito.when(gate.getTrafficProfile()).thenReturn(trafficProfile);

        // TODO - write tests when this is null and ExtClassifier or Ipv6Classifier objects are not null
        final Classifier classifier = Mockito.mock(Classifier.class);
        Mockito.when(gate.getClassifier()).thenReturn(classifier);
        Mockito.when(gate.getExtClassifier()).thenReturn(null);
        Mockito.when(gate.getIpv6Classifier()).thenReturn(null);

        // TODO - remove this sleep
        Thread.sleep(1000);

        final String msg = service.sendGateSet("app1/10.32.110.180/gate2", InetAddress.getByAddress(addr), gate,
                ServiceFlowDirection.Us);
        Assert.fail(msg);
    }

}

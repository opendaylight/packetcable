package org.opendaylight.controller.packetcable.provider;


import static org.junit.Assert.*;


import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.net.InetAddresses;

@RunWith(MockitoJUnitRunner.class)
public class SubnetTest {

    @Test
    public final void testCreateInstance() throws UnknownHostException {
           Subnet localSubnet = Subnet.createInstance("192.168.0.0/255.255.255.0");
           assertNotNull(localSubnet);
           assertEquals("192.168.0.0/255.255.255.0", localSubnet.toString());
           assertEquals(Subnet.createInstance("192.168.0.0/255.255.255.0"), localSubnet);
    }
}

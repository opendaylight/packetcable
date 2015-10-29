package org.opendaylight.controller.packetcable.provider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PacketcableProviderTest {

    @Mock InetAddress inetAddress;
    @Mock PacketcableProvider packetCableProv;

    @Before
    public void setUp() throws Exception {
        when(packetCableProv.getInetAddress(any(String.class))).thenReturn(inetAddress);
    }

    @Test
    public final void testClose() throws ExecutionException, InterruptedException {
        packetCableProv.close();
        verify(packetCableProv, times(1)).close();
    }

    @Test
    public final void testGetInetAddress() {
      assertNotNull(packetCableProv.getInetAddress("127.0.0.1"));
      verify(packetCableProv).getInetAddress("127.0.0.1");
    }

}

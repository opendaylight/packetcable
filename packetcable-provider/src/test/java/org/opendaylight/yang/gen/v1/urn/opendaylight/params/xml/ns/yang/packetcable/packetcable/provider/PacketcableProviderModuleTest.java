package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.packetcable.packetcable.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.packetcable.provider.OpendaylightPacketcableProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.packetcable.packetcable.provider.impl.rev140131.PacketcableProviderModule;

@RunWith(MockitoJUnitRunner.class)
public class PacketcableProviderModuleTest {

    @Mock
    private OpendaylightPacketcableProvider provider;
    @InjectMocks
    private PacketcableProviderModule packetCableProviderModuleMock = mock(PacketcableProviderModule.class);

    @Before
    public void setUp() throws Exception {
        when(packetCableProviderModuleMock.createInstance()).thenReturn(
                provider);
    }

    @Test
    public final void testCreateInstance() {
        assertNotNull(packetCableProviderModuleMock.createInstance());
        verify(packetCableProviderModuleMock, times(1)).createInstance();
    }

}

package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.packetcable.packetcable.provider.impl.rev140131;

import org.opendaylight.controller.packetcable.provider.OpendaylightPacketcableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketcableProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.packetcable.packetcable.provider.impl.rev140131.AbstractPacketcableProviderModule {
    private static final Logger logger = LoggerFactory.getLogger(PacketcableProviderModule.class);
    public PacketcableProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PacketcableProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.packetcable.packetcable.provider.impl.rev140131.PacketcableProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        OpendaylightPacketcableProvider provider = new OpendaylightPacketcableProvider();
        this.getBrokerDependency().registerProvider(provider, null);
        logger.info("PacketCableProvider Registered with Broker");
        return provider;
    }

}

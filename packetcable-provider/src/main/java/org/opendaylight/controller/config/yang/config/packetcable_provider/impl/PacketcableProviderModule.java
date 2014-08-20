package org.opendaylight.controller.config.yang.config.packetcable_provider.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;

public class PacketcableProviderModule
		extends
		org.opendaylight.controller.config.yang.config.packetcable_provider.impl.AbstractPacketcableProviderModule {
	public PacketcableProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
		super(identifier, dependencyResolver);
	}

	public PacketcableProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.packetcable_provider.impl.PacketcableProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
		super(identifier, dependencyResolver, oldModule, oldInstance);
	}

	@Override
	public void customValidation() {
		// add custom validation form module attributes here.
	}
//
	@Override
	public java.lang.AutoCloseable createInstance() {
//		final OpendaylightPacketcableProvider opendaylightPcmmProvider = new OpendaylightPacketcableProvider();
//
//		// Register to md-sal
//		opendaylightPcmmProvider.setNotificationProvider(getNotificationServiceDependency());
//
//		DataBroker dataBrokerService = getDataBrokerDependency();
//		opendaylightPcmmProvider.setDataProvider(dataBrokerService);
// 
//		final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration = dataBrokerService.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, OpendaylightPacketcableProvider.TOASTER_IID, opendaylightPcmmProvider, DataChangeScope.SUBTREE);
//
//		final BindingAwareBroker.RpcRegistration<ToasterService> rpcRegistration = getRpcRegistryDependency().addRpcImplementation(ToasterService.class, opendaylightPcmmProvider);
//
//		// Register runtimeBean for toaster statistics via JMX
//		final PacketcableProviderRuntimeRegistration runtimeReg = getRootRuntimeBeanRegistratorWrapper().register(opendaylightPcmmProvider);
//
//		// Wrap PCMM driver as AutoCloseable and close registrations to md-sal at
//		// close()
//		final class AutoCloseablePcmmProvider implements AutoCloseable {
//
//			@Override
//			public void close() throws Exception {
//				dataChangeListenerRegistration.close();
//				rpcRegistration.close();
//				runtimeReg.close();
//				opendaylightPcmmProvider.close();
//			
//				log.info("Toaster provider (instance {}) torn down.", this);
//			}
//		}
//
//		AutoCloseable ret = new AutoCloseablePcmmProvider();
//		log.info("Toaster provider (instance {}) initialized.", ret);
//		return ret;
		return null;
	}

}

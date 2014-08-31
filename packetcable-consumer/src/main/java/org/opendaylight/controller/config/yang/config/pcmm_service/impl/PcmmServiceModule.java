package org.opendaylight.controller.config.yang.config.pcmm_service.impl;

import org.opendaylight.controller.org.pcmm.api.PcmmService;
import org.opendaylight.controller.org.pcmm.impl.PcmmServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.PacketcableServiceService;
import org.opendaylight.yangtools.concepts.Registration;

public class PcmmServiceModule
		extends
		org.opendaylight.controller.config.yang.config.pcmm_service.impl.AbstractPcmmServiceModule {
	public PcmmServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
		super(identifier, dependencyResolver);
	}

	public PcmmServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.pcmm_service.impl.PcmmServiceModule oldModule, java.lang.AutoCloseable oldInstance) {
		super(identifier, dependencyResolver, oldModule, oldInstance);
	}

	@Override
	public void customValidation() {
		// add custom validation form module attributes here.
	}

	@Override
	public java.lang.AutoCloseable createInstance() {
		PacketcableServiceService packetcableServiceService = getRpcRegistryDependency().getRpcService(PacketcableServiceService.class);
		final PcmmService pcmmService = new PcmmServiceImpl(packetcableServiceService);
		final Registration pcmmListenerReg = getNotificationServiceDependency().registerNotificationListener(pcmmService);
		final PcmmServiceRuntimeRegistration runtimeReg = getRootRuntimeBeanRegistratorWrapper().register(pcmmService);
		return new AutoCloseablePcmmService(packetcableServiceService, pcmmListenerReg, runtimeReg);
	}

	class AutoCloseablePcmmService extends PcmmServiceImpl implements
			AutoCloseable {
		
		private PcmmServiceRuntimeRegistration runtimeReg;
		private Registration pcmmListenerReg;

		public AutoCloseablePcmmService(PacketcableServiceService packetcableServiceService, Registration pcmmListenerReg, PcmmServiceRuntimeRegistration runtimeReg) {
			super(packetcableServiceService);
			this.runtimeReg = runtimeReg;
			this.pcmmListenerReg = pcmmListenerReg;
		}

		@Override
		public void close() throws Exception {
			pcmmListenerReg.close();
			runtimeReg.close();
		}

	}
}

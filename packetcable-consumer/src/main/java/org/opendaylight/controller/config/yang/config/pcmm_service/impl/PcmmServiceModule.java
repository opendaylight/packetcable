package org.opendaylight.controller.config.yang.config.pcmm_service.impl;

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
		throw new UnsupportedOperationException("Implement me");
	}

}

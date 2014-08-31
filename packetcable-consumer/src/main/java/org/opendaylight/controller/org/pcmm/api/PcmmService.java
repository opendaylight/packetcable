package org.opendaylight.controller.org.pcmm.api;

import org.opendaylight.controller.config.yang.config.pcmm_service.impl.PcmmServiceRuntimeMXBean;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.PacketcableServiceListener;

public interface PcmmService extends PcmmServiceRuntimeMXBean,
		PacketcableServiceListener {

}

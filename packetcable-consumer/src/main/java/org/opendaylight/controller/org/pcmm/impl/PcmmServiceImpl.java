package org.opendaylight.controller.org.pcmm.impl;

import java.util.Iterator;
import java.util.Map;

import org.opendaylight.controller.org.pcmm.api.PcmmService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsUpdated;
import org.pcmm.rcd.IPCMMPolicyServer;
import org.pcmm.rcd.IPCMMPolicyServer.IPSCMTSClient;
import org.pcmm.rcd.impl.PCMMPolicyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class PcmmServiceImpl implements PcmmService {

	private static final Logger logger = LoggerFactory.getLogger(PcmmServiceImpl.class);
	private Map<IpAddress, IPSCMTSClient> cmtsClients;
	private IPCMMPolicyServer policyServer;

	public PcmmServiceImpl() {
		policyServer = new PCMMPolicyServer();
		cmtsClients = Maps.newConcurrentMap();
	}

	@Override
	public void onCmtsAdded(CmtsAdded notification) {
		String ipv4 = notification.getAddress().getIpv4Address().getValue();
		IPSCMTSClient client = policyServer.requestCMTSConnection(ipv4);
		if (client.isConnected()) {
			cmtsClients.put(notification.getAddress(), client);
		}
	}

	@Override
	public void onCmtsRemoved(CmtsRemoved notification) {
		if (cmtsClients.containsKey(notification.getAddress())) {
			IPSCMTSClient client = cmtsClients.remove(notification.getAddress());
			client.disconnect();
		}
	}

	@Override
	public void onCmtsUpdated(CmtsUpdated notification) {
		// TODO
	}

	@Override
	public Boolean sendGateDelete() {
		// TODO change me
		boolean ret = true;
		for (Iterator<IPSCMTSClient> iter = cmtsClients.values().iterator(); iter.hasNext();)
			ret &= cmtsClients.get(0).gateDelete();
		return ret;
	}

	@Override
	public Boolean sendGateSynchronize() {
		boolean ret = true;
		for (Iterator<IPSCMTSClient> iter = cmtsClients.values().iterator(); iter.hasNext();)
			ret &= cmtsClients.get(0).gateSynchronize();
		return ret;
	}

	@Override
	public Boolean sendGateInfo() {
		boolean ret = true;
		for (Iterator<IPSCMTSClient> iter = cmtsClients.values().iterator(); iter.hasNext();)
			ret &= cmtsClients.get(0).gateInfo();
		return ret;
	}

	@Override
	public Boolean sendGateSet() {
		boolean ret = true;
		for (Iterator<IPSCMTSClient> iter = cmtsClients.values().iterator(); iter.hasNext();)
			ret &= cmtsClients.get(0).gateSet();
		return ret;
	}
}

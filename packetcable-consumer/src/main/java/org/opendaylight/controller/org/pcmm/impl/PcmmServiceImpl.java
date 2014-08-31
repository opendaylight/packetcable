package org.opendaylight.controller.org.pcmm.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.opendaylight.controller.org.pcmm.api.PcmmService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140120.CmtsReference;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.PacketcableServiceService;
import org.pcmm.rcd.IPCMMPolicyServer;
import org.pcmm.rcd.IPCMMPolicyServer.IPSCMTSClient;
import org.pcmm.rcd.impl.PCMMPolicyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class PcmmServiceImpl implements PcmmService {

	private static final Logger log = LoggerFactory.getLogger(PcmmServiceImpl.class);
	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	private PacketcableServiceService packetcableServiceService;
	private List<IpAddress> cmtsList;
	private Map<CmtsReference, IPSCMTSClient> cmtsClients;
	private IPCMMPolicyServer policyServer;

	public PcmmServiceImpl(PacketcableServiceService packetcableServiceService) {
		this.packetcableServiceService = packetcableServiceService;
		policyServer = new PCMMPolicyServer();
		cmtsClients = Maps.newConcurrentMap();
		cmtsList = Lists.newArrayList();
	}

	@Override
	public void onCmtsAdded(CmtsAdded notification) {
		String ipv4 = notification.getId().getIpv4Address().getValue();
		IPSCMTSClient client = policyServer.requestCMTSConnection(ipv4);
		if (client.isConnected()) {
			cmtsClients.put(notification.getCmtsRef(), client);
			cmtsList.add(notification.getId());
		}
	}

	@Override
	public void onCmtsRemoved(CmtsRemoved notification) {
		if (cmtsList.contains(notification.getId()))
			cmtsList.remove(notification.getId());
		if (cmtsClients.containsKey(notification.getCmtsRef())) {
			IPSCMTSClient client = cmtsClients.remove(notification.getCmtsRef());
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

package org.opendaylight.controller.packetcable.provider;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileBestEffortAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileDocsisServiceClassNameAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileFlowspecAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.ITrafficProfile;

public interface OpenDaylightPacketCableProviderService {

	public ITrafficProfile buildTrafficProfile(TrafficProfileBestEffortAttributes bestEffort);

	public ITrafficProfile buildTrafficProfile(TrafficProfileFlowspecAttributes flowSpec);

	public ITrafficProfile buildTrafficProfile(TrafficProfileDocsisServiceClassNameAttributes docsis);

	public IClassifier buildClassifier(Match sid);

}

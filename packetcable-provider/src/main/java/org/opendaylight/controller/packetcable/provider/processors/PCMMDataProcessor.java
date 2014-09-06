/**
 * 
 */
package org.opendaylight.controller.packetcable.provider.processors;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileBestEffortAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileDocsisServiceClassNameAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileFlowspecAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.traffic.profile.best.effort.attributes.BeAuthorizedEnvelope;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.traffic.profile.best.effort.attributes.BeCommittedEnvelope;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.traffic.profile.best.effort.attributes.BereservedEnvelope;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.SubscriberIdRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.TcpMatchRangesAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.TcpMatchRangesRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.tcp.match.ranges.attributes.TcpMatchRanges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.udp.match.ranges.attributes.UpdMatchRanges;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.gates.impl.BestEffortService;
import org.pcmm.gates.impl.DOCSISServiceClassNameTrafficProfile;
import org.pcmm.gates.impl.ExtendedClassifier;
import org.pcmm.gates.impl.BestEffortService.BEEnvelop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * PacketCable data processor
 * 
 */
public class PCMMDataProcessor {

	private Logger logger = LoggerFactory.getLogger(PCMMDataProcessor.class);

	public ITrafficProfile process(TrafficProfileBestEffortAttributes bestEffort) {
		BestEffortService trafficProfile = new BestEffortService(BestEffortService.DEFAULT_ENVELOP);
		getBEAuthorizedEnvelop(bestEffort, trafficProfile);
		getBEReservedEnvelop(bestEffort, trafficProfile);
		getBECommittedEnvelop(bestEffort, trafficProfile);
		return trafficProfile;
	}

	public ITrafficProfile process(TrafficProfileDocsisServiceClassNameAttributes docsis) {
		DOCSISServiceClassNameTrafficProfile trafficProfile = new DOCSISServiceClassNameTrafficProfile();
		trafficProfile.setServiceClassName(docsis.getServiceClassName());
		return trafficProfile;
	}

	// TODO
	public ITrafficProfile process(TrafficProfileFlowspecAttributes flowSpec) {
		throw new UnsupportedOperationException("Not impelemnted yet");
	}

	public IClassifier process(Match match) {
		ExtendedClassifier classifier = new ExtendedClassifier();
		classifier.setProtocol(IClassifier.Protocol.NONE);
		getUdpMatchRangeValues(match.getAugmentation(UdpMatchRangesRpcAddFlow.class), classifier);
		getTcpMatchRangesValues(match.getAugmentation(TcpMatchRangesRpcAddFlow.class), classifier);
		SubscriberIdRpcAddFlow subId = match.getAugmentation(SubscriberIdRpcAddFlow.class);
		Ipv6Address ipv6Address = subId.getSubscriberId().getIpv6Address();
		if (ipv6Address != null)
			try {
				classifier.setDestinationIPAddress(InetAddress.getByName(ipv6Address.getValue()));
			} catch (UnknownHostException e) {
				logger.error(e.getMessage());
			}

		Ipv4Address ipv4Address = subId.getSubscriberId().getIpv4Address();
		if (ipv4Address != null)
			try {
				classifier.setDestinationIPAddress(InetAddress.getByName(ipv4Address.getValue()));
			} catch (UnknownHostException e) {
				logger.error(e.getMessage());
			}
		return classifier;
	}

	private void getBECommittedEnvelop(TrafficProfileBestEffortAttributes bestEffort, BestEffortService trafficProfile) {
		BEEnvelop committedEnvelop = trafficProfile.getCommittedEnvelop();
		BeCommittedEnvelope beCommittedEnvelope = bestEffort.getBeCommittedEnvelope();
		if (beCommittedEnvelope.getTrafficPriority() != null)
			committedEnvelop.setTrafficPriority(beCommittedEnvelope.getTrafficPriority().byteValue());
		else
			committedEnvelop.setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
		if (beCommittedEnvelope.getMaximumTrafficBurst() != null)
			committedEnvelop.setMaximumTrafficBurst(beCommittedEnvelope.getMaximumTrafficBurst().intValue());
		else
			committedEnvelop.setMaximumTrafficBurst(BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
		if (beCommittedEnvelope.getRequestTransmissionPolicy() != null)
			committedEnvelop.setRequestTransmissionPolicy(beCommittedEnvelope.getRequestTransmissionPolicy().intValue());
		// else
		// committedEnvelop.setRequestTransmissionPolicy(PCMMGlobalConfig.BETransmissionPolicy);
		if (beCommittedEnvelope.getMaximumSustainedTrafficRate() != null)
			committedEnvelop.setMaximumSustainedTrafficRate(beCommittedEnvelope.getMaximumSustainedTrafficRate().intValue());
		// else
		// committedEnvelop.setMaximumSustainedTrafficRate(PCMMGlobalConfig.DefaultLowBestEffortTrafficRate);
	}

	private void getBEReservedEnvelop(TrafficProfileBestEffortAttributes bestEffort, BestEffortService trafficProfile) {
		BEEnvelop reservedEnvelop = trafficProfile.getReservedEnvelop();
		BereservedEnvelope beReservedEnvelope = bestEffort.getBereservedEnvelope();
		if (beReservedEnvelope.getTrafficPriority() != null)
			reservedEnvelop.setTrafficPriority(beReservedEnvelope.getTrafficPriority().byteValue());
		else
			reservedEnvelop.setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
		if (beReservedEnvelope.getMaximumTrafficBurst() != null)
			reservedEnvelop.setMaximumTrafficBurst(beReservedEnvelope.getMaximumTrafficBurst().intValue());
		else
			reservedEnvelop.setMaximumTrafficBurst(BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
		if (beReservedEnvelope.getRequestTransmissionPolicy() != null)
			reservedEnvelop.setRequestTransmissionPolicy(beReservedEnvelope.getRequestTransmissionPolicy().intValue());
		if (beReservedEnvelope.getMaximumSustainedTrafficRate() != null)
			reservedEnvelop.setMaximumSustainedTrafficRate(beReservedEnvelope.getMaximumSustainedTrafficRate().intValue());
	}

	private void getBEAuthorizedEnvelop(TrafficProfileBestEffortAttributes bestEffort, BestEffortService trafficProfile) {
		BEEnvelop authorizedEnvelop = trafficProfile.getAuthorizedEnvelop();
		BeAuthorizedEnvelope beAuthorizedEnvelope = bestEffort.getBeAuthorizedEnvelope();
		if (beAuthorizedEnvelope.getTrafficPriority() != null)
			authorizedEnvelop.setTrafficPriority(beAuthorizedEnvelope.getTrafficPriority().byteValue());
		else
			authorizedEnvelop.setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
		if (beAuthorizedEnvelope.getMaximumTrafficBurst() != null)
			authorizedEnvelop.setMaximumTrafficBurst(beAuthorizedEnvelope.getMaximumTrafficBurst().intValue());
		else
			authorizedEnvelop.setMaximumTrafficBurst(BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
		if (beAuthorizedEnvelope.getRequestTransmissionPolicy() != null)
			authorizedEnvelop.setRequestTransmissionPolicy(beAuthorizedEnvelope.getRequestTransmissionPolicy().intValue());
		if (beAuthorizedEnvelope.getMaximumSustainedTrafficRate() != null)
			authorizedEnvelop.setMaximumSustainedTrafficRate(beAuthorizedEnvelope.getMaximumSustainedTrafficRate().intValue());
	}

	private void getTcpMatchRangesValues(TcpMatchRangesAttributes tcpRange, IExtendedClassifier classifier) {
		short srcPortStart, srcPortEnd, dstPortStart, dstPortEnd;
		srcPortStart = srcPortEnd = dstPortStart = dstPortEnd = 0;
		if (tcpRange != null) {
			classifier.setProtocol(IClassifier.Protocol.TCP);
			TcpMatchRanges tcpMatchRanges = tcpRange.getTcpMatchRanges();
			PortNumber tcpDestinationPortStart = tcpMatchRanges.getTcpDestinationPortBegin();
			if (tcpDestinationPortStart != null && tcpDestinationPortStart.getValue() != null)
				dstPortStart = tcpDestinationPortStart.getValue().shortValue();
			PortNumber tcpSourcePortStart = tcpMatchRanges.getTcpSourcePortStart();
			if (tcpSourcePortStart != null && tcpSourcePortStart.getValue() != null)
				srcPortStart = tcpSourcePortStart.getValue().shortValue();
			PortNumber tcpDestinationPortEnd = tcpMatchRanges.getTcpDestinationPortEnd();
			if (tcpDestinationPortEnd != null && tcpDestinationPortEnd.getValue() != null)
				dstPortEnd = tcpDestinationPortEnd.getValue().shortValue();
			PortNumber tcpSourcePortEnd = tcpMatchRanges.getTcpSourcePortEnd();
			if (tcpSourcePortEnd != null && tcpSourcePortEnd.getValue() != null)
				srcPortEnd = tcpSourcePortEnd.getValue().shortValue();
		}
		classifier.setDestinationPortStart(dstPortStart);
		classifier.setSourcePortStart(srcPortStart);
		classifier.setDestinationPortEnd(dstPortEnd);
		classifier.setSourcePortEnd(srcPortEnd);
	}

	private void getUdpMatchRangeValues(UdpMatchRangesAttributes updRange, IExtendedClassifier classifier) {
		short srcPortStart, srcPortEnd, dstPortStart, dstPortEnd;
		srcPortStart = srcPortEnd = dstPortStart = dstPortEnd = 0;
		if (updRange != null) {
			classifier.setProtocol(IClassifier.Protocol.UDP);
			UpdMatchRanges updMatchRanges = updRange.getUpdMatchRanges();
			PortNumber udpDestinationPortStart = updMatchRanges.getUdpDestinationPortStart();
			if (udpDestinationPortStart != null && udpDestinationPortStart.getValue() != null)
				dstPortStart = udpDestinationPortStart.getValue().shortValue();
			PortNumber udpSourcePortStart = updMatchRanges.getUdpSourcePortStart();
			if (udpSourcePortStart != null && udpSourcePortStart.getValue() != null)
				srcPortStart = udpSourcePortStart.getValue().shortValue();
			PortNumber udpDestinationPortEnd = updMatchRanges.getUdpDestinationPortEnd();
			if (udpDestinationPortEnd != null && udpDestinationPortEnd.getValue() != null)
				dstPortEnd = udpDestinationPortEnd.getValue().shortValue();
			PortNumber udpSourcePortEnd = updMatchRanges.getUdpSourcePortEnd();
			if (udpSourcePortEnd != null && udpSourcePortEnd.getValue() != null)
				srcPortEnd = udpSourcePortEnd.getValue().shortValue();
		}
		classifier.setDestinationPortStart(dstPortStart);
		classifier.setSourcePortStart(srcPortStart);
		classifier.setDestinationPortEnd(dstPortEnd);
		classifier.setSourcePortEnd(srcPortEnd);
	}

}

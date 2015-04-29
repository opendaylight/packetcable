/**
 * Build PCMM gate requests from API QoS Gate objects
 */
package org.opendaylight.controller.packetcable.provider;

import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.ext.classifier.ExtClassifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.ipv6.classifier.Ipv6Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.traffic.profile.TrafficProfile;
import org.pcmm.gates.*;
import org.pcmm.gates.IGateSpec.DSCPTOS;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.impl.DOCSISServiceClassNameTrafficProfile;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.SubscriberID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * PacketCable data processor
 *
 */
public class PCMMGateReqBuilder {

	private Logger logger = LoggerFactory.getLogger(PCMMGateReqBuilder.class);

	private PCMMGateReq gateReq = null;

	public PCMMGateReqBuilder() {
		gateReq = new org.pcmm.gates.impl.PCMMGateReq();
	}

	public PCMMGateReq getGateReq() {
		return gateReq;
	}

	public void build(AmId qosAmId){
		IAMID amId = new org.pcmm.gates.impl.AMID();
		amId.setApplicationMgrTag(qosAmId.getAmTag().shortValue());
		amId.setApplicationType(qosAmId.getAmType().shortValue());
        gateReq.setAMID(amId);
	}

	public void build(InetAddress qosSubId){
		ISubscriberID subId = new SubscriberID();
		subId.setSourceIPAddress(qosSubId);
		gateReq.setSubscriberID(subId);
	}

	public void build(GateSpec qosGateSpec, ServiceFlowDirection scnDirection) {
		IGateSpec gateSpec = new org.pcmm.gates.impl.GateSpec();
		// service flow direction
		ServiceFlowDirection qosDir = null;
		Direction gateDir = null;
		if (scnDirection != null) {
			qosDir = scnDirection;
		} else if (qosGateSpec.getDirection() != null) {
			qosDir = qosGateSpec.getDirection();
		}
		if (qosDir == ServiceFlowDirection.Ds) {
			gateDir = Direction.DOWNSTREAM;
		} else if (qosDir == ServiceFlowDirection.Us) {
			gateDir = Direction.UPSTREAM;
		}
		gateSpec.setDirection(gateDir);
		// DSCP/TOS Overwrite
		TosByte tosOverwrite = qosGateSpec.getDscpTosOverwrite();
		if (tosOverwrite != null) {
			byte gateTos = tosOverwrite.getValue().byteValue();
			gateSpec.setDSCP_TOSOverwrite(DSCPTOS.ENABLE);
			gateSpec.setDSCP_TOSOverwrite(gateTos);
			TosByte tosMask = qosGateSpec.getDscpTosMask();
			if (tosMask != null) {
				byte gateTosMask = tosMask.getValue().byteValue();
				gateSpec.setDSCP_TOSMask(gateTosMask);
			} else {
				gateSpec.setDSCP_TOSMask((byte)0xff);
			}
		}
		gateReq.setGateSpec(gateSpec);
	}

	public void build(TrafficProfile qosTrafficProfile) {
		if (qosTrafficProfile.getServiceClassName() != null) {
			String scn = qosTrafficProfile.getServiceClassName().getValue();
			DOCSISServiceClassNameTrafficProfile trafficProfile = new DOCSISServiceClassNameTrafficProfile();
			if (scn.length() <= 16) { // NB.16 char SCN is max length per PCMM spec
				trafficProfile.setServiceClassName(scn);
				gateReq.setTrafficProfile(trafficProfile);
			}
		}
	}

	private InetAddress getByName(String ipAddressStr){
		InetAddress ipAddress = null;
		try {
			ipAddress = InetAddress.getByName(ipAddressStr);
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		}
		return ipAddress;
	}

	public void build(Classifier qosClassifier) {
		// Legacy classifier
		IClassifier classifier = new org.pcmm.gates.impl.Classifier();
		classifier.setPriority((byte) 64);
		if (qosClassifier.getProtocol() != null){
			classifier.setProtocol(qosClassifier.getProtocol().getValue().shortValue());
		}
		if (qosClassifier.getSrcIp() != null) {
			InetAddress sip = getByName(qosClassifier.getSrcIp().getValue());
			if (sip != null) {
				classifier.setSourceIPAddress(sip);
			}
		}
		if (qosClassifier.getDstIp() != null) {
			InetAddress dip = getByName(qosClassifier.getDstIp().getValue());
			if (dip != null) {
				classifier.setDestinationIPAddress(dip);
			}
		}
		if (qosClassifier.getSrcPort() != null) {
			classifier.setSourcePort(qosClassifier.getSrcPort().getValue().shortValue());
		}
		if (qosClassifier.getDstPort() != null) {
			classifier.setDestinationPort(qosClassifier.getDstPort().getValue().shortValue());
		}
		if (qosClassifier.getTosByte() != null) {
			classifier.setDSCPTOS(qosClassifier.getTosByte().getValue().byteValue());
			if (qosClassifier.getTosMask() != null) {
				classifier.setDSCPTOSMask(qosClassifier.getTosMask().getValue().byteValue());
			} else {
				// set default TOS mask
				classifier.setDSCPTOSMask((byte)0xff);
			}
		}
		// push the classifier to the gate request
		gateReq.setClassifier(classifier);
	}

	public void build(ExtClassifier qosExtClassifier) {
		// Extended classifier
		IExtendedClassifier extClassifier = new org.pcmm.gates.impl.ExtendedClassifier();
		extClassifier.setPriority((byte) 64);
		extClassifier.setActivationState((byte) 0x01);
		// Protocol -- zero is match any
		if (qosExtClassifier.getProtocol() != null){
			extClassifier.setProtocol(qosExtClassifier.getProtocol().getValue().shortValue());
		} else {
			extClassifier.setProtocol((short)0);
		}
		// Source IP address & mask
		if (qosExtClassifier.getSrcIp() != null) {
			InetAddress sip = getByName(qosExtClassifier.getSrcIp().getValue());
			if (sip != null) {
				extClassifier.setSourceIPAddress(sip);
				if (qosExtClassifier.getSrcIpMask() != null) {
					InetAddress sipMask = getByName(qosExtClassifier.getSrcIpMask().getValue());
					extClassifier.setIPSourceMask(sipMask);
				} else {
					// default mask is /32
					extClassifier.setIPSourceMask(getByName("255.255.255.255"));
				}
			}
		}
		// Destination IP address & mask
		if (qosExtClassifier.getDstIp() != null) {
			InetAddress dip = getByName(qosExtClassifier.getDstIp().getValue());
			if (dip != null) {
				extClassifier.setDestinationIPAddress(dip);
				if (qosExtClassifier.getDstIpMask() != null) {
					InetAddress dipMask = getByName(qosExtClassifier.getDstIpMask().getValue());
					extClassifier.setIPDestinationMask(dipMask);
				} else {
					// default mask is /32
					extClassifier.setIPDestinationMask(getByName("255.255.255.255"));
				}
			}
		}
		// default source port range must be set to match any even if qosExtClassifier has no range
		// match any port range is 0-65535, NOT 0-0
		short startPort = (short)0;
		short endPort = (short)65535;
		if (qosExtClassifier.getSrcPortStart() != null) {
			startPort = qosExtClassifier.getSrcPortStart().getValue().shortValue();
			endPort = startPort;
			if (qosExtClassifier.getSrcPortEnd() != null) {
				endPort = qosExtClassifier.getSrcPortEnd().getValue().shortValue();
			}
			if (startPort > endPort) {
				logger.warn("Start port %d > End port %d in ext-classifier source port range -- forcing to same", startPort, endPort);
				endPort = startPort;
			}
		}
		extClassifier.setSourcePortStart(startPort);
		extClassifier.setSourcePortEnd(endPort);
		// default destination port range must be set to match any even if qosExtClassifier has no range
		// match any port range is 0-65535, NOT 0-0
		startPort = (short)0;
		endPort = (short)65535;
		if (qosExtClassifier.getDstPortStart() != null) {
			startPort = qosExtClassifier.getDstPortStart().getValue().shortValue();
			endPort = startPort;
			if (qosExtClassifier.getDstPortEnd() != null) {
				endPort = qosExtClassifier.getDstPortEnd().getValue().shortValue();
			}
			if (startPort > endPort) {
				logger.warn("Start port %d > End port %d in ext-classifier destination port range -- forcing to same", startPort, endPort);
				endPort = startPort;
			}
		}
		extClassifier.setDestinationPortStart(startPort);
		extClassifier.setDestinationPortEnd(endPort);
		// DSCP/TOP byte
		if (qosExtClassifier.getTosByte() != null) {
			// OR in the DSCP/TOS enable bit 0x01
			extClassifier.setDSCPTOS((byte) (qosExtClassifier.getTosByte().getValue().byteValue() | 0x01));
			if (qosExtClassifier.getTosMask() != null) {
				extClassifier.setDSCPTOSMask(qosExtClassifier.getTosMask().getValue().byteValue());
			} else {
				// set default TOS mask
				extClassifier.setDSCPTOSMask((byte)0xff);
			}
		}
		// push the extended classifier to the gate request
		gateReq.setClassifier(extClassifier);
	}

	public void build(Ipv6Classifier qosIpv6Classifier) {
		// IPv6 classifier
		IIPv6Classifier ipv6Classifier = new org.pcmm.gates.impl.IPv6Classifier();
		ipv6Classifier.setPriority((byte) 64);
		ipv6Classifier.setActivationState((byte) 0x01);
		// Flow Label
		if (qosIpv6Classifier.getFlowLabel() != null){
			ipv6Classifier.setFlowLabel(qosIpv6Classifier.getFlowLabel());
			ipv6Classifier.setFlowLabelEnableFlag((byte)0x01);
		}
		// Next Header
		if (qosIpv6Classifier.getNextHdr() != null){
			ipv6Classifier.setNextHdr(qosIpv6Classifier.getNextHdr().getValue().shortValue());
		} else {
			// default: match any nextHdr is 256 because nextHdr 0 is Hop-by-Hop option
			ipv6Classifier.setNextHdr((short)256);
		}
		// Source IPv6 address & prefix len
		byte prefLen;
		if (qosIpv6Classifier.getSrcIp6() != null) {
			String[] parts = qosIpv6Classifier.getSrcIp6().getValue().split("/");
			String Ipv6AddressStr = parts[0];
			InetAddress sip6 = getByName(Ipv6AddressStr);
			if (sip6 != null) {
				ipv6Classifier.setSourceIPAddress(sip6);
			}
			prefLen = (byte)128;
			if (parts.length > 1) {
				prefLen = (byte)Integer.parseInt(parts[1]);
			}
			ipv6Classifier.setSourcePrefixLen(prefLen);
		}
		// Destination IPv6 address & prefix len
		if (qosIpv6Classifier.getDstIp6() != null) {
			String[] parts = qosIpv6Classifier.getDstIp6().getValue().split("/");
			String Ipv6AddressStr = parts[0];
			InetAddress dip6 = getByName(Ipv6AddressStr);
			if (dip6 != null) {
				ipv6Classifier.setDestinationIPAddress(dip6);
			}
			prefLen = (byte)128;
			if (parts.length > 1) {
				prefLen = (byte)Integer.parseInt(parts[1]);
			}
			ipv6Classifier.setDestinationPrefixLen(prefLen);
		}
		// default source port range must be set to match any -- even if qosExtClassifier has no range value
		// match any port range is 0-65535, NOT 0-0
		short startPort = (short)0;
		short endPort = (short)65535;
		if (qosIpv6Classifier.getSrcPortStart() != null) {
			startPort = qosIpv6Classifier.getSrcPortStart().getValue().shortValue();
			endPort = startPort;
			if (qosIpv6Classifier.getSrcPortEnd() != null) {
				endPort = qosIpv6Classifier.getSrcPortEnd().getValue().shortValue();
			}
			if (startPort > endPort) {
				logger.warn("Start port %d > End port %d in ipv6-classifier source port range -- forcing to same", startPort, endPort);
				endPort = startPort;
			}
		}
		ipv6Classifier.setSourcePortStart(startPort);
		ipv6Classifier.setSourcePortEnd(endPort);
		// default destination port range must be set to match any -- even if qosExtClassifier has no range value
		// match any port range is 0-65535, NOT 0-0
		startPort = (short)0;
		endPort = (short)65535;
		if (qosIpv6Classifier.getDstPortStart() != null) {
			startPort = qosIpv6Classifier.getDstPortStart().getValue().shortValue();
			endPort = startPort;
			if (qosIpv6Classifier.getDstPortEnd() != null) {
				endPort = qosIpv6Classifier.getDstPortEnd().getValue().shortValue();
			}
			if (startPort > endPort) {
				logger.warn("Start port %d > End port %d in ipv6-classifier destination port range -- forcing to same", startPort, endPort);
				endPort = startPort;
			}
		}
		ipv6Classifier.setDestinationPortStart(startPort);
		ipv6Classifier.setDestinationPortEnd(endPort);
		// TC low, high, mask
		if (qosIpv6Classifier.getTcLow() != null) {
			ipv6Classifier.setTcLow(qosIpv6Classifier.getTcLow().getValue().byteValue());
			if (qosIpv6Classifier.getTcHigh() != null) {
				ipv6Classifier.setTcHigh(qosIpv6Classifier.getTcHigh().getValue().byteValue());
			}
			if (qosIpv6Classifier.getTcMask() != null) {
				ipv6Classifier.setTcMask(qosIpv6Classifier.getTcMask().getValue().byteValue());
			} else {
				// set default TOS mask
				ipv6Classifier.setTcMask((byte)0xff);
			}
		} else {
			// mask 0x00 is match any
			ipv6Classifier.setTcMask((byte)0x00);
		}
		// push the IPv6 classifier to the gate request
		gateReq.setClassifier(ipv6Classifier);
	}
}

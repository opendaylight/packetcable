/**
 * Validate all instance data received from the config datastore via the onDataChange() notification.
 *
 * N.B. that yang typedefs are not validated when a PUT operation places them into the config datastore.
 * This means that they can arrive at onDataChange() with invalid values.
 *
 * In particular integer range values and string patterns (such as IP prefix/len) are not checked
 * and accessing these values via any object.getValue() method call will cause an exception (as yang
 * finally gets around to actually enforcing the typedef).
 */
package org.opendaylight.controller.packetcable.provider;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.*;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.TpProtocol;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.CcapsBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.AmIdBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.ConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.classifier.ClassifierBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.ext.classifier.ExtClassifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.ext.classifier.ExtClassifierBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gate.spec.GateSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.GatesBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.ipv6.classifier.Ipv6Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.ipv6.classifier.Ipv6ClassifierBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.traffic.profile.TrafficProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.traffic.profile.TrafficProfileBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValidateInstanceData {

	private final static Logger logger = LoggerFactory.getLogger(ValidateInstanceData.class);

	// Final members
	private final DataBroker dataBroker;
	private final ExecutorService executor;

	// Gate Identities
	private final Map<InstanceIdentifier<Gates>, Gates> gateIidMap;

	// CCAP Identity
	private transient Ccaps ccap;
	private transient InstanceIdentifier<Ccaps> ccapIID;

	public ValidateInstanceData(final DataBroker dataBroker, final Map<InstanceIdentifier<?>, DataObject> thisData) {
		executor = Executors.newCachedThreadPool();
		this.dataBroker = dataBroker;
		getCcap(thisData);
		// TODO FIXME - this value is always null???
		if (ccap == null) {
			getGates(thisData);
		}
		gateIidMap = new ConcurrentHashMap<>();
	}
	public boolean isResponseEcho() {
		// see if there is a response object in the updated data
		// if so this is an echo of the response message insertion so our caller can exit right away
		if (ccap != null && ccap.getResponse() != null) {
			return true;
		} else if (! gateIidMap.isEmpty() && gateIidMap.values().iterator().next().getResponse() != null) {
			return true;
		}
		return false;
	}
	public boolean validateYang() {
		final String badText = "400 Bad Request - Invalid Element Values in json object - ";
		if (isResponseEcho()) {
			// don't validiate the echo again
			return true;
		}
		if (ccap != null) {
			Response response = new Response(dataBroker, ccapIID, ccap, badText);
			if (! validateCcap(ccap, response)) {
				logger.error("Validate CCAP {} failed - {}", ccap.getCcapId(), response.getMessage());
				executor.execute(response);
				return false;
			}
		} else if (! gateIidMap.isEmpty()) {
			for (Map.Entry<InstanceIdentifier<Gates>, Gates> entry : gateIidMap.entrySet()) {
				InstanceIdentifier<Gates> gateIID = entry.getKey();
				Gates gate = entry.getValue();
				Response response = new Response(dataBroker, gateIID, gate, badText);
				if (! validateGate(gate, response)) {
					logger.error("Validate Gate {} failed - {}", gate.getGateId(), response.getMessage());
					executor.execute(response);
					return false;
				}
			}
		}
		return true;
	}

	private void getCcap(final Map<InstanceIdentifier<?>, DataObject> thisData) {
		for (final Map.Entry<InstanceIdentifier<?>, DataObject> entry : thisData.entrySet()) {
			if (entry.getValue() instanceof Ccaps) {
	            ccap = (Ccaps)entry.getValue();
				// TODO FIXME - ClassCastException waiting to occur here!!!
	            ccapIID = (InstanceIdentifier<Ccaps>) entry.getKey();
	        }
	    }
	}

	private void getGates(final Map<InstanceIdentifier<?>, DataObject> thisData) {
		for (final Map.Entry<InstanceIdentifier<?>, DataObject> entry : thisData.entrySet()) {
			if (entry.getValue() instanceof Gates) {
				final Gates gate = (Gates)entry.getValue();
				// TODO FIXME - ClassCastException waiting to occur here!!!
				final InstanceIdentifier<Gates> gateIID = (InstanceIdentifier<Gates>)entry.getKey();
				gateIidMap.put(gateIID, gate);
			}
	    }
	}
	private String validateMethod(final Class<?> thisClass, final Object thisObj, final String methodName) {
		try {
			final Method method = thisClass.getMethod(methodName);
			method.invoke(thisObj);
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		} catch (Exception e) {
			return " ";
//			error = String.format("%s.%s(): Method failed: %s ", thisClass.getSimpleName(), methodName, e.getMessage());
		}
		return null;
	}

	private boolean validateGateSpec(final Gates gate, final GatesBuilder gateBuilder, final Response response) {
		// gate-spec
		String message = "";
		String error;
		boolean valid = true;
		GateSpec gateSpec = gate.getGateSpec();
		if (gateSpec != null) {
			final ServiceFlowDirection dir;
			error = validateMethod(GateSpec.class, gateSpec, "getDirection");
			if (error == null) {
				dir = gateSpec.getDirection();
				if (dir != null) {
					if (gate.getTrafficProfile().getServiceClassName() != null) {
						message += " gate-spec.direction not allowed for traffic-profile.SCN;";
						valid = false;
					}
				}
			} else {
				message += " gate-spec.direction invalid: must be 'us' or 'ds' -" + error;
				dir = null;
				valid = false;
			}
			final TosByte tosByte;
			error = validateMethod(GateSpec.class, gateSpec, "getDscpTosOverwrite");
			if (error == null) {
				tosByte = gateSpec.getDscpTosOverwrite();
			} else {
				message += " gate-spec.dscp-tos-overwrite invalid: " + error;
				tosByte = null;
				valid = false;
			}
			final TosByte tosMask;
			error = validateMethod(GateSpec.class, gateSpec, "getDscpTosMask");
			if (error == null) {
				tosMask = gateSpec.getDscpTosMask();
				if (tosByte != null && tosMask == null) {
					message += " gate-spec.dscp-tos-mask missing;";
					valid = false;
				}
			} else {
				message += " gate-spec.dscp-tos-mask invalid: " + error;
				tosMask = null;
				valid = false;
			}
			if (! valid) {
				// rebuild the gateSpec with nulls replacing bad values
				final GateSpecBuilder gateSpecBuilder = new GateSpecBuilder();
				gateSpecBuilder.setDirection(dir);
				gateSpecBuilder.setDscpTosOverwrite(tosByte);
				gateSpecBuilder.setDscpTosMask(tosMask);
				gateSpec = gateSpecBuilder.build();
				// update the gate
				gateBuilder.setGateSpec(gateSpec);
			}
		}
		if (! valid) {
			response.addMessage(message);
		}
		return valid;
	}

	private boolean validateTrafficProfile(final Gates gate, final GatesBuilder gateBuilder, final Response response) {
		// traffic-profile
		String message = "";
		boolean valid = true;
		TrafficProfile profile = gate.getTrafficProfile();
		if (profile == null) {
			message += " traffic-profile is required;";
			valid = false;
		} else {
			final ServiceClassName scn;
			final String error = validateMethod(TrafficProfile.class, profile, "getServiceClassName");
			if (error == null) {
				scn = profile.getServiceClassName();
				if (scn == null) {
					message += " traffic-profile.service-class-name missing;";
					valid = false;
				}
			} else {
				message += " traffic-profile.service-class-name invalid: must be 2-16 characters " + error;
				scn = null;
				valid = false;
			}
			if (! valid) {
				final TrafficProfileBuilder profileBuilder = new TrafficProfileBuilder();
				// TODO FIXME - scn is always null???
				profileBuilder.setServiceClassName(scn);
				profile = profileBuilder.build();
				// update the gate
				gateBuilder.setTrafficProfile(profile);
			}
		}
		if (! valid) {
			response.addMessage(message);
		}
		return valid;
	}

	// TODO FIXME - Break this method apart
	private boolean validateClassifier(final Gates gate, final GatesBuilder gateBuilder, final Response response) {
		// validate classifier
		String message = "";
		boolean valid = true;
		int count = 0;
		Classifier classifier = gate.getClassifier();
		// SIP
		final Ipv4Address sip;
		String error = validateMethod(Classifier.class, classifier, "getSrcIp");
		if (error == null) {
			sip = classifier.getSrcIp();
			count++;
		} else {
			message += " classifier.srcIp invalid: - " + error;
			sip = null;
			valid = false;
		}
		// DIP
		final Ipv4Address dip;
		error = validateMethod(Classifier.class, classifier, "getDstIp");
		if (error == null) {
			dip = classifier.getDstIp();
			count++;
		} else {
			message += " classifier.dstIp invalid: - " + error;
			dip = null;
			valid = false;
		}
		// Protocol
		final TpProtocol proto;
		error = validateMethod(Classifier.class, classifier, "getProtocol");
		if (error == null) {
			proto = classifier.getProtocol();
			count++;
		} else {
			message += " classifier.protocol invalid: - " + error;
			proto = null;
			valid = false;
		}
		// Source Port
		final PortNumber sport;
		error = validateMethod(Classifier.class, classifier, "getSrcPort");
		if (error == null) {
			sport = classifier.getSrcPort();
			count++;
		} else {
			message += " classifier.srcPort invalid: - " + error;
			sport = null;
			valid = false;
		}
		// Destination Port
		final PortNumber dport;
		error = validateMethod(Classifier.class, classifier, "getDstPort");
		if (error == null) {
			dport = classifier.getDstPort();
			count++;
		} else {
			message += " classifier.dstPort invalid: - " + error;
			dport = null;
			valid = false;
		}
		// TOS
		final TosByte tosByte;
		error = validateMethod(Classifier.class, classifier, "getTosByte");
		if (error == null) {
			tosByte = classifier.getTosByte();
			count++;
		} else {
			message += " classifier.tosByte invalid: " + error;
			tosByte = null;
			valid = false;
		}
		final TosByte tosMask;
		error = validateMethod(Classifier.class, classifier, "getTosMask");
		if (error == null) {
			tosMask = classifier.getTosMask();
			if (tosByte != null && tosMask == null) {
				message += " classifier.tosMask missing;";
				valid = false;
			}
		} else {
			message += " classifier.tosMask invalid: " + error;
			tosMask = null;
			valid = false;
		}
		if (count == 0) {
			message += " classifer must have at least one match field";
			valid = false;
		}
		if (! valid) {
			final ClassifierBuilder cBuilder = new ClassifierBuilder();
			cBuilder.setSrcIp(sip);
			cBuilder.setDstIp(dip);
			cBuilder.setProtocol(proto);
			cBuilder.setSrcPort(sport);
			cBuilder.setDstPort(dport);
			cBuilder.setTosByte(tosByte);
			cBuilder.setTosMask(tosMask);
			classifier = cBuilder.build();
			gateBuilder.setClassifier(classifier);
			response.addMessage(message);
		}
		return valid;
	}

	// TODO FIXME - breakup this method
	private boolean validateExtClassifier(final Gates gate, final GatesBuilder gateBuilder, final Response response) {
		// validate ext-classifier
		String message = "";
		String error;
		boolean valid = true;
		int count = 0;
		ExtClassifier extClassifier = gate.getExtClassifier();
		// SIP & mask
		final Ipv4Address sip;
		error = validateMethod(ExtClassifier.class, extClassifier, "getSrcIp");
		if (error == null) {
			sip = extClassifier.getSrcIp();
			count++;
		} else {
			message += " ext-classifier.srcIp invalid: - " + error;
			sip = null;
			valid = false;
		}
		final Ipv4Address sipMask;
		error = validateMethod(ExtClassifier.class, extClassifier, "getSrcIpMask");
		if (error == null) {
			sipMask = extClassifier.getSrcIpMask();
			count++;
		} else {
			message += " ext-classifier.srcIpMask invalid: - " + error;
			sipMask = null;
			valid = false;
		}
		if (sip != null && sipMask == null) {
			message += " ext-classifier.srcIpMask missing";
			valid = false;
		}
		// DIP & mask
		final Ipv4Address dip;
		error = validateMethod(ExtClassifier.class, extClassifier, "getDstIp");
		if (error == null) {
			dip = extClassifier.getDstIp();
			count++;
		} else {
			message += " ext-classifier.dstIp invalid: - " + error;
			dip = null;
			valid = false;
		}
		final Ipv4Address dipMask;
		error = validateMethod(ExtClassifier.class, extClassifier, "getDstIpMask");
		if (error == null) {
			dipMask = extClassifier.getDstIpMask();
			count++;
		} else {
			message += " ext-classifier.srcIpMask invalid: - " + error;
			dipMask = null;
			valid = false;
		}
		if (dip != null && dipMask == null) {
			message += " ext-classifier.dstIpMask missing;";
			valid = false;
		}
		// Protocol
		final TpProtocol proto;
		error = validateMethod(ExtClassifier.class, extClassifier, "getProtocol");
		if (error == null) {
			proto = extClassifier.getProtocol();
			count++;
		} else {
			message += " ext-classifier.protocol invalid: - " + error;
			proto = null;
			valid = false;
		}
		// Source port range
		final PortNumber sportStart;
		error = validateMethod(ExtClassifier.class, extClassifier, "getSrcPortStart");
		if (error == null) {
			sportStart = extClassifier.getSrcPortStart();
			count++;
		} else {
			message += " ext-classifier.srcPortStart invalid: - " + error;
			sportStart = null;
			valid = false;
		}
		final PortNumber sportEnd;
		error = validateMethod(ExtClassifier.class, extClassifier, "getSrcPortEnd");
		if (error == null) {
			sportEnd = extClassifier.getSrcPortEnd();
			count++;
		} else {
			message += " ext-classifier.srcPortEnd invalid: - " + error;
			sportEnd = null;
			valid = false;
		}
		if (sportStart != null && sportEnd != null) {
			if (sportStart.getValue() > sportEnd.getValue()) {
				message += " ext-classifier.srcPortStart greater than srcPortEnd";
				valid = false;
			}
		}
		// Destination port range
		final PortNumber dportStart;
		error = validateMethod(ExtClassifier.class, extClassifier, "getDstPortStart");
		if (error == null) {
			dportStart = extClassifier.getDstPortStart();
			count++;
		} else {
			message += " ext-classifier.dstPortStart invalid: - " + error;
			dportStart = null;
			valid = false;
		}
		final PortNumber dportEnd;
		error = validateMethod(ExtClassifier.class, extClassifier, "getDstPortEnd");
		if (error == null) {
			dportEnd = extClassifier.getDstPortEnd();
			count++;
		} else {
			message += " ext-classifier.dstPortEnd invalid: - " + error;
			dportEnd = null;
			valid = false;
		}
		if (dportStart != null && dportEnd != null) {
			if (dportStart.getValue() > dportEnd.getValue()) {
				message += " ext-classifier.dstPortStart greater than dstPortEnd";
				valid = false;
			}
		}
		// TOS byte
		final TosByte tosByte;
		error = validateMethod(ExtClassifier.class, extClassifier, "getTosByte");
		if (error == null) {
			tosByte = extClassifier.getTosByte();
			count++;
		} else {
			message += " ext-classifier.tosByte invalid: " + error;
			tosByte = null;
			valid = false;
		}
		final TosByte tosMask;
		error = validateMethod(ExtClassifier.class, extClassifier, "getTosMask");
		if (error == null) {
			tosMask = extClassifier.getTosMask();
			if (tosByte != null && tosMask == null) {
				message += " ext-classifier.tosMask missing;";
				valid = false;
			}
		} else {
			message += " ext-classifier.tosMask invalid: " + error;
			tosMask = null;
			valid = false;
		}
		if (count == 0) {
			message += " ext-classifer must have at least one match field";
			valid = false;
		}
		if (! valid) {
			final ExtClassifierBuilder cBuilder = new ExtClassifierBuilder();
			cBuilder.setSrcIp(sip);
			cBuilder.setSrcIpMask(sipMask);
			cBuilder.setDstIp(dip);
			cBuilder.setDstIpMask(dipMask);
			cBuilder.setProtocol(proto);
			cBuilder.setSrcPortStart(sportStart);
			cBuilder.setSrcPortEnd(sportEnd);
			cBuilder.setDstPortStart(dportStart);
			cBuilder.setDstPortEnd(dportEnd);
			cBuilder.setTosByte(tosByte);
			cBuilder.setTosMask(tosMask);
			extClassifier = cBuilder.build();
			gateBuilder.setExtClassifier(extClassifier);
			response.addMessage(message);
		}
		return valid;
	}

	// TODO FIXME - break apart this method.
	private boolean validateIpv6Classifier(final Gates gate, final GatesBuilder gateBuilder, final Response response) {
		// validate ipv6-classifier
		String message = "";
		String error;
		boolean valid = true;
		int count = 0;
		Ipv6Classifier ipv6Classifier = gate.getIpv6Classifier();
		// Source IPv6 prefix
		final Ipv6Prefix sip6;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getSrcIp6");
		if (error == null) {
			sip6 = ipv6Classifier.getSrcIp6();
			count++;
		} else {
			message += " ipv6-classifier.srcIp invalid: - " + error;
			sip6 = null;
			valid = false;
		}
		// Destination IPv6 prefix
		final Ipv6Prefix dip6;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getDstIp6");
		if (error == null) {
			dip6 = ipv6Classifier.getDstIp6();
			count++;
		} else {
			message += " ipv6-classifier.dstIp invalid: - " + error;
			dip6 = null;
			valid = false;
		}
		// Flow label
		Long flowLabel;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getFlowLabel");
		if (error == null) {
			flowLabel = ipv6Classifier.getFlowLabel();
			if (flowLabel > 1048575) {
				message += " ipv6-classifier.flowLabel invalid: - must be 0..1048575";
				flowLabel = null;
				valid = false;
			} else {
				count++;
			}
		} else {
			message += " ipv6-classifier.flowLabel invalid: - " + error;
			flowLabel = null;
			valid = false;
		}
		// Next Hdr
		final TpProtocol nxtHdr;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getNextHdr");
		if (error == null) {
			nxtHdr = ipv6Classifier.getNextHdr();
			count++;
		} else {
			message += " ipv6-classifier.nextHdr invalid: - " + error;
			nxtHdr = null;
			valid = false;
		}
		// Source port range
		final PortNumber sportStart;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getSrcPortStart");
		if (error == null) {
			sportStart = ipv6Classifier.getSrcPortStart();
			count++;
		} else {
			message += " ipv6-classifier.srcPortStart invalid: - " + error;
			sportStart = null;
			valid = false;
		}
		final PortNumber sportEnd;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getSrcPortEnd");
		if (error == null) {
			sportEnd = ipv6Classifier.getSrcPortEnd();
			count++;
		} else {
			message += " ipv6-classifier.srcPortEnd invalid: - " + error;
			sportEnd = null;
			valid = false;
		}
		if (sportStart != null && sportEnd != null) {
			if (sportStart.getValue() > sportEnd.getValue()) {
				message += " ipv6-classifier.srcPortStart greater than srcPortEnd";
				valid = false;
			}
		}
		// Destination port range
		final PortNumber dportStart;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getDstPortStart");
		if (error == null) {
			dportStart = ipv6Classifier.getDstPortStart();
			count++;
		} else {
			message += " ipv6-classifier.dstPortStart invalid: - " + error;
			dportStart = null;
			valid = false;
		}
		final PortNumber dportEnd;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getDstPortEnd");
		if (error == null) {
			dportEnd = ipv6Classifier.getDstPortEnd();
			count++;
		} else {
			message += " ipv6-classifier.dstPortEnd invalid: - " + error;
			dportEnd = null;
			valid = false;
		}
		if (dportStart != null && dportEnd != null) {
			if (dportStart.getValue() > dportEnd.getValue()) {
				message += " ipv6-classifier.dstPortStart greater than dstPortEnd";
				valid = false;
			}
		}
		// TC byte
		final TosByte tcLow;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getTcLow");
		if (error == null) {
			tcLow = ipv6Classifier.getTcLow();
			count++;
		} else {
			message += " ipv6-classifier.tc-low invalid: " + error;
			tcLow = null;
			valid = false;
		}
		final TosByte tcHigh;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getTcHigh");
		if (error == null) {
			tcHigh = ipv6Classifier.getTcHigh();
			count++;
		} else {
			message += " ipv6-classifier.tc-high invalid: " + error;
			tcHigh = null;
			valid = false;
		}
		if (tcLow != null && tcHigh != null) {
			if (tcLow.getValue() > tcHigh.getValue()) {
				message += " ipv6-classifier.tc-low is greater than tc-high";
				valid = false;
			}
		}
		final TosByte tcMask;
		error = validateMethod(Ipv6Classifier.class, ipv6Classifier, "getTcMask");
		if (error == null) {
			tcMask = ipv6Classifier.getTcMask();
		} else {
			message += " ipv6-classifier.tc-mask invalid: " + error;
			tcMask = null;
			valid = false;
		}
		if (tcLow != null && tcHigh != null && tcMask == null) {
			message += " ipv6-classifier.tc-mask missing;";
			valid = false;
		}
		if (count == 0) {
			message += " ipv6-classifer must have at least one match field";
			valid = false;
		}
		// rebuild ?
		if (! valid) {
			final Ipv6ClassifierBuilder cBuilder = new Ipv6ClassifierBuilder();
			cBuilder.setSrcIp6(sip6);
			cBuilder.setDstIp6(dip6);
			cBuilder.setFlowLabel(flowLabel);
			cBuilder.setNextHdr(nxtHdr);
			cBuilder.setSrcPortStart(sportStart);
			cBuilder.setSrcPortEnd(sportEnd);
			cBuilder.setDstPortStart(dportStart);
			cBuilder.setDstPortEnd(dportEnd);
			cBuilder.setTcLow(tcLow);
			cBuilder.setTcHigh(tcHigh);
			cBuilder.setTcMask(tcMask);
			ipv6Classifier = cBuilder.build();
			gateBuilder.setIpv6Classifier(ipv6Classifier);
			response.addMessage(message);
		}
		return valid;
	}

	// TODO FIXME - Do we really want the gate parameter object to be muted by this method?
	private boolean validateGate(Gates gate, final Response response) {
		// validate gate elements and null out invalid elements as we go
		final GatesBuilder gateBuilder = new GatesBuilder();
		String message = "";
		boolean rebuild = false;
		// gate-spec
		if (! validateGateSpec(gate, gateBuilder, response)) {
			rebuild = true;
		}
		// traffic-profile
		if (! validateTrafficProfile(gate, gateBuilder, response)) {
			rebuild = true;
		}
		// classifiers (one of legacy classifier, ext-classifier, or ipv6 classifier
		final Classifier classifier = gate.getClassifier();
		final ExtClassifier extClassifier = gate.getExtClassifier();
		final Ipv6Classifier ipv6Classifier = gate.getIpv6Classifier();
		int count = 0;
		if (classifier != null) { count++; }
		if (extClassifier != null) { count++; }
		if (ipv6Classifier != null) { count++; }
		if (count < 1){
			response.addMessage(" Missing classifer: must have only 1 of classifier, ext-classifier, or ipv6-classifier");
			rebuild = true;
		} else if (count > 1) {
			response.addMessage(" Multiple classifiers: must have only 1 of classifier, ext-classifier, or ipv6-classifier");
			rebuild = true;
		} else if (count == 1) {
			if (classifier != null) {
				// validate classifier
				if (! validateClassifier(gate, gateBuilder, response)) {
					rebuild = true;
				}
			} else if (extClassifier != null) {
				//validate ext-classifier
				if (! validateExtClassifier(gate, gateBuilder, response)) {
					rebuild = true;
				}
			} else if (ipv6Classifier != null) {
				// TODO FIXME - ipv6Classifier is always null???
				// validate ipv6-classifier
				if (! validateIpv6Classifier(gate, gateBuilder, response)) {
					rebuild = true;
				}
			}
		}
		// rebuild the gate object with valid data and set the response
		if (rebuild) {
			gateBuilder.setGateId(gate.getGateId());
			gateBuilder.setKey(gate.getKey());
			// TODO FIXME - the input parameter "gate" is being muted here???
			gate = gateBuilder.build();
			response.setGateBase(gate);
			response.addMessage(message);
		}
		return (! rebuild);
	}

	private boolean validateAmId(final Ccaps ccap, final CcapsBuilder ccapBuilder, final Response response) {
		// amId
		String message = "";
		String error;
		boolean valid = true;
		AmId amId = ccap.getAmId();
		if (amId == null) {
			message += " amId is required;";
			valid = false;
		} else {
			final Integer amTag;
			error = validateMethod(AmId.class, amId, "getAmTag");
			if (error == null) {
				amTag = amId.getAmTag();
				if (amTag == null) {
					message += " amId.amTag missing;";
					valid = false;
				}
			} else {
				message += " amId.amTag invalid: " + error;
				amTag = null;
				valid = false;
			}
			final Integer amType;
			error = validateMethod(AmId.class, amId, "getAmType");
			if (error == null) {
				amType = amId.getAmType();
				if (amType == null) {
					message += " amId.amType missing;";
					valid = false;
				}
			} else {
				message += " amId.amType invalid: " + error;
				amType = null;
				valid = false;
			}
			if (! valid) {
				final AmIdBuilder amIdBuilder = new AmIdBuilder();
				amIdBuilder.setAmTag(amTag);
				amIdBuilder.setAmType(amType);
				amId = amIdBuilder.build();
				ccapBuilder.setAmId(amId);
			}
		}
		if (! valid) {
			response.addMessage(message);
		}
		return valid;
	}

	private boolean validateConnection(final Ccaps ccap, final CcapsBuilder ccapBuilder, final Response response) {
		// connection
		String message = "";
		String error;
		boolean valid = true;
		Connection conn = ccap.getConnection();
		if (conn == null) {
			message += " connection is required;";
			valid = false;
		} else {
			// IP address
			final IpAddress ipAddress;
			error = validateMethod(Connection.class, conn, "getIpAddress");
			if (error == null) {
				ipAddress = conn.getIpAddress();
				if (ipAddress == null) {
					message += " connection.ipAddress missing;";
					valid = false;
				}
			} else {
				message += " connection.ipAddress invalid: " + error;
				ipAddress = null;
				valid = false;
			}
			// Port number
			final PortNumber portNum;
			error = validateMethod(Connection.class, conn, "getPort");
			if (error == null) {
				portNum = conn.getPort();
			} else {
				message += " connection.port invalid: " + error;
				portNum = null;
				valid = false;
			}
			if (! valid) {
				final ConnectionBuilder connBuilder = new ConnectionBuilder();
				connBuilder.setIpAddress(ipAddress);
				connBuilder.setPort(portNum);
				conn = connBuilder.build();
				ccapBuilder.setConnection(conn);
			}
		}
		if (! valid) {
			response.addMessage(message);
		}
		return valid;
	}

	private boolean validateSubscriberSubnets(final Ccaps ccap, final CcapsBuilder ccapBuilder,
											  final Response response) {
		// subscriber-subnets
		String message = "";
		String error;
		boolean valid = true;
		List<IpPrefix> subnets = null;
		error = validateMethod(Ccaps.class, ccap, "getSubscriberSubnets");
		if (error == null) {
			subnets = ccap.getSubscriberSubnets();
			if (subnets == null) {
				message += " subscriber-subnets is required;";
				valid = false;
			}
		} else {
			message += " subscriber-subnets contains invalid IpPrefix - must be <ipaddress>/<prefixlen> format;" + error;
			valid = false;
		}
		if (! valid) {
			// TODO FIXME - subnets is always null???
			ccapBuilder.setSubscriberSubnets(subnets);
			response.addMessage(message);
		}
		return valid;
	}

	private boolean validateUpstreamScns(final Ccaps ccap, final CcapsBuilder ccapBuilder, final Response response) {
		// upstream-scns
		String message = "";
		String error;
		boolean valid = true;
		List<ServiceClassName> usScns = null;
		error = validateMethod(Ccaps.class, ccap, "getUpstreamScns");
		if (error == null) {
			usScns = ccap.getUpstreamScns();
			if (usScns == null) {
				message += " upstream-scns is required;";
				valid = false;
			}
		} else {
			message += " upstream-scns contains invalid SCN - must be 2-16 characters;" + error;
			valid = false;
		}
		if (! valid) {
			// TODO FIXME - usScns is always null???
			ccapBuilder.setUpstreamScns(usScns);
			response.addMessage(message);
		}
		return valid;
	}

	private boolean validateDownstreamScns(final Ccaps ccap, final CcapsBuilder ccapBuilder, final Response response) {
		// downstream-scns
		String message = "";
		boolean valid = true;
		List<ServiceClassName> dsScns = null;
		final String error = validateMethod(Ccaps.class, ccap, "getDownstreamScns");
		if (error == null) {
			dsScns = ccap.getDownstreamScns();
			if (dsScns == null) {
				message += " downstream-scns is required;";
				valid = false;
			}
		} else {
			message += " downstream-scns contains invalid SCN - must be 2-16 characters;" + error;
			valid = false;
		}
		if (! valid) {
			// TODO FIXME - dsScns is always null???
			ccapBuilder.setDownstreamScns(dsScns);
			response.addMessage(message);
		}
		return valid;
	}


	// TODO FIXME - Do we really want the ccap parameter object to be muted by this method?
	private boolean validateCcap(Ccaps ccap, final Response response) {
		// validate ccap and null out invalid elements as we go
		final CcapsBuilder ccapBuilder = new CcapsBuilder();
		String message = "";
		boolean rebuild = false;
		// amId
		if ( ! validateAmId(ccap, ccapBuilder, response))        {
			rebuild = true;
		}
		// connection
		if ( ! validateConnection(ccap, ccapBuilder, response))        {
			rebuild = true;
		}
		// subscriber-subnets
		if ( ! validateSubscriberSubnets(ccap, ccapBuilder, response))        {
			rebuild = true;
		}
		// upstream-scns
		if ( ! validateUpstreamScns(ccap, ccapBuilder, response))        {
			rebuild = true;
		}
		// downstream-scns
		if ( ! validateDownstreamScns(ccap, ccapBuilder, response))        {
			rebuild = true;
		}
		// rebuild the ccap object with valid data and set the response
		if (rebuild) {
			ccapBuilder.setCcapId(ccap.getCcapId());
			ccapBuilder.setKey(ccap.getKey());
			// TODO FIXME - the input parameter "ccap" is being muted here???
			ccap = ccapBuilder.build();
			response.setCcapBase(ccap);
			response.addMessage(message);
		}
		return (! rebuild);
	}
}


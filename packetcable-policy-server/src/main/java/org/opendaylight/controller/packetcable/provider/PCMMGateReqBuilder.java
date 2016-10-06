/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.classifier.attributes.classifiers.ClassifierContainer;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.classifier.attributes.classifiers.classifier.container.ClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.classifier.attributes.classifiers.classifier.container.classifier.choice.ExtClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.classifier.attributes.classifiers.classifier.container.classifier.choice.Ipv6ClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.classifier.attributes.classifiers.classifier.container.classifier.choice.QosClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.ext.classifier.ExtClassifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.ipv6.classifier.Ipv6Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.traffic.profile.TrafficProfile;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IClassifier.Protocol;
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.IExtendedClassifier.ActivationState;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.IIPv6Classifier.FlowLabel;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.gates.impl.AMID;
import org.pcmm.gates.impl.DOCSISServiceClassNameTrafficProfile;
import org.pcmm.gates.impl.GateID;
import org.pcmm.gates.impl.GateState;
import org.pcmm.gates.impl.GateTimeInfo;
import org.pcmm.gates.impl.GateUsageInfo;
import org.pcmm.gates.impl.PCMMError;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.SubscriberID;
import org.pcmm.gates.impl.TransactionID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build PCMM gate requests from API QoS Gate objects
 */
public class PCMMGateReqBuilder {

    private final Logger logger = LoggerFactory.getLogger(PCMMGateReqBuilder.class);

    private GateID gateID = null;
    private AMID amid = null;
    private SubscriberID subscriberID = null;
    private TransactionID transactionID = null;
    private org.pcmm.gates.impl.GateSpec gateSpec = null;
    private ITrafficProfile trafficProfile = null;
    private final List<IClassifier> classifiers = Lists.newArrayListWithExpectedSize(4);
    private PCMMError error = null;
    private GateState gateState = null;
    private GateTimeInfo gateTimeInfo = null;
    private GateUsageInfo gateUsageInfo = null;

    public PCMMGateReq build() {
        return new PCMMGateReq(amid, subscriberID, transactionID, gateSpec, trafficProfile, classifiers,
                gateID, error, gateState, gateTimeInfo, gateUsageInfo);
    }

    public void setAmId(final AmId qosAmId) {
        amid = new AMID(qosAmId.getAmType().shortValue(), qosAmId.getAmTag().shortValue());
    }

    public void setSubscriberId(final InetAddress qosSubId) {
        subscriberID = new SubscriberID(qosSubId);
    }

    public void setGateSpec(final GateSpec qosGateSpec, final ServiceFlowDirection scnDirection) {

        final ServiceFlowDirection qosDir;
        if (scnDirection != null) {
            qosDir = scnDirection;
        } else {
            if (qosGateSpec.getDirection() != null) {
                qosDir = qosGateSpec.getDirection();
            } else {
                // TODO - determine if this is a valid default value
                qosDir = ServiceFlowDirection.Ds;
            }
        }

        final Direction gateDir;
        if (qosDir == ServiceFlowDirection.Ds) {
            gateDir = Direction.DOWNSTREAM;
        } else {
            gateDir = Direction.UPSTREAM;
        }

        // DSCP/TOS Overwrite
        final byte dscptos;
        final byte gateTosMask;

        final TosByte tosOverwrite = qosGateSpec.getDscpTosOverwrite();
        if (tosOverwrite != null) {
            dscptos = 1;
            TosByte tosMask = qosGateSpec.getDscpTosMask();
            if (tosMask != null) {
                gateTosMask = tosMask.getValue().byteValue();
            } else {
                gateTosMask = (byte) 0xff;
            }
        } else {
            // TODO - These values appear to be required
            dscptos = 0;
            gateTosMask = 0;
        }
        gateSpec = new org.pcmm.gates.impl.GateSpec(gateDir, dscptos, gateTosMask);
    }

    public void setTrafficProfile(final TrafficProfile qosTrafficProfile) {
        if (qosTrafficProfile.getServiceClassName() != null) {
            trafficProfile =
                    new DOCSISServiceClassNameTrafficProfile(qosTrafficProfile.getServiceClassName().getValue());
        }
    }

    private InetAddress getByName(final String ipAddressStr) {
        try {
            return InetAddress.getByName(ipAddressStr);
        } catch (UnknownHostException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public void setClassifiers(final List<ClassifierContainer> classifiers) {
        checkNotNull(classifiers);

        for (ClassifierContainer container : classifiers) {
            final ClassifierChoice choice = container.getClassifierChoice();
            final Short index = container.getClassifierId();

            if (choice instanceof QosClassifierChoice) {
                addClassifier(index, ((QosClassifierChoice) choice).getClassifier());
            }
            else if (choice instanceof ExtClassifierChoice) {
                addExtClassifier(index, ((ExtClassifierChoice) choice).getExtClassifier());
            }
            else if (choice instanceof Ipv6ClassifierChoice) {
                addIpv6Classifier(index, ((Ipv6ClassifierChoice) choice).getIpv6Classifier());
            }
            else {
                throw new IllegalStateException("Unknown ClassifierChoice: " + choice);
            }
        }
    }

    private void addClassifier(final Short index,final Classifier qosClassifier) {
        // TODO - try and make these variables immutable
        Protocol protocol = null;
        byte tosOverwrite = 0;
        byte tosMask = (byte)0x0;
        short srcPort = (short) 0;
        short dstPort = (short) 0;
        //byte priority = (byte) 64;
        byte priority = (byte) (64+index);

        // Legacy classifier

        // Protocol -- zero is match any
        if (qosClassifier.getProtocol() != null) {
            protocol = Protocol.valueOf(qosClassifier.getProtocol().getValue().shortValue());
        } else {
            protocol = Protocol.NONE;
        }

        // IP Addresss and mask wildcards - addr byte 0 for no match (or match anything)

        Inet4Address srcAddress = (Inet4Address) getByName("0.0.0.0");

        if (qosClassifier.getSrcIp() != null) {
            srcAddress = (Inet4Address) getByName(qosClassifier.getSrcIp().getValue());
        }

        Inet4Address dstAddress = (Inet4Address) getByName("0.0.0.0");

        if (qosClassifier.getDstIp() != null) {
            dstAddress = (Inet4Address) getByName(qosClassifier.getDstIp().getValue());
        }


        if (qosClassifier.getSrcPort() != null) {
            srcPort = qosClassifier.getSrcPort().getValue().shortValue();
        }
        if (qosClassifier.getDstPort() != null) {
            dstPort = qosClassifier.getDstPort().getValue().shortValue();
        }
        if (qosClassifier.getTosByte() != null) {
            tosOverwrite = qosClassifier.getTosByte().getValue().byteValue();
            if (qosClassifier.getTosMask() != null) {
                tosMask = qosClassifier.getTosMask().getValue().byteValue();
            } else {
                // set default TOS mask
                tosMask = (byte) 0xff;
            }
        }
        // push the classifier to the gate request
        classifiers.add(new org.pcmm.gates.impl.Classifier(protocol, tosOverwrite, tosMask, srcAddress, dstAddress, srcPort,
                dstPort, priority));
    }

    private void addExtClassifier(final Short index, final ExtClassifier qosExtClassifier) {
        // Extended classifier
        final byte priority = (byte) (index+64);
        final ActivationState activationState = ActivationState.ACTIVE;
        // Protocol -- zero is match any
        final Protocol protocol;
        if (qosExtClassifier.getProtocol() != null) {
            protocol = Protocol.valueOf(qosExtClassifier.getProtocol().getValue().shortValue());
        } else {
            protocol = Protocol.NONE;
        }

        // default source port range must be set to match any even if qosExtClassifier has no range
        // match any port range is 0-65535, NOT 0-0
        // TODO - try to make these two variables immutable
        short srcStartPort = (short) 0;
        short srcEndPort = (short) 65535;
        if (qosExtClassifier.getSrcPortStart() != null) {
            srcStartPort = qosExtClassifier.getSrcPortStart().getValue().shortValue();
            srcEndPort = srcStartPort;
            if (qosExtClassifier.getSrcPortEnd() != null) {
                srcEndPort = qosExtClassifier.getSrcPortEnd().getValue().shortValue();
            }
            if ((int)(srcStartPort & 0xffff) > (int) (srcEndPort & 0xffff)) {
                logger.warn("Start port %d > End port %d in ext-classifier source port range -- forcing to same",
                        srcStartPort, srcEndPort);
                srcEndPort = srcStartPort;
            }
        }
        // default destination port range must be set to match any even if qosExtClassifier has no range
        // match any port range is 0-65535, NOT 0-0
        // TODO - try to make these two variables immutable
        short dstStartPort = (short) 0;
        short dstEndPort = (short) 65535;
        if (qosExtClassifier.getDstPortStart() != null) {
            dstStartPort = qosExtClassifier.getDstPortStart().getValue().shortValue();
            dstEndPort = dstStartPort;
            if (qosExtClassifier.getDstPortEnd() != null) {
                dstEndPort = qosExtClassifier.getDstPortEnd().getValue().shortValue();
            }
            if ((int)(dstStartPort & 0xffff) > (int)(dstEndPort & 0xffff)) {
                logger.warn("Start port %d > End port %d in ext-classifier destination port range -- forcing to same",
                        dstStartPort, dstEndPort);
                dstEndPort = dstStartPort;
            }
        }

        // DSCP/TOP byte
        // TODO - try to make these two variables immutable
        byte tosOverwrite = 0;
        byte tosMask = (byte)0x00;
        if (qosExtClassifier.getTosByte() != null) {
            // OR in the DSCP/TOS enable bit 0x01
            tosOverwrite = (byte) (qosExtClassifier.getTosByte().getValue().byteValue() | 0x01);
            if (qosExtClassifier.getTosMask() != null) {
                tosMask = qosExtClassifier.getTosMask().getValue().byteValue();
            } else {
                // set default TOS mask
                tosMask = (byte) 0xff;
            }
        }

        // IP Addresss and mask wildcards - addr byte 0 for no match (or match anything) and mask is 255.255.255.255 by default
        Inet4Address srcIpAddr = (Inet4Address) getByName("0.0.0.0");

        if (qosExtClassifier.getSrcIp() != null) {
            srcIpAddr = getInet4Address(qosExtClassifier.getSrcIp());
        }

        Inet4Address dstIpAddr = (Inet4Address) getByName("0.0.0.0");
        if (qosExtClassifier.getDstIp() != null) {
            dstIpAddr = getInet4Address(qosExtClassifier.getDstIp());
        }

        //mask
        Inet4Address srcIpMask = (Inet4Address) getByName("255.255.255.255");
        if (qosExtClassifier.getSrcIpMask() != null) {
            srcIpMask = getInet4Address(qosExtClassifier.getSrcIpMask());
        }

        Inet4Address dstIpMask = (Inet4Address) getByName("255.255.255.255");
        if (qosExtClassifier.getDstIpMask() != null) {
            dstIpMask = getInet4Address(qosExtClassifier.getDstIpMask());
        }

        // TODO - find out what the classifier ID should really be. It was never getting set previously
        final short classifierId = (short)index;

        // TODO - find out what the action value should really be. It was never getting set previously
        final IExtendedClassifier.Action action = IExtendedClassifier.Action.ADD;

        // push the extended classifier to the gate request
        classifiers.add(new org.pcmm.gates.impl.ExtendedClassifier(protocol, tosOverwrite, tosMask,
                srcIpAddr, dstIpAddr,
                srcStartPort, dstStartPort, priority, srcIpMask, dstIpMask, srcEndPort, dstEndPort, classifierId, activationState,
                action));
    }

    private Inet4Address getInet4Address(
            final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address address) {
        if (address != null) {
            final InetAddress out = getByName(address.getValue());
            if (out != null && out instanceof Inet4Address) {
                return (Inet4Address) out;
            }
        }
        return null;
    }

    private void addIpv6Classifier(final Short index, final Ipv6Classifier qosIpv6Classifier) {
        // Next Header
        final short nextHdr;
        if (qosIpv6Classifier.getNextHdr() != null) {
            nextHdr = qosIpv6Classifier.getNextHdr().getValue().shortValue();
        }
        // default: match any nextHdr is 256 because nextHdr 0 is Hop-by-Hop option
        else {
            nextHdr = (short) 256;
        }

        // Source IPv6 address & prefix len
        // TODO - try to make these two variables immutable
        byte srcPrefixLen = (byte) 128;
        Inet6Address srcAddress = (Inet6Address) getByName("0::0");

        if (qosIpv6Classifier.getSrcIp6() != null) {
            String[] parts = qosIpv6Classifier.getSrcIp6().getValue().split("/");
            String Ipv6AddressStr = parts[0];
            srcAddress = (Inet6Address) getByName(Ipv6AddressStr);
            if (parts.length > 1) {
                srcPrefixLen = (byte) Integer.parseInt(parts[1]);
            } else {
                srcPrefixLen = (byte) 128;
            }

        }

        // TODO - try to make these two variables immutable
        Inet6Address dstAddress = (Inet6Address) getByName("0::0");

        byte dstPrefLen = (byte) 128;
        // Destination IPv6 address & prefix len
        if (qosIpv6Classifier.getDstIp6() != null) {
            final String[] parts = qosIpv6Classifier.getDstIp6().getValue().split("/");
            final String Ipv6AddressStr = parts[0];
            dstAddress = (Inet6Address)getByName(Ipv6AddressStr);
            if (parts.length > 1) dstPrefLen = (byte) Integer.parseInt(parts[1]);
            else dstPrefLen = (byte) 128;
        }

        // default source port range must be set to match any -- even if qosExtClassifier has no range value
        // match any port range is 0-65535, NOT 0-0
        short srcPortBegin = (short) 0;
        short srcPortEnd = (short) 65535;
        if (qosIpv6Classifier.getSrcPortStart() != null) {
            srcPortBegin = qosIpv6Classifier.getSrcPortStart().getValue().shortValue();
            srcPortEnd = srcPortBegin;
            if (qosIpv6Classifier.getSrcPortEnd() != null) {
                srcPortEnd = qosIpv6Classifier.getSrcPortEnd().getValue().shortValue();
            }
            if ((int)(srcPortBegin & 0xffff) > (int)(srcPortEnd & 0xffff)) {
                logger.warn("Start port %d > End port %d in ipv6-classifier source port range -- forcing to same",
                        srcPortBegin, srcPortEnd);
                srcPortEnd = srcPortBegin;
            }
        }

        // default destination port range must be set to match any -- even if qosExtClassifier has no range value
        // match any port range is 0-65535, NOT 0-0
        short dstPortBegin = (short) 0;
        short dstPortEnd = (short) 65535;
        if (qosIpv6Classifier.getDstPortStart() != null) {
            dstPortBegin = qosIpv6Classifier.getDstPortStart().getValue().shortValue();
            dstPortEnd = dstPortBegin;
            if (qosIpv6Classifier.getDstPortEnd() != null) {
                dstPortEnd = qosIpv6Classifier.getDstPortEnd().getValue().shortValue();
            }
            if ( (int)(dstPortBegin & 0xffff) > (int)(dstPortEnd & 0xffff)) {
                logger.warn("Start port %d > End port %d in ipv6-classifier destination port range -- forcing to same",
                        dstPortBegin, dstPortEnd);
                dstPortEnd = dstPortBegin;
            }
        }

        final byte tcLow;
        if (qosIpv6Classifier.getTcLow() != null)
            tcLow = qosIpv6Classifier.getTcLow().getValue().byteValue();
        else tcLow = (byte) 0x00;

        final byte tcHigh;
        if (qosIpv6Classifier.getTcHigh() != null)
            tcHigh = qosIpv6Classifier.getTcHigh().getValue().byteValue();
        else tcHigh = (byte) 0x00;

        final byte tcMask;
        if (qosIpv6Classifier.getTcHigh() != null)
            tcMask = qosIpv6Classifier.getTcHigh().getValue().byteValue();
        else if (qosIpv6Classifier.getTcLow() != null) tcMask = (byte) 0xff;
        else tcMask = (byte) 0x00;

        FlowLabel flowLabelFlag = FlowLabel.IRRELEVANT;
        int flowLabelId = 0;

        if (qosIpv6Classifier.getFlowLabel() != null) {
            flowLabelFlag = FlowLabel.VALID;
            flowLabelId = qosIpv6Classifier.getFlowLabel().intValue();
        }


        // TODO - find out what the classifier ID should really be. It was never getting set previously
        final short classifierId = (short)index;

        // TODO - find out what the action value should really be. It was never getting set previously
        final IExtendedClassifier.Action action = IExtendedClassifier.Action.ADD;

        // push the IPv6 classifier to the gate request
        classifiers.add(
                new org.pcmm.gates.impl.IPv6Classifier(srcAddress, dstAddress, srcPortBegin, dstPortBegin, (byte) (index+64),
                        srcPortEnd, dstPortEnd, classifierId, ActivationState.ACTIVE, action, flowLabelFlag, tcLow,
                        tcHigh, tcMask, flowLabelId, nextHdr, srcPrefixLen, dstPrefLen));
    }
}

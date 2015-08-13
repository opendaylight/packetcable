/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates;

/**
 * The IPv6 Classifier object also specifies the packet matching rules associated with a Gate, when IPv6 Addresses are
 * used. As defined in Sections 6.4.3.1 and 6.4.3.2, for Unicast Gates multiple IPv6 Classifier objects may be included
 * in the Gate-Set to allow for complex classifier rules. However, since the ordering of objects in a message and the
 * order of processing those objects is not mandated, an AM SHOULD NOT send a GateSet with multiple IPv6
 * Classifiers with the same ClassificationID, yet different Actions. When an AM is using IPv6 Classifier objects, at
 * least one IPv6 Classifier MUST be provided by the PDP in all Gate-Set messages. For Unicast Gates more than one
 * IPv6 Classifier is allowed. For Multicast Gates only one IPv6 Classifier is required to be supported. Since the IPv6
 * Classifier is based on the DOCSIS IPv6 Classifier, all DOCSIS classifier semantics apply, with the exeption that at
 * least one IPv6 Classifier be present in a Gate-Set message.
 */
public interface IIPv6Classifier extends IExtendedClassifier {
//    short LENGTH = 64;
//    byte SNUM = 6;
    byte STYPE = 3;

    // flags: Flow Label match enable flag
    FlowLabel getFlowLabelEnableFlag();

    // Tc-low
    byte getTcLow();

    // Tc-high
    byte getTcHigh();

    // Tc-mask
    byte getTcMask();

    // Flow Label
    int getFlowLabel();

    // Next Header Type
    short getNextHdr();

    // Source Prefix Length
    byte getSourcePrefixLen();

    // Destination Prefix Length
    byte getDestinationPrefixLen();

    /**
     * The valid activation state values
     */
    enum FlowLabel {

        IRRELEVANT((byte) 0), VALID((byte) 1);

        FlowLabel(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static FlowLabel valueOf(byte v) {
            switch (v) {
                case 0:
                    return FlowLabel.IRRELEVANT;
                case 1:
                    return FlowLabel.VALID;
                default:
                    throw new IllegalArgumentException("not supported value");
            }
        }

        private byte value;

    }

}

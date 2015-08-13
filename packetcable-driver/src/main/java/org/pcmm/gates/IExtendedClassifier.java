/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.gates;

import java.net.InetAddress;

/**
 * The Extended Classifier object specifies the packet matching rules associated with a Gate, but includes more
 * detailed information for matching traffic, as well adding, modifying, deleting, activating and inactivating Classifiers.
 * As defined in sections 6.4.3.1 and 6.4.3.2, for Unicast Gates multiple Extended Classifier objects may be included
 * in the Gate-Set to allow for complex classifier rules. However, since the ordering of objects in a message and the
 * order of processing those objects is not mandated, and AM SHOULD NOT send a GateSet with multiple Extended
 * Classifiers with the same ClassifierID, yet different Actions. When an AM is using Extended Classifier objects, at
 * least one Extended Classifier is allowed. For Multicast Gates, only one Extended Classifier is required to be supported.
 * Since the Extended Classifier is based on the DOCSIS IP Classifier, all DOCSIS classifier semantics apply, with the
 * exception that at least one Extended Classifier be present in a Gate-Set message.
 *
 * Message length including header == 40
 */
public interface IExtendedClassifier extends IClassifier {

    byte STYPE = 2;

    /**
     * Returns the IP Source Mask value
     * @return - the InetAddress object
     */
    InetAddress getIPSourceMask();

    /**
     * Returns the IP Destination Mask value
     * @return - the InetAddress object
     */
    InetAddress getIPDestinationMask();

    /**
     * Returns the Start Source Port value
     * @return - the port number
     */
    short getSourcePortStart();

    /**
     * Returns the End Source Port value
     * @return - the port number
     */
    short getSourcePortEnd();

    /**
     * Returns the Start Destination Port value
     * @return - the port number
     */
    short getDestinationPortStart();

    /**
     * Returns the End Destination Port value
     * @return - the port number
     */
    short getDestinationPortEnd();

    /**
     * The ID value of this classifier
     * @return - the ID
     */
    short getClassifierID();

    /**
     * The activation state
     * @return
     */
    ActivationState getActivationState();

    byte getAction();

    /**
     * The valid activation state values
     */
    enum ActivationState {

        INACTIVE((byte) 0), ACTIVE((byte) 1);

        ActivationState(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static ActivationState valueOf(byte v) {
            switch (v) {
                case 0:
                    return ActivationState.INACTIVE;
                case 1:
                    return ActivationState.ACTIVE;
                default:
                    throw new IllegalArgumentException("not supported value");
            }
        }

        private byte value;

    }

}

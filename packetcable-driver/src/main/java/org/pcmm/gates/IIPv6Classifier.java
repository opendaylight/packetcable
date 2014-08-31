/**
 @header@
 */


package org.pcmm.gates;

public interface IIPv6Classifier extends IExtendedClassifier {
    static final short LENGTH = 64;
    static final byte SNUM = 6;
    static final byte STYPE = 3;

    // Tc-low
    // Tc-high
    // Tc-mask
    // Flow Label
    // Next Header Type
    // Source Prefix Length
    // Destination Prefix Length
    // IPv6 Source Address
    // IPv6 Destination Address
    // Source Port Start
    // Source Port End
    // Destination Port Start
    // Destination Port End
    // ClassifierID
    // Priority
    // Activation State
    // Action

}

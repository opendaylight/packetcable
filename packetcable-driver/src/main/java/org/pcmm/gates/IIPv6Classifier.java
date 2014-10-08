/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

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

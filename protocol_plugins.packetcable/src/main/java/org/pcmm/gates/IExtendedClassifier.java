/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */


package org.pcmm.gates;

import java.net.InetAddress;

public interface IExtendedClassifier extends IClassifier {

    static final short LENGTH = 40;
    static final byte SNUM = 6;
    static final byte STYPE = 2;

    void setIPSourceMask(InetAddress a);

    void setIPDestinationMask(InetAddress m);

    void setSourcePortStart(short p);

    void setSourcePortEnd(short p);

    void setDestinationPortStart(short p);

    void setDestinationPortEnd(short p);

    void setClassifierID(short p);

    /**
     * <pre>
     * 0x00 Inactive
     * 0x01 Active
     * </pre>
     *
     * @param s
     */
    void setActivationState(byte s);

    /**
     * <pre>
     * 0x00 Add classifier
     * 0x01 Replace classifier
     * 0x02 Delete classifier
     * 0x03 No change
     * </pre>
     *
     * @param a
     */
    void setAction(byte a);

    InetAddress getIPSourceMask();

    InetAddress getIPDestinationMask();

    short getSourcePortStart();

    short getSourcePortEnd();

    short getDestinationPortStart();

    short getDestinationPortEnd();

    short getClassifierID();

    byte getActivationState();

    byte getAction();
}

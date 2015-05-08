/**
 @header@
 */


package org.pcmm.gates;

import java.net.InetAddress;

public interface IIPv6Classifier extends IExtendedClassifier {
    short LENGTH = 64;
    byte SNUM = 6;
    byte STYPE = 3;

    // flags: Flow Label match enable flag
    void setFlowLabelEnableFlag(byte flag);
    byte getFlowLabelEnableFlag();

    // Tc-low
    void setTcLow(byte tcLow);
    byte getTcLow();

    // Tc-high
    void setTcHigh(byte tcHigh);
    byte getTcHigh();

    // Tc-mask
    void setTcMask(byte tcHigh);
    byte getTcMask();

    // Flow Label
    void setFlowLabel(Long flowLabel);
    int getFlowLabel();

    // Next Header Type
    void setNextHdr(short nxtHdr);
    short getNextHdr();

    // Source Prefix Length
    void setSourcePrefixLen(byte srcPrefixLen);
    byte getSourcePrefixLen();

    // Destination Prefix Length
    void setDestinationPrefixLen(byte dstPrefixLen);
    byte getDestinationPrefixLen();

    // IPv6 Source Address
    void setSourceIPAddress(InetAddress a);
    InetAddress getSourceIPAddress();

    // IPv6 Destination Address
    void setDestinationIPAddress(InetAddress a);
    InetAddress getDestinationIPAddress();

    // Source Port Start
    short getSourcePortStart();
    void setSourcePortStart(short p);

    // Source Port End
    short getSourcePortEnd();
    void setSourcePortEnd(short p);

    // Destination Port Start
    short getDestinationPortStart();
    void setDestinationPortStart(short p);

    // Destination Port End
    short getDestinationPortEnd();
    void setDestinationPortEnd(short p);

    // ClassifierID
    short getClassifierID();
    void setClassifierID(short p);

    // Priority
    void setPriority(byte p);
    byte getPriority();

    // Activation State
    void setActivationState(byte s);
    byte getActivationState();

    // Action
    void setAction(byte a);
    byte getAction();
}

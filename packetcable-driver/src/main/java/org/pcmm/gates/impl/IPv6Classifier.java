/**

 * Copyright (c) 2014 CableLabs.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IIPv6Classifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
public class IPv6Classifier extends PCMMBaseObject implements
        IIPv6Classifier {

    private Logger logger = LoggerFactory.getLogger(IPv6Classifier.class);

    public IPv6Classifier() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data - the data bytes to parse
     */
    public IPv6Classifier(byte[] data) {
        super(data);
    }

    /**
     * @param len - the classifier's length
     * @param sType - the sType value
     * @param sNum - the sNum value
     */
    public IPv6Classifier(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    // offset:length Field Name: Description
    // 00:01 Flags: 0000.0001 Flow Label enable match
    // 01:01 Tc-low
    // 02:01 Tc-high
    // 03:01 Tc-mask
    // 04:04 Flow Label: low order 20 bits; high order 12 bits ignored
    // 08:02 Next Header Type
    // 10:01 Source Prefix Length
    // 11:01 Destination Prefix Length
    // 12:16 IPv6 Source Address
    // 28:16 IPv6 Destination Address
    // 44:02 Source Port Start
    // 46:02 Source Port End
    // 48:02 Destination Port Start
    // 50:02 Destination Port End
    // 52:02 ClassifierID
    // 54:01 Priority
    // 55:01 Activation State
    // 56:01 Action
    // 57:03 Reserved

    // 00:01 Flags: 0000.0001 Flow Label enable match
    @Override
    public void setFlowLabelEnableFlag(byte flag) {
        setByte(flag, (short) 0);
    }
    @Override
    public byte getFlowLabelEnableFlag() {
        return getByte((short) 0);
    }

    // 01:01 Tc-low
    @Override
    public void setTcLow(byte tcLow) {
        setByte(tcLow, (short) 1);
    }
    @Override
    public byte getTcLow() {
        return getByte((short) 1);
    }

    // 02:01 Tc-high
    @Override
    public void setTcHigh(byte tcHigh) {
        setByte(tcHigh, (short) 2);
    }
    @Override
    public byte getTcHigh() {
        return getByte((short) 2);
    }

    // 03:01 Tc-mask
    @Override
    public void setTcMask(byte tcMask) {
        setByte(tcMask, (short) 3);
    }
    @Override
    public byte getTcMask() {
        return getByte((short) 3);
    }

    // 04:04 Flow Label: low order 20 bits; high order 12 bits ignored
    @Override
    public void setFlowLabel(Long flowLabel) {
        setInt(flowLabel.intValue(), (short) 4);
    }
    @Override
    public int getFlowLabel() {
        return getInt((short) 4);
    }

    // 08:02 Next Header Type
    @Override
    public void setNextHdr(short nxtHdr) {
        setShort(nxtHdr, (short) 8);
    }
    @Override
    public short getNextHdr() {
        return getShort((short) 8);
    }

    // 10:01 Source Prefix Length
    @Override
    public void setSourcePrefixLen(byte srcPrefixLen) {
        setByte(srcPrefixLen, (short) 10);
    }
    @Override
    public byte getSourcePrefixLen() {
        return getByte((short) 10);
    }

    // 11:01 Destination Prefix Length
    @Override
    public void setDestinationPrefixLen(byte dstPrefixLen) {
        setByte(dstPrefixLen, (short) 11);
    }
    @Override
    public byte getDestinationPrefixLen() {
        return getByte((short) 11);
    }

    // 12:16 IPv6 Source Address
    @Override
    public void setSourceIPAddress(InetAddress a) {
        setBytes(a.getAddress(), (short) 12);
    }
    @Override
    public InetAddress getSourceIPAddress() {
        try {
            return InetAddress.getByAddress(getBytes((short) 12, (short) 16));
        } catch (UnknownHostException e) {
            logger.error("getSourceIPAddress(): Malformed IPv6 address: {}", e.getMessage());
        }
        return null;
    }

    // 28:16 IPv6 Destination Address
    @Override
    public void setDestinationIPAddress(InetAddress a) {
        setBytes(a.getAddress(), (short) 28);
    }
    @Override
    public InetAddress getDestinationIPAddress() {
        try {
            return InetAddress.getByAddress(getBytes((short) 28, (short) 16));
        } catch (UnknownHostException e) {
            logger.error("getDestinationIPAddress(): Malformed IPv6 address: {}", e.getMessage());
        }
        return null;
    }
    // 44:02 Source Port Start
    @Override
    public short getSourcePortStart() {
        return getShort((short) 44);
    }
    @Override
    public void setSourcePortStart(short p) {
        setShort(p, (short) 44);
    }

    // 46:02 Source Port End
    @Override
    public short getSourcePortEnd() {
        return getShort((short) 46);
    }
    @Override
    public void setSourcePortEnd(short p) {
        setShort(p, (short) 46);
    }

    // 48:02 Destination Port Start
    @Override
    public short getDestinationPortStart() {
        return getShort((short) 48);
    }
    @Override
    public void setDestinationPortStart(short p) {
        setShort(p, (short) 48);
    }

    // 50:02 Destination Port End
    @Override
    public short getDestinationPortEnd() {
        return getShort((short) 50);
    }
    @Override
    public void setDestinationPortEnd(short p) {
        setShort(p, (short) 50);
    }

    // 52:02 ClassifierID
    @Override
    public short getClassifierID() {
        return getShort((short) 52);
    }

    @Override
    public void setClassifierID(short p) {
        setShort(p, (short) 52);
    }

    // 54:01 Priority
    @Override
    public void setPriority(byte p) {
        setByte(p, (short) 54);
    }
    @Override
    public byte getPriority() {
        return getByte((short) 54);
    }

    // 55:01 Activation State
    @Override
    public void setActivationState(byte s) {
        setByte(s, (short) 55);
    }
    @Override
    public byte getActivationState() {
        return getByte((short) 55);
    }

    // 56:01 Action
    @Override
    public void setAction(byte a) {
        setByte(a, (short) 56);
    }
    @Override
    public byte getAction() {
        return getByte((short) 56);
    }



    // baggage from IExtendedClassifier
    // not used in IPv6 classifiers
    @Override
    public void setIPSourceMask(InetAddress a) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIPDestinationMask(InetAddress m) {
        // TODO Auto-generated method stub

    }

    @Override
    public InetAddress getIPSourceMask() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InetAddress getIPDestinationMask() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public short getDestinationPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDestinationPort(short p) {
        // TODO Auto-generated method stub

    }

    @Override
    public short getSourcePort() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setSourcePort(short p) {
        // TODO Auto-generated method stub

    }

    @Override
    public short getProtocol() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setProtocol(short p) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte getDSCPTOS() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDSCPTOS(byte v) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte getDSCPTOSMask() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDSCPTOSMask(byte v) {
        // TODO Auto-generated method stub

    }

}

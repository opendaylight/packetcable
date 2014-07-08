/**
 @header@
 */
package org.pcmm.gates.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IExtendedClassifier;

/**
 *
 */
public class ExtendedClassifier extends PCMMBaseObject implements
            IExtendedClassifier {

    public ExtendedClassifier() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data
     */
    public ExtendedClassifier(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public ExtendedClassifier(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#getDestinationIPAddress()
     */
    @Override
    public InetAddress getDestinationIPAddress() {
        try {
            return InetAddress.getByAddress(getBytes((short) 12, (short) 4));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.pcmm.gates.IClassifier#setDestinationIPAddress(java.net.InetAddress)
     */
    @Override
    public void setDestinationIPAddress(InetAddress address) {
        setBytes(address.getAddress(), (short) 12);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#getDestinationPort()
     */
    @Override
    public short getDestinationPort() {
        return getShort((short) 24);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#setDestinationPort(short)
     */
    @Override
    public void setDestinationPort(short p) {
        setShort(p, (short) 24);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#getSourceIPAddress()
     */
    @Override
    public InetAddress getSourceIPAddress() {
        try {
            return InetAddress.getByAddress(getBytes((short) 4, (short) 4));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#setSourceIPAddress(java.net.InetAddress)
     */
    @Override
    public void setSourceIPAddress(InetAddress a) {
        setBytes(a.getAddress(), (short) 4);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#getSourcePort()
     */
    @Override
    public short getSourcePort() {
        return getShort((short) 20);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#setSourcePort(short)
     */
    @Override
    public void setSourcePort(short p) {
        setShort(p, (short) 20);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#getProtocol()
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.valueOf(getShort((short) 0));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#setProtocol(short)
     */
    @Override
    public void setProtocol(Protocol p) {
        setShort(p.getValue(), (short) 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#getPriority()
     */
    @Override
    public byte getPriority() {
        return getByte((short) 30);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IClassifier#setPriority(byte)
     */
    @Override
    public void setPriority(byte p) {
        setByte(p, (short) 30);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getIPSourceMask()
     */
    @Override
    public InetAddress getIPSourceMask() {
        try {
            return InetAddress.getByAddress(getBytes((short) 8, (short) 4));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.pcmm.gates.IExtendedClassifier#setIPSourceMask(java.net.InetAddress)
     */
    @Override
    public void setIPSourceMask(InetAddress a) {
        setBytes(a.getAddress(), (short) 8);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getIPDestinationMask()
     */
    @Override
    public InetAddress getIPDestinationMask() {
        try {
            return InetAddress.getByAddress(getBytes((short) 16, (short) 4));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.pcmm.gates.IExtendedClassifier#setIPDestinationMask(java.net.InetAddress
     * )
     */
    @Override
    public void setIPDestinationMask(InetAddress m) {
        setBytes(m.getAddress(), (short) 16);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getSourcePortStart()
     */
    @Override
    public short getSourcePortStart() {
        return getShort((short) 20);
    }

    @Override
    public void setSourcePortStart(short p) {
        setShort(p, (short) 20);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getSourcePortEnd()
     */
    @Override
    public short getSourcePortEnd() {
        return getShort((short) 22);
    }

    @Override
    public void setSourcePortEnd(short p) {
        setShort(p, (short) 22);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getDestinationPortStart()
     */
    @Override
    public short getDestinationPortStart() {
        return getShort((short) 24);
    }

    @Override
    public void setDestinationPortStart(short p) {
        setShort(p, (short) 24);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getDestinationPortEnd()
     */
    @Override
    public short getDestinationPortEnd() {
        return getShort((short) 26);
    }

    @Override
    public void setDestinationPortEnd(short p) {
        setShort(p, (short) 26);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getClassifierID()
     */
    @Override
    public short getClassifierID() {
        return getShort((short) 28);
    }

    @Override
    public void setClassifierID(short p) {
        setShort(p, (short) 28);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getActivationState()
     */
    @Override
    public byte getActivationState() {
        return getByte((short) 31);
    }

    @Override
    public void setActivationState(byte s) {
        setByte(s, (short) 31);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IExtendedClassifier#getAction()
     */
    @Override
    public byte getAction() {
        return getByte((short) 32);
    }

    @Override
    public void setAction(byte a) {
        setByte(a, (short) 32);
    }

    @Override
    public byte getDSCPTOS() {
        return getByte((short) 2);
    }

    @Override
    public void setDSCPTOS(byte v) {
        setByte(v, (short) 2);
    }

    @Override
    public byte getDSCPTOSMask() {
        return getByte((short) 3);
    }

    @Override
    public void setDSCPTOSMask(byte v) {
        setByte(v, (short) 3);
    }

}

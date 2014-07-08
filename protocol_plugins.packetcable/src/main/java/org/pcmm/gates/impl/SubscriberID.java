/**
 @header@
 */
package org.pcmm.gates.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ISubscriberID;

/**
 *
 */
public class SubscriberID extends PCMMBaseObject implements ISubscriberID {

    /**
     *
     */
    public SubscriberID() {
        this(LENGTH, STYPE, SNUM);
    }

    /**
     * @param data
     */
    public SubscriberID(byte[] data) {
        super(data);
    }

    /**
     * @param len
     * @param sType
     * @param sNum
     */
    public SubscriberID(short len, byte sType, byte sNum) {
        super(len, sType, sNum);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ISubscriberID#getSourceIPAddress()
     */
    @Override
    public InetAddress getSourceIPAddress() {
        try {
            return Inet4Address.getByAddress(getBytes((short) 0, (short) 4));
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.pcmm.gates.ISubscriberID#setSourceIPAddress(java.net.InetAddress)
     */
    @Override
    public void setSourceIPAddress(InetAddress address) {
        setBytes(address.getAddress(), (short) 0);
    }

}

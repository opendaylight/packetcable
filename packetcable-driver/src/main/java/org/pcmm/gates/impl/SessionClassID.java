/**
 @header@
 */
package org.pcmm.gates.impl;

import org.pcmm.gates.ISessionClassID;

/**
 *
 */
public class SessionClassID implements ISessionClassID {

    private byte priority;
    private byte preemption;

    // TODO check this;
    private byte session;

    public SessionClassID() {
        this((byte) 0);
    }

    public SessionClassID(byte value) {
        session = value;
        priority = 0;
        preemption = 0;
        priority |= value >> 2;
        preemption |= value >> 3;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ISessionClassID#getPriority()
     */
    @Override
    public byte getPriority() {
        return priority;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ISessionClassID#setPriority(byte)
     */
    @Override
    public void setPriority(byte value) {
        this.priority = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ISessionClassID#getPreemption()
     */
    @Override
    public byte getPreemption() {
        return preemption;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ISessionClassID#setPreemption(byte)
     */
    @Override
    public void setPreemption(byte value) {
        this.preemption = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.ISessionClassID#toSingleByte()
     */
    @Override
    public byte toSingleByte() {
        // byte ret = 0;
        // ret |= (priority << 2);
        // ret |= (preemption & 0xf);
        // return ret;
        return session;
    }
}

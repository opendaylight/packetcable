/**
 @header@
 */
package org.pcmm.gates;

/**
 *
 */
public interface ISessionClassID {

    /**
     * gets the priority bits
     *
     * @return O-7 priority value
     */
    byte getPriority();

    /**
     * sets the priority value (0-7)
     *
     * @param value
     *            priority
     */
    void setPriority(byte value);

    /**
     * gets the preemption value;
     *
     * @return peemption
     */
    byte getPreemption();

    /**
     * * sets the preemption
     *
     * @param value
     *            preemption
     */
    void setPreemption(byte value);

    /**
     * compress the priority and preemption to a single byte value;
     *
     * @return SessionClassID as byte
     */
    byte toSingleByte();

}

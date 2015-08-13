/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
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
     * gets the preemption value;
     *
     * @return peemption
     */
    byte getPreemption();

    /**
     * compress the priority and preemption to a single byte value;
     *
     * @return SessionClassID as byte
     */
    byte toSingleByte();

}

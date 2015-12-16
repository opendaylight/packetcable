/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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

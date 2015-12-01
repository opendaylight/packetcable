/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 *
 * From the Packetcable Multimedia specification section 6.4.2.13
 *
 * The Gate Usage Info object contains a counter indicating the number of kilobytes transmitted
 * over this Gate.
 */
public interface IGateUsageInfo extends IPCMMBaseObject {

    /**
     * The S-Type for Gate Time Info
     */
    byte STYPE = 1;


    /**
     * Time Committed total amount of time the Gate has been in the Committed and Committed
     * Recovery states
     *
     * @return usage in kbps;
     */
   long getGateUsageInfo();
}
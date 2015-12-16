/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm;

import org.umu.cops.common.COPS_def;

public class PCMMDef extends COPS_def {

    public static final short C_PCMM = (short) 0x800A;

    /**
     * Get a representative string for an COPS Client Type.
     *
     * @param cType
     *            COPS Client Type
     * @return A representative <tt>String</tt>
     *
     */
    public String strClientType(short cType) {
        switch (cType) {
        case C_PCMM:
            return ("C_PCMM");
        default:
            return super.strClientType(cType);
        }
    }

}

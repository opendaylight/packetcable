/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm;

public interface PCMMConstants {

	// Port used by the PCMM
	public static final String PCMM_PORT = "pcmm.port";
	// Pool size, determining the number of connections that could be
	// established with CMTSs
	public static final String PS_POOL_SIZE = "pcmm.ps.pool.size";
	// Default keep-alive timer value (secs)
	public static final String KA_TIMER = "pcmm.keep.alive.timer";
	// Default accounting timer value (secs)
	public static final String ACC_TIMER = "pcmm.accounting.timer";
	// default ip mask
	public static final String DEFAULT_MASK = "pcmm.default.mask";
	// default timeout
	public static final String DEFAULT_TIEMOUT = "pcmm.default.timeout";

}

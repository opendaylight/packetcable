/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */

package org.pcmm.utils;

import org.pcmm.gates.IPCMMError;

/**
 * Defines the Exception that could be thrown by the API.
 * 
 */
public class PCMMException extends Exception {

	public PCMMException(IPCMMError error) {
		this(error.getDescription(), error.getErrorCode());
	}

	public PCMMException(String message, int code) {
		super("error code [" + code + "]" + message);
	}

}

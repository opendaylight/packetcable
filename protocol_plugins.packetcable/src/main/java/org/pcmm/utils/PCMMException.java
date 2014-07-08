/**
 @header@
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

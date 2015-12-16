/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.base.impl;

import com.google.common.primitives.Bytes;
import org.pcmm.base.IPCMMBaseObject;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSMsgParser;
import org.umu.cops.stack.COPSObjectParser;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Abstract implementation of the base class {@link IPCMMBaseObject}
 * Implementations of this class are used for sending/receiving PCMM data to/from a COPS client
 */
public abstract class PCMMBaseObject implements IPCMMBaseObject {

	protected static final short HEADER_OFFSET = (short) 4;
	protected static final short PADDING_OFFSET = (short) 4;

	// The following two attributes denote the type of PCMMBaseObject
	private final SNum sNum;
	private final byte sType;

	/**
	 * Constructor
	 * @param sNum - the S-Num
	 * @param sType - the S-Type
	 */
	protected PCMMBaseObject(final SNum sNum, final byte sType) {
		if (sNum == null) throw new IllegalArgumentException("Invalid or null SNum");
		this.sNum = sNum;
		this.sType = sType;
	}

	@Override
	public final void writeData(final Socket socket) throws IOException {
		final byte[] data = getAsBinaryArray();
		socket.getOutputStream().write(data, 0, data.length);
	}

	@Override
	public final byte[] getAsBinaryArray() {
		final byte[] data = getBytes();
		final COPSData padding;
		if ((data.length % PADDING_OFFSET) != 0) {
			final int padLen = PADDING_OFFSET - (data.length % PADDING_OFFSET);
			padding = COPSObjectParser.getPadding(padLen);
		} else {
			padding = new COPSData();
		}

		final int payloadSize = data.length + padding.length() + HEADER_OFFSET;
		final List<Byte> outBytes = new ArrayList<>();
		outBytes.addAll(Bytes.asList(COPSMsgParser.shortToBytes((short) payloadSize)));
		outBytes.add(sNum.getValue());
		outBytes.add(sType);
		outBytes.addAll(Bytes.asList(data));
		outBytes.addAll(Bytes.asList(padding.getData()));
		return Bytes.toArray(outBytes);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PCMMBaseObject)) {
			return false;
		}

		final PCMMBaseObject that = (PCMMBaseObject) o;
		return sType == that.sType && sNum == that.sNum;
	}

	@Override
	public int hashCode() {
		int result = sNum.hashCode();
		result = 31 * result + (int) sType;
		return result;
	}

	/**
	 * Returns the byte array consisting of the data contained within the implementation class
	 * @return - the byte array
	 */
	protected abstract byte[] getBytes();

	/**
	 * Enumeration of SNum values that is used by the client to determine which Object type is being sent
	 */
	public enum SNum {
		TRANSACTION_ID((byte) 1),
		AMID((byte) 2),
		SUBSCRIBER_ID((byte) 3),
		GATE_ID((byte) 4),
		GATE_SPEC((byte) 5),
		CLASSIFIERS((byte) 6),
		TRAFFIC_PROFILE((byte) 7),
		EVENT_GEN_INFO((byte) 8),
		VOL_BASED_USAGE_LIMIT((byte) 9),
		TIME_BASED_USAGE_LIMIT((byte) 10),
		OPAQUE_DATA((byte) 11),
		GATE_TIME_INFO((byte) 12),
		GATE_USAGE_INFO((byte) 13),
		PCMM_ERROR((byte) 14),
		GATE_STATE((byte) 15),
		VERSION_INFO((byte) 16),
		PSID((byte) 17),
		SYNC_OPTS((byte) 18),
		MSG_RECEIPT_KEY((byte) 19),
		USER_ID((byte) 20),
		SHARED_RES_ID((byte) 21);

		private byte value;

		SNum(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		public static SNum valueOf(byte v) {
			switch (v) {
				case 1:
					return SNum.TRANSACTION_ID;
				case 2:
					return SNum.AMID;
				case 3:
					return SNum.SUBSCRIBER_ID;
				case 4:
					return SNum.GATE_ID;
				case 5:
					return SNum.GATE_SPEC;
				case 6:
					return SNum.CLASSIFIERS;
				case 7:
					return SNum.TRAFFIC_PROFILE;
				case 8:
					return SNum.EVENT_GEN_INFO;
				case 9:
					return SNum.VOL_BASED_USAGE_LIMIT;
				case 10:
					return SNum.TIME_BASED_USAGE_LIMIT;
				case 11:
					return SNum.OPAQUE_DATA;
				case 12:
					return SNum.GATE_TIME_INFO;
				case 13:
					return SNum.GATE_USAGE_INFO;
				case 14:
					return SNum.PCMM_ERROR;
				case 15:
					return SNum.GATE_STATE;
				case 16:
					return SNum.VERSION_INFO;
				case 17:
					return SNum.PSID;
				case 18:
					return SNum.SYNC_OPTS;
				case 19:
					return SNum.MSG_RECEIPT_KEY;
				case 20:
					return SNum.USER_ID;
				case 21:
					return SNum.SHARED_RES_ID;
				default:
					throw new IllegalArgumentException("not supported value");
			}
		}

	}



}

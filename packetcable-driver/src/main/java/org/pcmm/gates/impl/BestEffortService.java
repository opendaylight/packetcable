/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import com.google.common.primitives.Bytes;
import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITrafficProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * The Best Effort object defines the Traffic Profile associated with a gate through an upstream DOCSIS-specific
 * parameterization scheme.
 */
public class BestEffortService extends PCMMBaseObject implements ITrafficProfile {

	public static final byte STYPE = 3;
	// XXX -> 60=0x3C, 112 = 0x70, 164=0xA4
	// Length = 44=0x2C, 80=0x50 or 116=0x74

	public static final byte DEFAULT_TRAFFIC_PRIORITY = 0;

	public static final int DEFAULT_MAX_TRAFFIC_BURST = 3044;

	/**
	 * The envelope
	 */
	private final byte envelope;

	/**
	 * The authorized envelope. See BEEnvelope for description of the attributes. MUST NOT be NULL.
	 */
	private final BEEnvelop authorizedEnvelop;

	/**
	 * The reserved envelope. See BEEnvelope for description of the attributes. CAN BE NULL.
	 */
	private final BEEnvelop reservedEnvelop;

	/**
	 * The committed envelope. See BEEnvelope for description of the attributes. CAN BE NULL.
	 */
	private final BEEnvelop committedEnvelop;

	/**
	 * General use constructor
	 * @param auth - the authorized envelope (required)
	 * @param reserved - the reserved envelope (optional)
	 * @param committed - the committed envelope (optional)
	 */
	public BestEffortService(final BEEnvelop auth, final BEEnvelop reserved, final BEEnvelop committed) {
		this(DEFAULT_ENVELOP, auth, reserved, committed);
	}

	/**
	 * Constructor generally used for byte parsing only.
	 * @param envelope - the envelope value
	 * @param auth - the authorized envelope (required)
	 * @param reserved - the reserved envelope (optional)
	 * @param committed - the committed envelope (optional)
	 */
	protected BestEffortService(final byte envelope, final BEEnvelop auth, final BEEnvelop reserved,
							 final BEEnvelop committed) {
		super(SNum.TRAFFIC_PROFILE, STYPE);
		if (auth == null) throw new IllegalArgumentException("AUTHORIZED envelope must not be null");

		// TODO - Cannot figure out any other means to parse the bytes unless this is true. Determine if correct???
		if (reserved == null && committed != null)
			throw new IllegalArgumentException("Cannot have a committed envelope without a reserved");

		this.envelope = envelope;
		this.authorizedEnvelop = auth;
		this.reservedEnvelop = reserved;
		this.committedEnvelop = committed;
	}

	@Override
	public byte getEnvelop() {
		return envelope;
	}

	// Getters
	public BEEnvelop getAuthorizedEnvelop() {
		return authorizedEnvelop;
	}

	public BEEnvelop getReservedEnvelop() {
		return reservedEnvelop;
	}

	public BEEnvelop getCommittedEnvelop() {
		return committedEnvelop;
	}

	@Override
	public byte[] getBytes() {
		final List<Byte> byteList = new ArrayList<>();
		byteList.addAll(Bytes.asList(envelope, (byte) 0, (byte) 0, (byte) 0));
		byteList.addAll(authorizedEnvelop.getBytes());
		if (reservedEnvelop != null) byteList.addAll(reservedEnvelop.getBytes());
		if (committedEnvelop != null) byteList.addAll(committedEnvelop.getBytes());
		return Bytes.toArray(byteList);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BestEffortService)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final BestEffortService that = (BestEffortService) o;
		return envelope == that.envelope && authorizedEnvelop.equals(that.authorizedEnvelop) &&
				!(reservedEnvelop != null ? !reservedEnvelop.equals(that.reservedEnvelop) :
						that.reservedEnvelop != null) &&
				!(committedEnvelop != null ? !committedEnvelop.equals(that.committedEnvelop) :
						that.committedEnvelop != null);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (int) envelope;
		result = 31 * result + authorizedEnvelop.hashCode();
		result = 31 * result + (reservedEnvelop != null ? reservedEnvelop.hashCode() : 0);
		result = 31 * result + (committedEnvelop != null ? committedEnvelop.hashCode() : 0);
		return result;
	}

	/**
	 * Returns a BestEffortService object from a byte array
	 * @param data - the data to parse
	 * @return - the object or null if cannot be parsed
	 * TODO - make me more robust as RuntimeExceptions can be thrown here.
	 */
	public static BestEffortService parse(final byte[] data) {
		final List<Byte> bytes = Bytes.asList(data);
		bytes.subList(0, 51);
		if (data.length >= 56 && data.length < 108)
			return new BestEffortService(data[0], BEEnvelop.parse(Bytes.toArray(bytes.subList(4, 56))), null, null);
		else if (data.length >= 108 && data.length < 160)
			return new BestEffortService(data[0], BEEnvelop.parse(Bytes.toArray(bytes.subList(4, 56))),
					BEEnvelop.parse(Bytes.toArray(bytes.subList(56, 108))), null);
		else if (data.length >= 160)
				return new BestEffortService(data[0], BEEnvelop.parse(Bytes.toArray(bytes.subList(4, 56))),
						BEEnvelop.parse(Bytes.toArray(bytes.subList(56, 108))),
						BEEnvelop.parse(Bytes.toArray(bytes.subList(108, 160))));
		else return null;
	}

}

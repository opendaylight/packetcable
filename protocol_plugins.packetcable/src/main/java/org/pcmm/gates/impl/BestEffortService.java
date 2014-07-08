/**
 @header@
 */
package org.pcmm.gates.impl;

import java.util.Arrays;

// import org.junit.Assert;
import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.ITrafficProfile;
import org.umu.cops.stack.COPSData;

/**
 *
 */
public class BestEffortService extends PCMMBaseObject implements
		ITrafficProfile {
	public static final byte STYPE = 3;
	// XXX -> 60=0x3C, 112 = 0x70, 164=0xA4
	// Length = 44=0x2C, 80=0x50 or 116=0x74
	public static final short LENGTH = 44;

	public static final byte DEFAULT_TRAFFIC_PRIORITY = 0;
	// Authorized
	public static final byte DEFAULT_ENVELOP = 0x7;

	public static final int DEFAULT_MAX_TRAFFIC_BURST = 3044;

	private BEEnvelop authorizedEnvelop;

	private BEEnvelop reservedEnvelop;

	private BEEnvelop committedEnvelop;

	/**
	 * 
	 * @param e
	 *            envelop
	 */
	public BestEffortService(byte e) {
		super((short) (e == 1 ? LENGTH : (e == 7 ? 116 : 80)), STYPE, SNUM);
		setEnvelop(e);
		authorizedEnvelop = new BEEnvelop();
		if (e > 1) {
			reservedEnvelop = new BEEnvelop();
			if (e == 7)
				committedEnvelop = new BEEnvelop();
		}
	}

	public BestEffortService(byte[] bytes) {
		super(bytes);
		byte e = getEnvelop();
		authorizedEnvelop = new BEEnvelop(headPadding(offset, Arrays.copyOfRange(bytes, 8, LENGTH)));
		if (e > 1) {
			reservedEnvelop = new BEEnvelop(headPadding(offset, Arrays.copyOfRange(bytes, LENGTH, 80)));
			if (e == 7)
				committedEnvelop = new BEEnvelop(headPadding(offset, Arrays.copyOfRange(bytes, 80, 116)));
		}
	}

	@Override
	public void setEnvelop(byte e) {
		setLength((short) (e == 1 ? LENGTH : (e == 7 ? 116 : 80)));
		// reset cops data to fit the new length
		byte[] array = new byte[getLength() - offset];
		Arrays.fill(array, (byte) 0);
		setData(new COPSData(array, 0, array.length));
		setByte(e, (short) 0);
	}

	@Override
	public byte getEnvelop() {
		return getByte((short) 0);
	}

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
	public byte[] getAsBinaryArray() {
		byte[] returnBuffer = super.getAsBinaryArray();

		{// fill buffer with the Authorized Envelop
			byte[] authEnv = Arrays.copyOfRange(getAuthorizedEnvelop().getAsBinaryArray(), offset, BEEnvelop.LENGHT);
			// offset + 4 since the Envelop data begin from byte nb 8
			System.arraycopy(authEnv, 0, returnBuffer, offset + 4, authEnv.length);
		}
		if (getReservedEnvelop() != null) {
			byte[] reservedEnv = Arrays.copyOfRange(getReservedEnvelop().getAsBinaryArray(), offset, BEEnvelop.LENGHT);
			System.arraycopy(reservedEnv, 0, returnBuffer, LENGTH, reservedEnv.length);
		}
		if (getCommittedEnvelop() != null) {
			byte[] commitEnv = Arrays.copyOfRange(getCommittedEnvelop().getAsBinaryArray(), offset, BEEnvelop.LENGHT);
			System.arraycopy(commitEnv, 0, returnBuffer, LENGTH + 36, commitEnv.length);
		}
		return returnBuffer;
	}

	/**
     *
     *
     */
	public static class BEEnvelop extends PCMMBaseObject {
		// basically we need 36 bytes but since PCMMBasedObject needs 4 bytes
		// more we allocate 40 bytes and then subtract them when setting BE
		// data.
		private final static short LENGHT = 40;

		protected BEEnvelop() {
			super(LENGHT, (byte) 0, (byte) 0);
			setTrafficPriority(DEFAULT_TRAFFIC_PRIORITY);
		}

		protected BEEnvelop(byte[] buffer) {
			super(buffer);
		}

		public void setTrafficPriority(byte p) {
			setByte(p, (short) 0);
		}

		public byte getTrafficPriority() {
			return getByte((short) 0);
		}

		//
		public void setRequestTransmissionPolicy(int p) {
			setInt(p, (short) 4);
		}

		public int getRequestTransmissionPolicy() {
			return getInt((short) 4);
		}

		public int getMaximumSustainedTrafficRate() {
			return getInt((short) 8);
		}

		public void setMaximumSustainedTrafficRate(int p) {
			setInt(p, (short) 8);
		}

		public int getMaximumTrafficBurst() {
			return getInt((short) 12);
		}

		public void setMaximumTrafficBurst(int p) {
			setInt(p, (short) 12);
		}

		public int getMinimumReservedTrafficRate() {
			return getInt((short) 16);
		}

		public void setMinimumReservedTrafficRate(int p) {
			setInt(p, (short) 16);
		}

		public short getAssumedMinimumReservedTrafficRatePacketSize() {
			return getShort((short) 20);
		}

		public void setAssumedMinimumReservedTrafficRatePacketSize(short p) {
			setShort(p, (short) 20);
		}

		public short getMaximumConcatenatedBurst() {
			return getShort((short) 22);
		}

		public void setMaximumConcatenatedBurst(short p) {
			setShort(p, (short) 22);
		}

		public int getRequiredAttributeMask() {
			return getInt((short) 24);
		}

		public void setRequiredAttributeMask(int p) {
			setInt(p, (short) 24);
		}

		public int getForbiddenAttributeMask() {
			return getInt((short) 28);
		}

		public void setForbiddenAttributeMask(int p) {
			setInt(p, (short) 28);
		}

		public int getAttributeAggregationRuleMask() {
			return getInt((short) 32);
		}

		public void setAttributeAggregationRuleMask(int p) {
			setInt(p, (short) 32);
		}

	}

}

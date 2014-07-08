/**
 @header@
 */
package org.pcmm.objects;

import org.pcmm.base.impl.PCMMBaseObject;

/**
 * 
 * PCMM SyncOptions object
 * 
 */
public class SyncOptions extends PCMMBaseObject {

	private byte synchType;

	private byte reportType;

	public static final byte STANDARD_REPORT_DATA = (byte) 0;
	public static final byte COMPLETE_GATE_DATA = (byte) 1;
	public static final byte FULL_SYNCHRONIZATION = (byte) 0;
	public static final byte INCREMENTAL_SYNCHRONIZATION = (byte) 1;

	public SyncOptions() {
		this(STANDARD_REPORT_DATA, FULL_SYNCHRONIZATION);
	}

	public SyncOptions(byte reportType, byte synchType) {
		super((short) 8, (byte) 1, (byte) 18);
		setByte(this.reportType = reportType, (short) 4);
		setByte(this.synchType = synchType, (short) 6);
	}

	/**
	 * Parse data and create COPSHandle object
	 */
	public SyncOptions(byte[] dataPtr) {
		super(dataPtr);
		reportType = getByte((short) 4);
		synchType = getByte((short) 6);
	}

	/**
	 * @return the synchType
	 */
	public byte getSynchType() {
		return synchType;
	}

	/**
	 * @param synchType
	 *            the synchType to set
	 */
	public void setSynchType(byte synchType) {
		this.synchType = synchType;
	}

	/**
	 * @return the reportType
	 */
	public byte getReportType() {
		return reportType;
	}

	/**
	 * @param reportType
	 *            the reportType to set
	 */
	public void setReportType(byte reportType) {
		this.reportType = reportType;
	}

}

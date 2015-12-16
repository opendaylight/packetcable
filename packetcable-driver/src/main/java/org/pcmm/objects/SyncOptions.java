/*
 * Copyright (c) 2014, 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.objects;

import org.pcmm.base.impl.PCMMBaseObject;
import org.umu.cops.stack.COPSMsgParser;

/**
 * PCMM SyncOptions object
 */
public class SyncOptions extends PCMMBaseObject {

	/**
	 * The requested report type
	 */
	private final ReportType reportType;

	/**
	 * The requested type of synchronization
	 */
	private final SyncType syncType;

	/**
	 * Constructor
	 * @param reportType - the requested report type
	 * @param syncType - the requested synchronization type
	 */
	public SyncOptions(final ReportType reportType, final SyncType syncType) {
		super(SNum.SYNC_OPTS, (byte)1);
		if (reportType == null) throw new IllegalArgumentException("Report type must not be null");
		if (syncType == null) throw new IllegalArgumentException("Synchronization type must not be null");
		this.reportType = reportType;
		this.syncType = syncType;
	}

	/**
	 * @return the syncType
	 */
	public SyncType getSyncType() {
		return syncType;
	}

	/**
	 * @return the reportType
	 */
	public ReportType getReportType() {
		return reportType;
	}

	@Override
	protected byte[] getBytes() {
		final byte[] rptTypeBytes = COPSMsgParser.shortToBytes(reportType.getValue());
		final byte[] syncTypeBytes = COPSMsgParser.shortToBytes(syncType.getValue());
		final byte[] data = new byte[rptTypeBytes.length + syncTypeBytes.length];
		System.arraycopy(rptTypeBytes, 0, data, 0, rptTypeBytes.length);
		System.arraycopy(syncTypeBytes, 0, data, rptTypeBytes.length, syncTypeBytes.length);
		return data;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SyncOptions)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final SyncOptions that = (SyncOptions) o;
		return reportType == that.reportType && syncType == that.syncType;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + reportType.hashCode();
		result = 31 * result + syncType.hashCode();
		return result;
	}

	/**
	 * Returns a SyncOptions object from a byte array
	 * @param data - the data to parse
	 * @return - the object
	 * TODO - make me more robust as RuntimeExceptions can be thrown here.
	 */
	public static SyncOptions parse(final byte[] data) {
		return new SyncOptions(ReportType.valueOf(COPSMsgParser.bytesToShort(data[0], data[1])),
				SyncType.valueOf(COPSMsgParser.bytesToShort(data[2], data[3])));
	}

	/**
	 * The supported Report types
	 */
	public enum ReportType {

		STANDARD_REPORT_DATA((short) 0), COMPLETE_GATE_DATA((short) 1);

		ReportType(short value) {
			this.value = value;
		}

		public short getValue() {
			return value;
		}

		public static ReportType valueOf(short v) {
			switch (v) {
				case 0:
					return ReportType.STANDARD_REPORT_DATA;
				case 1:
					return ReportType.COMPLETE_GATE_DATA;
				default:
					throw new IllegalArgumentException("not supported value");
			}
		}

		private short value;

	}

	/**
	 * The supported Synchronization types
	 */
	public enum SyncType {

		FULL_SYNCHRONIZATION((short) 0), INCREMENTAL_SYNCHRONIZATION((short) 1);

		SyncType(short value) {
			this.value = value;
		}

		public short getValue() {
			return value;
		}

		public static SyncType valueOf(short v) {
			switch (v) {
				case 0:
					return SyncType.FULL_SYNCHRONIZATION;
				case 1:
					return SyncType.INCREMENTAL_SYNCHRONIZATION;
				default:
					throw new IllegalArgumentException("not supported value");
			}
		}

		private short value;

	}

}

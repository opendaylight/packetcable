/**
 @header@
 */
package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 *
 */
public interface IPCMMError extends IPCMMBaseObject {
	static final short LENGTH = 8;
	static final byte SNUM = 14;
	static final byte STYPE = 1;
	final String[] errors = { "Insufficient Resources", "Unknown GateID",
			"Missing Required Object", "Invalid Object",
			"Volume Based Usage Limit Exceeded",
			"Time Based Usage Limit Exceeded", "Session Class Limit Exceeded",
			"Undefined Service Class Name", "Incompatible Envelope",
			"Invalid SubscriberID", "Unauthorized AMID",
			"Number of Classifiers Not Supported", "Policy Exception",
			"Invalid Field Value in Object", "Transport Error",
			"Unknown Gate Command", "DOCSIS 1.0 CM",
			"Number of SIDs exceeded in CM", "Number of SIDs exceeded in CMTS",
			"Unauthorized PSID", "No State for PDP", "Unsupported Synch Type",
			"State Data Incomplete", "Upstream Drop Unsupported",
			"Multicast Gate Error", "Multicast Volume Limit Unsupported",
			"Uncommitted Multicast Not Supported",
			"Multicast Gate Modification Not Supported",
			"Upstream Multicast Not Supported",
			"Multicast GateSpec incompatibility", "Multicast QoS Error",
			"Multicast Downstream Resequencing mismatch",
			"Other, Unspecified Error" };

	static enum Description {
		ERROR_01((short) 1, errors[0]), ERROR_02((short) 2, errors[1]), ERROR_06(
				(short) 6, errors[2]), ERROR_07((short) 7, errors[3]), ERROR_08(
				(short) 8, errors[4]), ERROR_09((short) 9, errors[5]), ERROR_10(
				(short) 10, errors[6]), ERROR_11((short) 11, errors[7]), ERROR_12(
				(short) 12, errors[8]), ERROR_13((short) 13, errors[9]), ERROR_14(
				(short) 14, errors[10]), ERROR_15((short) 15, errors[11]), ERROR_16(
				(short) 16, errors[12]), ERROR_17((short) 17, errors[13]), ERROR_18(
				(short) 18, errors[14]), ERROR_19((short) 19, errors[15]), ERROR_20(
				(short) 20, errors[16]), ERROR_21((short) 21, errors[17]), ERROR_22(
				(short) 22, errors[18]), ERROR_23((short) 23, errors[19]), ERROR_24(
				(short) 24, errors[20]), ERROR_25((short) 25, errors[21]), ERROR_26(
				(short) 26, errors[22]), ERROR_27((short) 27, errors[23]), ERROR_28(
				(short) 28, errors[24]), ERROR_29((short) 29, errors[25]), ERROR_30(
				(short) 30, errors[26]), ERROR_31((short) 31, errors[27]), ERROR_32(
				(short) 32, errors[28]), ERROR_33((short) 33, errors[29]), ERROR_34(
				(short) 34, errors[30]), ERROR_35((short) 35, errors[31]), ERROR_127(
				(short) 127, errors[28]);

		private final short code;
		private final String description;

		private Description(short code, String description) {
			this.code = code;
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public short getCode() {
			return code;
		}

		public static String valueOf(short errCode) {
			switch (errCode) {
			case 1:
			case 2:
				return errors[errCode - 1];
			case 127:
				return errors[32];
			default:
				if (errCode > 35 || errCode < 1)
					throw new IllegalArgumentException("unrecongnized error code : " + errCode);
				return errors[errCode - 4];
			}
		}
	}

	void setErrorCode(short ErrorCode);

	short getErrorCode();

	void setErrorSubcode(short ErrorSubcode);

	short getErrorSubcode();

	String getDescription();
}

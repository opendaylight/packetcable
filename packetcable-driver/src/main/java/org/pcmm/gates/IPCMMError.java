/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 * The aPacketCable Error object contains information on the type of error that has occurred. The error is generated in
 * response to a Gate Control command and is contained in Gate-Set-Err, Gate-Info-Err and Gate-Delete-Err messages.
 */
public interface IPCMMError extends IPCMMBaseObject {

	byte STYPE = 1;

	/**
	 * Returns the error code
	 * @return - not null or NA
	 */
	ErrorCode getErrorCode();

	/**
	 * Returns the error sub-code
	 * @return - the sub error code
	 */
	short getErrorSubcode();

	/**
	 * Returns the error's description
	 * @return - not null
	 */
	String getDescription();

	/**
	 * The supported PCMM error codes with description.
	 */
	enum ErrorCode {
		NA((short) 0, "NA"),
		INSUFF_RES((short) 1, "Insufficient Resources"),
		UNK_GATE_ID((short) 2, "Unknown GateID"),
		MISSING_REQ_OBJ((short) 6, "Missing Required Object"),
		INVALID_OBJ((short) 7, "Invalid Object"),
		VOL_USG_LMT((short) 8, "Volume Based Usage Limit Exceeded"),
		TIME_USG_LMT((short) 9, "Time Based Usage Limit Exceeded"),
		SESSN_CLASS_LMT((short) 10, "Session Class Limit Exceeded"),
		UNDEF_SCN_NAME((short) 11, "Undefined Service Class Name"),
		INCOMPAT_ENV((short) 12, "Incompatible Envelope"),
		INVALID_SUB_ID((short) 13, "Invalid SubscriberID"),
		UNAUTH_AMID((short) 14, "Unauthorized AMID"),
		NUM_CLASSIFIERS((short) 15, "Number of Classifiers Not Supported"),
		POLICY_EXCEPTION((short) 16, "Policy Exception"),
		INVALID_FIELD((short) 17, "Invalid Field Value in Object"),
		TRANSPORT_ERROR((short) 18, "Transport Error"),
		UNK_GATE_CMD((short) 19, "Unknown Gate Command"),
		DOCSIS_1_CM((short) 20, "DOCSIS 1.0 CM"),
		NUM_CM_SID((short) 21, "Number of SIDs exceeded in CM"),
		NUM_CMTS_SID((short) 22, "Number of SIDs exceeded in CMTS"),
		UNAUTH_PSID((short) 23, "Unauthorized PSID"),
		NO_STATE_PDP((short) 24, "No State for PDP"),
		UNSUP_SYNC_TYPE((short) 25, "Unsupported Synch Type"),
		STATE_INCMPL((short) 26, "State Data Incomplete"),
		UP_DROP_UNSUPPORT((short) 27, "Upstream Drop Unsupported"),
		MULTI_GATE_ERR((short) 28, "Multicast Gate Error"),
		MULTI_VOL_LIMIT((short) 29, "Multicast Volume Limit Unsupported"),
		MULTI_UNCOMMITTED((short) 30, "Uncommitted Multicast Not Supported"),
		MULTI_GATE_MOD((short) 31, "Multicast Gate Modification Not Supported"),
		MULTI_UP_ERR((short) 32, "Upstream Multicast Not Supported"),
		MULTI_GATE_INCOMPAT((short) 33, "Multicast GateSpec incompatibility"),
		MULTI_QOS_ERR((short) 34, "Multicast QoS Error"),
		MULTI_DN_RESEQ((short) 35, "Multicast Downstream Resequencing mismatch"),
		OTHER_UNSPECIFIED((short) 127, "Other, Unspecified Error");

		private final short code;
		private final String description;

		ErrorCode(short code, String description) {
			this.code = code;
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public short getCode() {
			return code;
		}

		public static ErrorCode valueOf(final short index) {
			switch (index) {
				case 0:
					return NA;
				case 1:
					return INSUFF_RES;
				case 2:
					return UNK_GATE_ID;
				case 6:
					return MISSING_REQ_OBJ;
				case 7:
					return INVALID_OBJ;
				case 8:
					return VOL_USG_LMT;
				case 9:
					return TIME_USG_LMT;
				case 10:
					return SESSN_CLASS_LMT;
				case 11:
					return UNDEF_SCN_NAME;
				case 12:
					return INCOMPAT_ENV;
				case 13:
					return INVALID_SUB_ID;
				case 14:
					return UNAUTH_AMID;
				case 15:
					return NUM_CLASSIFIERS;
				case 16:
					return POLICY_EXCEPTION;
				case 17:
					return INVALID_FIELD;
				case 18:
					return TRANSPORT_ERROR;
				case 19:
					return UNK_GATE_CMD;
				case 20:
					return DOCSIS_1_CM;
				case 21:
					return NUM_CM_SID;
				case 22:
					return NUM_CMTS_SID;
				case 23:
					return UNAUTH_PSID;
				case 24:
					return NO_STATE_PDP;
				case 25:
					return UNSUP_SYNC_TYPE;
				case 26:
					return STATE_INCMPL;
				case 27:
					return UP_DROP_UNSUPPORT;
				case 28:
					return MULTI_GATE_ERR;
				case 29:
					return MULTI_VOL_LIMIT;
				case 30:
					return MULTI_UNCOMMITTED;
				case 31:
					return MULTI_GATE_MOD;
				case 32:
					return MULTI_UP_ERR;
				case 33:
					return MULTI_GATE_INCOMPAT;
				case 34:
					return MULTI_QOS_ERR;
				case 35:
					return MULTI_DN_RESEQ;
				case 127:
					return OTHER_UNSPECIFIED;
				default:
					return null;
			}
		}
	}

}

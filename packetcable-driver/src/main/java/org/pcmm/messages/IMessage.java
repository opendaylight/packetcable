/**
 @header@
 */

package org.pcmm.messages;

/**
 * This defines the messages exchanged between client and server.
 * 
 * 
 * <pre>
 * 1 = Request                 (REQ)
 * 2 = Decision                (DEC)
 * 3 = Report State            (RPT)
 * 4 = Delete Request State    (DRQ)
 * 5 = Synchronize State Req   (SSQ)
 * 6 = Client-Open             (OPN)
 * 7 = Client-Accept           (CAT)
 * 8 = Client-Close            (CC)
 * 9 = Keep-Alive              (KA)
 * 10= Synchronize Complete    (SSC)
 * </pre>
 * 
 */
public interface IMessage {

	public static enum MessageProperties {
		CLIENT_TYPE("Client-Type"), PEP_ID("Pep-ID"), KA_TIMER("KA-Timer"), ACCEPT_TIMER(
				"Accept-Timer"), ERR_MESSAGE("Error-Message"), ERR_MESSAGE_SUB_CODE(
				"Error-Message-Code"), MM_MAJOR_VERSION_INFO(
				"MM-Major-Version-info"), MM_MINOR_VERSION_INFO(
				"MM-Minor-Version-info"), R_TYPE("R-Type"), M_TYPE("M-Type"), CLIENT_HANDLE(
				"Client-Handle"), GATE_CONTROL("Gate-Control"), DECISION_CMD_CODE(
				"Decision-Type"), DECISION_FLAG("Decision-Flag");

		private MessageProperties(String valueString) {
			this.value = valueString;
		}

		private String value;

		public String getValue() {
			return value;
		}
	}

}

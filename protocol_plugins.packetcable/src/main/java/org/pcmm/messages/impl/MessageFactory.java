/**
 * @header@
 */
package org.pcmm.messages.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.pcmm.messages.IMessage.MessageProperties;
import org.pcmm.messages.IMessageFactory;
import org.pcmm.objects.MMVersionInfo;
import org.pcmm.rcd.ICMTS;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.COPSAcctTimer;
import org.umu.cops.stack.COPSClientAcceptMsg;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSClientOpenMsg;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSContext;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSDecisionMsg;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHandle;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSKAMsg;
import org.umu.cops.stack.COPSKATimer;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSObjHeader;
import org.umu.cops.stack.COPSPepId;
import org.umu.cops.stack.COPSReqMsg;

/**
 * 
 * 
 */
public class MessageFactory implements IMessageFactory {

	/** Default keep-alive timer value (secs) */
	public static final short KA_TIMER_VALUE = 30;
	/** Default accounting timer value (secs) */
	public static final short ACCT_TIMER_VALUE = 0;

	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	private static MessageFactory instance;

	private MessageFactory() {
	}

	public static MessageFactory getInstance() {
		if (instance == null)
			instance = new MessageFactory();
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcmm.messages.IMessageFactory#create(pcmm.messages.MessageType)
	 */
	public COPSMsg create(byte messageType) {
		return create(messageType, new Properties());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pcmm.messages.IMessageFactory#create(org.pcmm.messages.IMessage.
	 * MessageType, java.util.Properties)
	 */
	public COPSMsg create(byte messageType, Properties properties) {
		// return new PCMMMessage(messageType, content);
		switch (messageType) {
		case COPSHeader.COPS_OP_OPN:
			return createOPNMessage(properties);
		case COPSHeader.COPS_OP_REQ:
			return createREQMessage(properties);
		case COPSHeader.COPS_OP_CAT:
			return createCATMessage(properties);
		case COPSHeader.COPS_OP_CC:
			return createCCMessage(properties);
		case COPSHeader.COPS_OP_DEC:
			return createDECMessage(properties);
		case COPSHeader.COPS_OP_DRQ:
		break;
		case COPSHeader.COPS_OP_KA:
			return createKAMessage(properties);
		case COPSHeader.COPS_OP_RPT:
		break;
		case COPSHeader.COPS_OP_SSC:
		break;
		case COPSHeader.COPS_OP_SSQ:
		break;
		}
		return null;
	}

	/**
	 * 
	 * @param prop
	 * @return
	 */
	protected COPSMsg createDECMessage(Properties prop) {
		COPSDecisionMsg msg = new COPSDecisionMsg();
		// ===common part between all gate control messages
		COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, IPCMMClient.CLIENT_TYPE);
		// handle
		COPSHandle handle = new COPSHandle();
		// context
		COPSContext context = new COPSContext(COPSContext.CONFIG, (short) 0);
		// decision
		COPSDecision decision = new COPSDecision();
		if (prop.get(MessageProperties.DECISION_CMD_CODE) != null)
			decision.setCmdCode((byte) prop.get(MessageProperties.DECISION_CMD_CODE));
		if (prop.get(MessageProperties.DECISION_FLAG) != null)
			decision.setFlags((short) prop.get(MessageProperties.DECISION_FLAG));
		COPSClientSI si = new COPSClientSI(COPSObjHeader.COPS_DEC, (byte) 4);
		if (prop.get(MessageProperties.GATE_CONTROL) != null)
			si.setData((COPSData) prop.get(MessageProperties.GATE_CONTROL));
		try {
			msg.add(hdr);
			if (prop.get(MessageProperties.CLIENT_HANDLE) != null)
				handle.setId(new COPSData((String) prop.get(MessageProperties.CLIENT_HANDLE)));
			msg.add(handle);
			msg.addDecision(decision, context);
			msg.add(si);
			// try {
			// msg.dump(System.out);
			// } catch (IOException unae) {
			// }

		} catch (COPSException e) {
			logger.error(e.getMessage());
		}

		return msg;
	}

	/**
	 * creates a Client-Open message.
	 * 
	 * @param prop
	 *            properties
	 * @return COPS message
	 */
	protected COPSMsg createOPNMessage(Properties prop) {
		COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_OPN, IPCMMClient.CLIENT_TYPE);
		COPSPepId pepId = new COPSPepId();
		// version infor object
		short majorVersion = MMVersionInfo.DEFAULT_MAJOR_VERSION_INFO;
		short minorVersion = MMVersionInfo.DEFAULT_MINOR_VERSION_INFO;
		if (prop.get(MessageProperties.MM_MAJOR_VERSION_INFO) != null)
			majorVersion = (Short) prop.get(MessageProperties.MM_MAJOR_VERSION_INFO);
		if (prop.get(MessageProperties.MM_MINOR_VERSION_INFO) != null)
			minorVersion = (Short) prop.get(MessageProperties.MM_MINOR_VERSION_INFO);
		// Mandatory MM version.
		COPSClientSI clientSI = new COPSClientSI((byte) 1);
		byte[] versionInfo = new MMVersionInfo(majorVersion, minorVersion).getAsBinaryArray();
		clientSI.setData(new COPSData(versionInfo, 0, versionInfo.length));
		COPSClientOpenMsg msg = new COPSClientOpenMsg();
		try {
			COPSData d = null;
			if (prop.get(MessageProperties.PEP_ID) != null)
				d = new COPSData((String) prop.get(MessageProperties.PEP_ID));
			else
				d = new COPSData(InetAddress.getLocalHost().getHostName());
			pepId.setData(d);
			msg.add(hdr);
			msg.add(pepId);
			msg.add(clientSI);
		} catch (COPSException e) {
			logger.error(e.getMessage());
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		}
		return msg;
	}

	/**
	 * creates a Client-Accept message.
	 * 
	 * @param prop
	 *            properties
	 * @return COPS message
	 */
	protected COPSMsg createCATMessage(Properties prop) {
		COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_CAT, IPCMMClient.CLIENT_TYPE);
		COPSKATimer katimer = null;
		COPSAcctTimer acctTimer = null;
		if (prop.get(MessageProperties.KA_TIMER) != null)
			katimer = new COPSKATimer((short) prop.get(MessageProperties.KA_TIMER));
		else
			katimer = new COPSKATimer((short) KA_TIMER_VALUE);
		if (prop.get(MessageProperties.ACCEPT_TIMER) != null)
			acctTimer = new COPSAcctTimer((short) prop.get(MessageProperties.ACCEPT_TIMER));
		else
			acctTimer = new COPSAcctTimer(ACCT_TIMER_VALUE);
		COPSClientAcceptMsg acceptMsg = new COPSClientAcceptMsg();
		try {
			acceptMsg.add(hdr);
			acceptMsg.add(katimer);
			if (acctTimer.getTimerVal() != 0)
				acceptMsg.add(acctTimer);
		} catch (COPSException e) {
			logger.error(e.getMessage());
		}
		return acceptMsg;
	}

	/**
	 * creates a Client-Close message.
	 * 
	 * @param prop
	 *            properties
	 * @return COPS message
	 */
	protected COPSMsg createCCMessage(Properties prop) {
		COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_CC, IPCMMClient.CLIENT_TYPE);
		COPSError err = null;
		if (prop.get(MessageProperties.ERR_MESSAGE) != null) {
			short code = (short) 0;
			short error = (short) prop.get(MessageProperties.ERR_MESSAGE);
			if (prop.get(MessageProperties.ERR_MESSAGE_SUB_CODE) != null)
				code = (short) prop.get(MessageProperties.ERR_MESSAGE_SUB_CODE);
			err = new COPSError(error, code);
		} else
			err = new COPSError(COPSError.COPS_ERR_UNKNOWN, (short) 0);
		COPSClientCloseMsg closeMsg = new COPSClientCloseMsg();
		try {
			closeMsg.add(cHdr);
			closeMsg.add(err);
		} catch (COPSException e) {
			logger.error(e.getMessage());
		}
		return closeMsg;
	}

	/**
	 * creates a Request message
	 * 
	 * @param prop
	 *            properties
	 * @return Request message
	 */
	protected COPSMsg createREQMessage(Properties prop) {
		COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_REQ, IPCMMClient.CLIENT_TYPE);
		COPSReqMsg req = new COPSReqMsg();
		short rType = ICMTS.DEFAULT_R_TYPE;
		short mType = ICMTS.DEFAULT_M_TYPE;
		if (prop.get(MessageProperties.R_TYPE) != null)
			rType = (Short) prop.get(MessageProperties.R_TYPE);
		if (prop.get(MessageProperties.M_TYPE) != null)
			mType = (Short) prop.get(MessageProperties.M_TYPE);
		COPSContext copsContext = new COPSContext(rType, mType);
		COPSHandle copsHandle = new COPSHandle();
		if (prop.get(MessageProperties.CLIENT_HANDLE) != null)
			copsHandle.setId(new COPSData((String) prop.get(MessageProperties.CLIENT_HANDLE)));
		else
			// just a random handle
			copsHandle.setId(new COPSData("" + Math.random() * 82730));
		try {
			req.add(cHdr);
			req.add(copsContext);
			req.add(copsHandle);
		} catch (COPSException e) {
			logger.error(e.getMessage());
		}
		return req;
	}

	/**
	 * creates a Keep-Alive message.
	 * 
	 * @param prop
	 *            properties
	 * @return COPS message
	 */
	protected COPSMsg createKAMessage(Properties prop) {
		COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_KA, (short) 0);
		COPSKAMsg kaMsg = new COPSKAMsg();
		COPSKATimer timer = null;
		if (prop.get(MessageProperties.KA_TIMER) != null)
			timer = new COPSKATimer((Short) prop.get(MessageProperties.KA_TIMER));
		else
			timer = new COPSKATimer();
		try {
			kaMsg.add(cHdr);
		} catch (COPSException e) {
			logger.error(e.getMessage());
		}
		return kaMsg;
	}
}

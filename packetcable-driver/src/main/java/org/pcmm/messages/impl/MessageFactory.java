/**
 * @header@
 */
package org.pcmm.messages.impl;

import org.pcmm.messages.IMessage.MessageProperties;
import org.pcmm.messages.IMessageFactory;
import org.pcmm.objects.MMVersionInfo;
import org.pcmm.rcd.ICMTS;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.stack.*;

import java.net.InetAddress;
import java.util.Properties;

/**
 *
 *
 */
public class MessageFactory implements IMessageFactory {

    /** Default keep-alive timer value (secs) */
    public static final short KA_TIMER_VALUE = 30;
    /** Default accounting timer value (secs) */
    public static final short ACCT_TIMER_VALUE = 0;

    private static final Logger logger = LoggerFactory.getLogger(MessageFactory.class);

    private static final MessageFactory instance = new MessageFactory();

    private MessageFactory() {
    }

    public static MessageFactory getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     *
     * @see pcmm.messages.IMessageFactory#create(pcmm.messages.MessageType)
     */
    public COPSMsg create(final byte messageType) {
        return create(messageType, new Properties());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.messages.IMessageFactory#create(org.pcmm.messages.IMessage.
     * MessageType, java.util.Properties)
     */
    public COPSMsg create(final byte messageType, final Properties properties) {
        logger.info("Creating message of type - " + messageType);
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
     * @param prop - the properties
     * @return - the message
     */
    protected COPSMsg createDECMessage(final Properties prop) {
        final COPSDecisionMsg msg = new COPSDecisionMsg();
        // ===common part between all gate control messages
        final COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_DEC, IPCMMClient.CLIENT_TYPE);
        // handle
        // context
        final COPSContext context = new COPSContext(COPSContext.CONFIG, (short) 0);
        // decision
        final COPSDecision decision = new COPSDecision();
        if (prop.get(MessageProperties.DECISION_CMD_CODE) != null)
            decision.setCmdCode((byte) prop.get(MessageProperties.DECISION_CMD_CODE));
        if (prop.get(MessageProperties.DECISION_FLAG) != null)
            decision.setFlags((short) prop.get(MessageProperties.DECISION_FLAG));

        final COPSClientSI si = new COPSClientSI(COPSObjHeader.COPS_DEC, (byte) 4);
        if (prop.get(MessageProperties.GATE_CONTROL) != null)
            si.setData((COPSData) prop.get(MessageProperties.GATE_CONTROL));
        try {
            msg.add(hdr);
            final COPSHandle handle;
            if (prop.get(MessageProperties.CLIENT_HANDLE) != null) {
                handle = new COPSHandle(new COPSData((String) prop.get(MessageProperties.CLIENT_HANDLE)));
            }
            else {
                // TODO - This smells wrong to have a null handle ID
                handle = new COPSHandle(null);
            }
            msg.add(handle);
            msg.addDecision(decision, context);
            msg.add(si);

            // TODO - determine why this block has been commented out
            // try {
            // msg.dump(System.out);
            // } catch (IOException unae) {
            // }

        } catch (final COPSException e) {
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
    protected COPSMsg createOPNMessage(final Properties prop) {
        final COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_OPN, IPCMMClient.CLIENT_TYPE);
        final COPSPepId pepId = new COPSPepId();
        // version infor object
        short majorVersion = MMVersionInfo.DEFAULT_MAJOR_VERSION_INFO;
        short minorVersion = MMVersionInfo.DEFAULT_MINOR_VERSION_INFO;
        if (prop.get(MessageProperties.MM_MAJOR_VERSION_INFO) != null)
            majorVersion = (Short) prop.get(MessageProperties.MM_MAJOR_VERSION_INFO);
        if (prop.get(MessageProperties.MM_MINOR_VERSION_INFO) != null)
            minorVersion = (Short) prop.get(MessageProperties.MM_MINOR_VERSION_INFO);
        // Mandatory MM version.
        final COPSClientSI clientSI = new COPSClientSI((byte) 1);
        byte[] versionInfo = new MMVersionInfo(majorVersion, minorVersion).getAsBinaryArray();
        clientSI.setData(new COPSData(versionInfo, 0, versionInfo.length));
        final COPSClientOpenMsg msg = new COPSClientOpenMsg();
        try {
            final COPSData d;
            if (prop.get(MessageProperties.PEP_ID) != null)
                d = new COPSData((String) prop.get(MessageProperties.PEP_ID));
            else
                d = new COPSData(InetAddress.getLocalHost().getHostName());
            pepId.setData(d);
            msg.add(hdr);
            msg.add(pepId);
            msg.add(clientSI);
        } catch (Exception e) {
            logger.error("Error creating OPN message", e);
        }
        return msg;
    }

    /**
     * creates a Client-Accept message.
     * @param prop - properties
     * @return COPS message
     */
    protected COPSMsg createCATMessage(final Properties prop) {
        final COPSHeader hdr = new COPSHeader(COPSHeader.COPS_OP_CAT, IPCMMClient.CLIENT_TYPE);
        final COPSKATimer katimer;
        if (prop.get(MessageProperties.KA_TIMER) != null)
            katimer = new COPSKATimer((short) prop.get(MessageProperties.KA_TIMER));
        else
            katimer = new COPSKATimer(KA_TIMER_VALUE);

        final COPSAcctTimer acctTimer;
        if (prop.get(MessageProperties.ACCEPT_TIMER) != null)
            acctTimer = new COPSAcctTimer((short) prop.get(MessageProperties.ACCEPT_TIMER));
        else
            acctTimer = new COPSAcctTimer(ACCT_TIMER_VALUE);

        final COPSClientAcceptMsg acceptMsg = new COPSClientAcceptMsg();
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
    protected COPSMsg createCCMessage(final Properties prop) {
        final COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_CC, IPCMMClient.CLIENT_TYPE);
        final COPSError err;
        if (prop.get(MessageProperties.ERR_MESSAGE) != null) {
            short code = (short) 0;
            final short error = (short) prop.get(MessageProperties.ERR_MESSAGE);
            if (prop.get(MessageProperties.ERR_MESSAGE_SUB_CODE) != null)
                code = (short) prop.get(MessageProperties.ERR_MESSAGE_SUB_CODE);
            err = new COPSError(error, code);
        } else
            err = new COPSError(COPSError.COPS_ERR_UNKNOWN, (short) 0);

        final COPSClientCloseMsg closeMsg = new COPSClientCloseMsg();
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
    protected COPSMsg createREQMessage(final Properties prop) {
        final COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_REQ, IPCMMClient.CLIENT_TYPE);
        final COPSReqMsg req = new COPSReqMsg();

        final short rType;
        if (prop.get(MessageProperties.R_TYPE) != null)
            rType = (Short) prop.get(MessageProperties.R_TYPE);
        else rType = ICMTS.DEFAULT_R_TYPE;

        final short mType;
        if (prop.get(MessageProperties.M_TYPE) != null)
            mType = (Short) prop.get(MessageProperties.M_TYPE);
        else mType = ICMTS.DEFAULT_M_TYPE;

        final COPSContext copsContext = new COPSContext(rType, mType);
        final COPSHandle copsHandle;
        if (prop.get(MessageProperties.CLIENT_HANDLE) != null)
            copsHandle = new COPSHandle(new COPSData((String) prop.get(MessageProperties.CLIENT_HANDLE)));
        else
            // just a random handle
            copsHandle = new COPSHandle(new COPSData("" + Math.random() * 82730));
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
     * TODO - Why is there a timer being instantiated but never used?
     */
    protected COPSMsg createKAMessage(final Properties prop) {
        final COPSHeader cHdr = new COPSHeader(COPSHeader.COPS_OP_KA, (short) 0);
        final COPSKAMsg kaMsg = new COPSKAMsg();
        final COPSKATimer timer;
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

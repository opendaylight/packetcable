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
import org.umu.cops.stack.COPSClientSI.CSIType;
import org.umu.cops.stack.COPSContext.RType;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSError.ErrorTypes;
import org.umu.cops.stack.COPSHeader.ClientType;
import org.umu.cops.stack.COPSHeader.OPCode;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.net.InetAddress;
import java.util.*;

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
    public COPSMsg create(final OPCode messageType) {
        return create(messageType, new Properties());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.messages.IMessageFactory#create(org.pcmm.messages.IMessage.
     * MessageType, java.util.Properties)
     */
    public COPSMsg create(final OPCode messageType, final Properties properties) {
        logger.info("Creating message of type - " + messageType);
        // return new PCMMMessage(messageType, content);
        switch (messageType) {
            case OPN:
                return createOPNMessage(properties);
            case REQ:
                return createREQMessage(properties);
            case CAT:
                return createCATMessage(properties);
            case CC:
                return createCCMessage(properties);
            case DEC:
                return createDECMessage(properties);
            case DRQ:
                break;
            case KA:
                return createKAMessage(properties);
            case RPT:
                break;
            case SSC:
                break;
            case SSQ:
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

        // ===common part between all gate control messages
        //        final COPSHeader hdr = new COPSHeader(OPCode.DEC, IPCMMClient.CLIENT_TYPE);
        // handle
        // context
        // decision
        // TODO - the old command and flag codes are not congruent with the ones described in COPSDecision
        // TODO - what is the correct client type to be using here???
/*
        if (prop.get(MessageProperties.DECISION_CMD_CODE) != null)
            decision.setCmdCode((byte) prop.get(MessageProperties.DECISION_CMD_CODE));
        if (prop.get(MessageProperties.DECISION_FLAG) != null)
            decision.setFlags((short) prop.get(MessageProperties.DECISION_FLAG));
*/

        final COPSData data;
        if (prop.get(MessageProperties.GATE_CONTROL) != null)
            data = (COPSData) prop.get(MessageProperties.GATE_CONTROL);
        else
            data = null;

        // TODO - Need to determine is SIGNALED is the correct default CSIType
        // Decided that CSI object is not what should be encapsulated by the COPSDecisionMsg, therefore placing
        // data into the COPSDecision object located in the decisionMap
        //        final COPSClientSI si = new COPSClientSI(CSIType.SIGNALED, data);

        final COPSHandle handle;
        if (prop.get(MessageProperties.CLIENT_HANDLE) != null) {
            handle = new COPSHandle(new COPSData((String) prop.get(MessageProperties.CLIENT_HANDLE)));
        }
        else {
            // TODO - This smells wrong to have a null handle ID
            handle = new COPSHandle(null);
        }
        final Set<COPSDecision> decisionSet = new HashSet<>();
        decisionSet.add(new COPSDecision(CType.DEF, Command.NULL, DecisionFlag.NA, data));
        final Map<COPSContext, Set<COPSDecision>> decisionMap = new HashMap<>();
        decisionMap.put(new COPSContext(RType.CONFIG, (short)0), decisionSet);

        final COPSDecisionMsg msg = new COPSDecisionMsg(ClientType.TYPE_1, handle, decisionMap, null);

        // TODO - determine why this block has been commented out
        // try {
        // msg.dump(System.out);
        // } catch (IOException unae) {
        // }

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
        // version infor object
        short majorVersion = MMVersionInfo.DEFAULT_MAJOR_VERSION_INFO;
        short minorVersion = MMVersionInfo.DEFAULT_MINOR_VERSION_INFO;
        if (prop.get(MessageProperties.MM_MAJOR_VERSION_INFO) != null)
            majorVersion = (Short) prop.get(MessageProperties.MM_MAJOR_VERSION_INFO);
        if (prop.get(MessageProperties.MM_MINOR_VERSION_INFO) != null)
            minorVersion = (Short) prop.get(MessageProperties.MM_MINOR_VERSION_INFO);
        // Mandatory MM version.
        byte[] versionInfo = new MMVersionInfo(majorVersion, minorVersion).getAsBinaryArray();
        final COPSClientSI clientSI = new COPSClientSI(CSIType.SIGNALED, new COPSData(versionInfo, 0, versionInfo.length));
        try {
            final COPSData d;
            if (prop.get(MessageProperties.PEP_ID) != null)
                d = new COPSData((String) prop.get(MessageProperties.PEP_ID));
            else
                d = new COPSData(InetAddress.getLocalHost().getHostName());
            final COPSPepId pepId = new COPSPepId(d);
            return new COPSClientOpenMsg(ClientType.NA, pepId, clientSI, null, null);
        } catch (Exception e) {
            logger.error("Error creating OPN message", e);
        }

        // TODO - this probably should not return null and throw an exception instead
        return null;
    }

    /**
     * creates a Client-Accept message.
     * @param prop - properties
     * @return COPS message
     */
    protected COPSMsg createCATMessage(final Properties prop) {
        // TODO - determine what the first constructor parameter really should be???
        final COPSKATimer katimer;
        if (prop.get(MessageProperties.KA_TIMER) != null)
            katimer = new COPSKATimer((short)prop.get(MessageProperties.KA_TIMER));
        else
            katimer = new COPSKATimer(KA_TIMER_VALUE);

        // TODO - determine what the first constructor parameter really should be???
        final COPSAcctTimer acctTimer;
        if (prop.get(MessageProperties.ACCEPT_TIMER) != null)
            acctTimer = new COPSAcctTimer((short) prop.get(MessageProperties.ACCEPT_TIMER));
        else
            acctTimer = new COPSAcctTimer(ACCT_TIMER_VALUE);

        if (acctTimer.getTimerVal() != 0)
            return new COPSClientAcceptMsg(IPCMMClient.CLIENT_TYPE, katimer, acctTimer, null);
        else return new COPSClientAcceptMsg(IPCMMClient.CLIENT_TYPE, katimer, null, null);
    }

    /**
     * creates a Client-Close message.
     *
     * @param prop
     *            properties
     * @return COPS message
     */
    protected COPSMsg createCCMessage(final Properties prop) {
        final COPSError err;
        if (prop.get(MessageProperties.ERR_MESSAGE) != null) {
            ErrorTypes code = ErrorTypes.NA;
            final ErrorTypes error = (ErrorTypes) prop.get(MessageProperties.ERR_MESSAGE);
            if (prop.get(MessageProperties.ERR_MESSAGE_SUB_CODE) != null)
                code = (ErrorTypes) prop.get(MessageProperties.ERR_MESSAGE_SUB_CODE);
            err = new COPSError(COPSError.ERROR_CODE_TO_TYPE.get(error.ordinal()),
                    COPSError.ERROR_CODE_TO_TYPE.get(code.ordinal()));
        } else
            err = new COPSError(ErrorTypes.UNKNOWN, ErrorTypes.NA);

        return new COPSClientCloseMsg(IPCMMClient.CLIENT_TYPE, err, null, null);
    }

    /**
     * creates a Request message
     *
     * @param prop
     *            properties
     * @return Request message
     */
    protected COPSMsg createREQMessage(final Properties prop) {
        final short rType;
        if (prop.get(MessageProperties.R_TYPE) != null)
            rType = (Short) prop.get(MessageProperties.R_TYPE);
        else rType = ICMTS.DEFAULT_R_TYPE;

        final short mType;
        if (prop.get(MessageProperties.M_TYPE) != null)
            mType = (Short) prop.get(MessageProperties.M_TYPE);
        else mType = ICMTS.DEFAULT_M_TYPE;

        final COPSContext copsContext = new COPSContext(COPSContext.VAL_TO_RTYPE.get((int)rType), mType);
        final COPSHandle copsHandle;
        if (prop.get(MessageProperties.CLIENT_HANDLE) != null)
            copsHandle = new COPSHandle(new COPSData((String) prop.get(MessageProperties.CLIENT_HANDLE)));
        else
            // just a random handle
            copsHandle = new COPSHandle(new COPSData("" + Math.random() * 82730));

        return new COPSReqMsg(ClientType.TYPE_1, copsHandle, copsContext, null, null, null, null, null);
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
        // TODO - determine why this isn't really doing anything
        return new COPSKAMsg(ClientType.NA, null);
/*
        final COPSKATimer timer;
        if (prop.get(MessageProperties.KA_TIMER) != null)
            timer = new COPSKATimer((short)0, (Short) prop.get(MessageProperties.KA_TIMER));
        else
            timer = new COPSKATimer();
*/
    }
}

/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * COPS Decision (RFC 2748)
 *
 * Decision made by the PDP. Appears in replies. The specific non-
 * mandatory decision objects required in a decision to a particular
 * request depend on the type of client.
 *
 * C-Num = 6
 * C-Type = 1, Decision Flags (Mandatory)
 *
 * Commands:
 * 0 = NULL Decision (No configuration data available)
 * 1 = Install (Admit request/Install configuration)
 * 2 = Remove (Remove request/Remove configuration)
 *
 * Flags:
 * 0x01 = Trigger Error (Trigger error message if set)
 * Note: Trigger Error is applicable to client-types that
 * are capable of sending error notifications for signaled
 * messages.
 *
 * Flag values not applicable to a given context's R-Type or
 * client-type MUST be ignored by the PEP.
 *
 * C-Type = 2, Stateless Data
 *
 * This type of decision object carries additional stateless
 * information that can be applied by the PEP locally. It is a
 * variable length object and its internal format SHOULD be
 * specified in the relevant COPS extension document for the given
 * client-type. This object is optional in Decision messages and is
 * interpreted relative to a given context.
 *
 * It is expected that even outsourcing PEPs will be able to make
 * some simple stateless policy decisions locally in their LPDP. As
 * this set is well known and implemented ubiquitously, PDPs are
 * aware of it as well (either universally, through configuration,
 * or using the Client-Open message). The PDP may also include this
 * information in its decision, and the PEP MUST apply it to the
 * resource allocation event that generated the request.
 *
 * C-Type = 3, Replacement Data
 *
 * This type of decision object carries replacement data that is to
 * replace existing data in a signaled message. It is a variable
 * length object and its internal format SHOULD be specified in the
 * relevant COPS extension document for the given client-type. It is
 * optional in Decision messages and is interpreted relative to a
 * given context.
 *
 * C-Type = 4, Client Specific Decision Data
 *
 * Additional decision types can be introduced using the Client
 * Specific Decision Data Object. It is a variable length object and
 * its internal format SHOULD be specified in the relevant COPS
 * extension document for the given client-type. It is optional in
 * Decision messages and is interpreted relative to a given context.
 *
 * C-Type = 5, Named Decision Data
 *
 * Named configuration information is encapsulated in this version
 * of the decision object in response to configuration requests. It
 * is a variable length object and its internal format SHOULD be
 * specified in the relevant COPS extension document for the given
 * client-type. It is optional in Decision messages and is
 * interpreted relative to both a given context and decision flags.
 */
public class COPSDecision extends COPSObjBase {

    static Map<Integer, Command> VAL_TO_CMD = new ConcurrentHashMap<>();
    static {
        VAL_TO_CMD.put(Command.NULL.ordinal(), Command.NULL);
        VAL_TO_CMD.put(Command.INSTALL.ordinal(), Command.INSTALL);
        VAL_TO_CMD.put(Command.REMOVE.ordinal(), Command.REMOVE);
    }

    static Map<Integer, DecisionFlag> VAL_TO_FLAG = new ConcurrentHashMap<>();
    static {
        VAL_TO_FLAG.put(DecisionFlag.NA.ordinal(), DecisionFlag.NA);
        VAL_TO_FLAG.put(DecisionFlag.REQERROR.ordinal(), DecisionFlag.REQERROR);
        VAL_TO_FLAG.put(DecisionFlag.REQSTATE.ordinal(), DecisionFlag.REQSTATE);
    }

    /**
     * All CTypes are supported except NA
     */
    private final Command _cmdCode;
    private final DecisionFlag _flags;
    private final COPSData _data;
    private final COPSData _padding;

    /**
     * Constructor generally used for sending messages without any extra data
     * @param cmdCode - the command
     * @throws java.lang.IllegalArgumentException
     */
    public COPSDecision(final Command cmdCode) {
        this(CType.DEF, cmdCode, DecisionFlag.NA, new COPSData());
    }

    /**
     * Constructor generally used for sending messages with a specific CType and extra data and a NA decision flag
     * @param cType - the CType
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    public COPSDecision(final CType cType, final COPSData data) {
        this(cType, Command.NULL, DecisionFlag.NA, data);
    }

    /**
     * Constructor generally used for sending messages with a specific Command and DecisionFlag
     * @param cmdCode - the command
     * @param flags - the flags
     * @throws java.lang.IllegalArgumentException
     */
    public COPSDecision(final Command cmdCode, final DecisionFlag flags) {
        this(CType.DEF, cmdCode, flags, new COPSData());
    }

    /**
     * Constructor generally used for sending messages with a specific, CType, Command and DecisionFlag
     * @param cType - the CType
     * @param cmdCode - the command
     * @param flags - the flags
     * @throws java.lang.IllegalArgumentException
     */
    public COPSDecision(final CType cType, final Command cmdCode, final DecisionFlag flags) {
        this(cType, cmdCode, flags, new COPSData());
    }

    /**
     * Constructor generally used for sending messages with a specific, CType, Command, DecisionFlag and data
     * @param cType - the CType
     * @param cmdCode - the command
     * @param flags - the flags
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    public COPSDecision(final CType cType, final Command cmdCode, final DecisionFlag flags,
                        final COPSData data) {
        this(new COPSObjHeader(CNum.DEC, cType), cmdCode, flags, data);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param cmdCode - the command
     * @param flags - the flags
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSDecision(final COPSObjHeader hdr, final Command cmdCode, final DecisionFlag flags,
                           final COPSData data) {
        super(hdr);
        // TODO - find a better way to make this check
        if (this.getClass().getName().equals("org.umu.cops.stack.COPSDecision") && !hdr.getCNum().equals(CNum.DEC))
            throw new IllegalArgumentException("CNum must be equal to " + CNum.DEC);

        if (hdr.getCType().equals(CType.NA)) throw new IllegalArgumentException("CType must not be " + CType.NA);
        if (cmdCode == null) throw new IllegalArgumentException("Command code must not be null");
        if (flags == null) throw new IllegalArgumentException("Flags must not be null");
        if (data == null) throw new IllegalArgumentException("Data object must not be null");

        _cmdCode = cmdCode;
        _flags = flags;
        _data = data;

        if ((_data.length() % 4) != 0) {
            final int padLen = 4 - (_data.length() % 4);
            _padding = COPSObjectParser.getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
    }

    /**
     * Returns the command
     * @return - the command
     */
    public Command getCommand() { return _cmdCode; }

    @Override
    public int getDataLength() {
        return 4 + _data.length() + _padding.length();
    }

    /**
     * Get the associated data if decision object is of cType 2 or higher
     * @return   a COPSData
     */
    public COPSData getData() {
        return (_data);
    }

    /**
     * If cType == 1 , get the flags associated
     * @return   a short
     */
    public DecisionFlag getFlag() {
        return _flags;
    }

    /**
     * Method getTypeStr
     * @return   a String
     */
    public String getTypeStr() {
        switch (this.getHeader().getCType()) {
            case DEF:
                return "Default";
            case STATELESS:
                return "Stateless data";
            case REPL:
                return "Replacement data";
            case CSI:
                return "Client specific decision data";
            case NAMED:
                return "Named decision data";
            default:
                return "Unknown";
        }
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        final byte[] buf = new byte[4];
        buf[0] = (byte) (_cmdCode.ordinal() >> 8);
        buf[1] = (byte) _cmdCode.ordinal();
        buf[2] = (byte) (_flags.ordinal() >> 8);
        buf[3] = (byte) _flags.ordinal();
        COPSUtil.writeData(socket, buf, 4);

        COPSUtil.writeData(socket, _data.getData(), _data.length());
        if (_padding != null) {
            COPSUtil.writeData(socket, _padding.getData(), _padding.length());
        }
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        if (this.getHeader().getCType().equals(CType.DEF)) {
            os.write(("Decision (" + getTypeStr() + ")\n").getBytes());
            os.write(("Command code: " + _cmdCode + "\n").getBytes());
            os.write(("Command flags: " + _flags + "\n").getBytes());
        } else {
            os.write(("Decision (" + getTypeStr() + ")\n").getBytes());
            os.write(("Data: " + _data.str() + "\n").getBytes());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSDecision)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSDecision that = (COPSDecision) o;

        return _cmdCode == that._cmdCode && _flags == that._flags && _data.equals(that._data) &&
                _padding.equals(that._padding) ||
                COPSUtil.copsDataPaddingEquals(this._data, this._padding, that._data, that._padding);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _data.hashCode();
        result = 31 * result + _cmdCode.hashCode();
        result = 31 * result + _flags.hashCode();
        result = 31 * result + _padding.hashCode();
        return result;
    }

    /**
     * Parses bytes to return a COPSDecision object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSDecision parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        int _cmdCode = 0;
        _cmdCode |= ((short) dataPtr[4]) << 8;
        _cmdCode |= ((short) dataPtr[5]) & 0xFF;

        int _flags = 0;
        _flags |= ((short) dataPtr[6]) << 8;
        _flags |= ((short) dataPtr[7]) & 0xFF;

        final COPSData d;
        if (objHdrData.header.getCType().equals(CType.DEF)) {
            d = null;
        } else {
            d = new COPSData(dataPtr, 8, objHdrData.msgByteCount - 8);
        }
        return new COPSDecision(objHdrData.header, COPSDecision.VAL_TO_CMD.get(_cmdCode),
                COPSDecision.VAL_TO_FLAG.get(_flags), d);
    }

    /**
     * Supported command types
     */
    public enum Command {
        NULL,    // No configuration data available
        INSTALL, // Admit request/install configuration
        REMOVE   // Remove request/remove configuration
    }

    public enum DecisionFlag {
        NA,
        REQERROR, // = Trigger error
        REQSTATE, // = ???
    }

}

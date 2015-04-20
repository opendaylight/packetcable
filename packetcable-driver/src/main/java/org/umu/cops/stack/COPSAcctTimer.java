/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;


import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS Accounting Timer Object (RFC 2748)
 *
 * Times are encoded as 2 octet integer values and are in units of
 * seconds.  The timer value is treated as a delta.
 *
 * C-Num = 15,
 * C-Type = 1, Accounting timer value
 *
 * Optional timer value used to determine the minimum interval between
 * periodic accounting type reports. It is used by the PDP to describe
 * to the PEP an acceptable interval between unsolicited accounting
 * updates via Report messages where applicable. It provides a method
 * for the PDP to control the amount of accounting traffic seen by the
 * network. The range of finite time values is 1 to 65535 seconds
 * represented as an unsigned two-octet integer. A value of zero means
 * there SHOULD be no unsolicited accounting updates.
 */
public class COPSAcctTimer extends COPSTimer {

    /**
     * Constructor generally used for sending messages
     * @param timeVal - the timer value
     * @throws java.lang.IllegalArgumentException when the id parameter is null
     */
    public COPSAcctTimer(final short timeVal) {
        this((short)0, timeVal);
    }

    /**
     * Constructor generally used for sending messages with some reserved value
     * @param reserved - ???
     * @param timeVal - the timer value
     * @throws java.lang.IllegalArgumentException when the id parameter is null
     */
    protected COPSAcctTimer(final short reserved, final short timeVal) {
        this(new COPSObjHeader(CNum.ACCT_TIMER, CType.DEF), reserved, timeVal);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param header - the object header
     * @param reserved - ???
     * @param timeVal - the timer value
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSAcctTimer(final COPSObjHeader header, final short reserved, final short timeVal) {
        super(header, reserved, timeVal);
        if (!header.getCNum().equals(CNum.ACCT_TIMER))
            throw new IllegalArgumentException("Invalid CNum value. Must be " + CNum.ACCT_TIMER);
        if (!header.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Invalid CType value. Must be " + CType.DEF);
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSAcctTimer parse(final COPSObjHeaderData objHdrData, byte[] dataPtr) {
        short reserved = 0;
        reserved |= ((short) dataPtr[4]) << 8;
        reserved |= ((short) dataPtr[5]) & 0xFF;

        short timerValue = 0;
        timerValue |= ((short) dataPtr[6]) << 8;
        timerValue |= ((short) dataPtr[7]) & 0xFF;

        return new COPSAcctTimer(objHdrData.header, reserved, timerValue);
    }

}


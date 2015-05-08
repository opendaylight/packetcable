/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;


import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS Keep Alive Timer (RFC 2748)
 *
 * Times are encoded as 2 octet integer values and are in units of
 * seconds.  The timer value is treated as a delta.
 *
 * C-Num = 10,
 *
 * C-Type = 1, Keep-alive timer value
 * Timer object used to specify the maximum time interval over which a
 * COPS message MUST be sent or received. The range of finite timeouts
 * is 1 to 65535 seconds represented as an unsigned two-octet integer.
 * The value of zero implies infinity.
 */
public class COPSKATimer extends COPSTimer {

    /**
     * Constructor generally used for sending messages
     * @throws java.lang.IllegalArgumentException
     */
    public COPSKATimer(final short timeVal) {
        this((short)0, timeVal);
    }

    /**
     * Constructor to override the reserved member value from 0
     * @throws java.lang.IllegalArgumentException
     */
    public COPSKATimer(final short reserved, final short timeVal) {
        this(new COPSObjHeader(CNum.KA, CType.DEF), reserved, timeVal);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param reserved - ???
     * @param timeVal - the timer value
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSKATimer(final COPSObjHeader hdr, final short reserved, final short timeVal) {
        super(hdr, reserved, timeVal);
        if (!hdr.getCNum().equals(CNum.KA))
            throw new IllegalArgumentException("Invalid CNum value. Must be " + CNum.KA);
        if (!hdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Invalid CType value. Must be " + CType.DEF);
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSKATimer parse(final COPSObjHeaderData objHdrData, byte[] dataPtr) {
        short reserved = 0;
        reserved |= ((short) dataPtr[4]) << 8;
        reserved |= ((short) dataPtr[5]) & 0xFF;

        short timerValue = 0;
        timerValue |= ((short) dataPtr[6]) << 8;
        timerValue |= ((short) dataPtr[7]) & 0xFF;

        return new COPSKATimer(objHdrData.header, reserved, timerValue);
    }

}


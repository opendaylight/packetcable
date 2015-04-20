/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * For extension by other COPS Timer Objects such as COPSAcctTimer & COPSKATimer
 */
public abstract class COPSTimer extends COPSObjBase {

    // TODO - determine what is the function of this member
    private final short _reserved;

    /**
     * The timer values
     */
    private final short _timerValue;

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param header - the object header
     * @param reserved - ???
     * @param timerVal - the timer value
     */
    protected COPSTimer(final COPSObjHeader header, final short reserved, final short timerVal) {
        super(header);
        _reserved = reserved;
        _timerValue = timerVal;
    }

    /**
     * Method getTimerVal
     * @return   a short
     */
    public short getTimerVal() {
        return _timerValue;
    }

    @Override
    public void writeBody(Socket socket) throws IOException {
        byte[] buf = new byte[4];

        buf[0] = (byte) (_reserved >> 8);
        buf[1] = (byte) _reserved;
        buf[2] = (byte) (_timerValue >> 8);
        buf[3] = (byte) _timerValue;
        COPSUtil.writeData(socket, buf, 4);
    }

    @Override
    protected int getDataLength() {
        return 4;
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("Timer val: " + _timerValue + "\n").getBytes());
    }

}


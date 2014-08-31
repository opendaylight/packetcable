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
 * COPS Reason Object (RFC 2748 pag. 12)
 *
 *   This object specifies the reason why the request state was deleted.
 *   It appears in the delete request (DRQ) message. The Reason Sub-code
 *   field is reserved for more detailed client-specific reason codes
 *   defined in the corresponding documents.
 *
 *           C-Num = 5, C-Type = 1
 *
 *               0             1              2             3
 *       +--------------+--------------+--------------+--------------+
 *       |         Reason-Code         |       Reason Sub-code       |
 *       +--------------+--------------+--------------+--------------+
 *
 *           Reason Code:
 *               1 = Unspecified
 *               2 = Management
 *               3 = Preempted (Another request state takes precedence)
 *               4 = Tear (Used to communicate a signaled state removal)
 *               5 = Timeout (Local state has timed-out)
 *               6 = Route Change (Change invalidates request state)
 *               7 = Insufficient Resources (No local resource available)
 *               8 = PDP's Directive (PDP decision caused the delete)
 *                9 = Unsupported decision (PDP decision not supported)
 *               10= Synchronize Handle Unknown
 *               11= Transient Handle (stateless event)
 *               12= Malformed Decision (could not recover)
 *               13= Unknown COPS Object from PDP:
 *                   Sub-code (octet 2) contains unknown object's C-Num
 *                   and (octet 3) contains unknown object's C-Type.
 *
 * @version COPSReason.java, v 1.00 2003
 *
 */
public class COPSReason extends COPSPrObjBase {

    public final static String[] G_msgArray = {
        "Unknown.",
        "Unspecified.",
        "Management.",
        "Preempted (Another request state takes precedence).",
        "Tear (Used to communicate a signaled state removal).",
        "Timeout ( Local state has timed-out).",
        "Route change (Change invalidates request state).",
        "Insufficient Resources.",
        "PDP's Directive.",
        "Unsupported decision.",
        "Synchronize handle unknown.",
        "Transient handle.",
        "Malformed decision.",
        "Unknown COPS object from PDP.",
    };

    private COPSObjHeader _objHdr;
    private short _reasonCode;
    private short _reasonSubCode;

    ///
    public COPSReason(short reasonCode, short subCode) {
        _objHdr = new COPSObjHeader();
        _reasonCode = reasonCode;
        _reasonSubCode = subCode;
        _objHdr.setCNum(COPSObjHeader.COPS_REASON_CODE);
        _objHdr.setCType((byte) 1);
        _objHdr.setDataLength((short) 4);
    }

    /**
          Parse data and create COPSReason object
     */
    protected COPSReason(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        _reasonCode |= ((short) dataPtr[4]) << 8;
        _reasonCode |= ((short) dataPtr[5]) & 0xFF;
        _reasonSubCode |= ((short) dataPtr[6]) << 8;
        _reasonSubCode |= ((short) dataPtr[7]) & 0xFF;

        _objHdr.setDataLength((short) 4);
    }

    /**
     * Returns size in number of octects, including header
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        return (_objHdr.getDataLength());
    }

    /**
     * Get Reason description
     *
     * @return   a String
     *
     */
    public String getDescription() {
        String reasonStr1;
        String reasonStr2;

        ///Get the details from the error code
        reasonStr1 = G_msgArray[_reasonCode];
        //TODO - defind reason sub-codes
        reasonStr2 = "";
        return (reasonStr1 + ":" + reasonStr2);
    }

    /**
     * Always return true
     *
     * @return   a boolean
     *
     */
    public boolean isReason() {
        return true;
    }

    /**
     * Write object in network byte order to a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {

        _objHdr.writeData(id);

        byte[] buf = new byte[4];

        buf[0] = (byte) (_reasonCode >> 8);
        buf[1] = (byte) _reasonCode;
        buf[2] = (byte) (_reasonSubCode >> 8);
        buf[3] = (byte) _reasonSubCode;


        COPSUtil.writeData(id, buf, 4);
    }

    /**
     * Write an object textual description in the output stream
     *
     * @param    os                  an OutputStream
     *
     * @throws   IOException
     *
     */
    public void dump(OutputStream os) throws IOException {
        _objHdr.dump(os);
        os.write(new String("Reason Code: " + _reasonCode + "\n").getBytes());
        os.write(new String("Reason Sub Code: " + _reasonSubCode + "\n").getBytes());
    }
}



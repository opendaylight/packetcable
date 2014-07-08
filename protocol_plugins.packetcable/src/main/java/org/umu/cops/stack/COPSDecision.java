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
 * COPS Decision
 *
 * @version COPSDecision.java, v 1.00 2003
 *
 */
public class COPSDecision extends COPSObjBase {

    // CType
    public final static byte DEC_DEF = 1;
    public final static byte DEC_STATELESS = 2;
    public final static byte DEC_REPL = 3;
    public final static byte DEC_CSI = 4;
    public final static byte DEC_NAMED = 5;

    // Command
    public final static byte DEC_NULL = 0;
    public final static byte DEC_INSTALL = 1;
    public final static byte DEC_REMOVE = 2;

    // Flags
    public final static byte F_REQERROR = 0x1;
    public final static byte F_REQSTATE = 0x2;

    protected COPSObjHeader _objHdr;
    private COPSData _data;
    private short _cmdCode;
    private short _flags;
    private COPSData _padding;

    /**
      Constructor to create a Decision object. By default creates
      a decision object which is of fixed length.
     */
    public COPSDecision(byte cType) {
        _objHdr = new COPSObjHeader();
        _cmdCode = 0;
        _flags = 0;
        _objHdr.setCNum(COPSObjHeader.COPS_DEC);
        _objHdr.setCType(cType);
        if (cType == DEC_DEF) _objHdr.setDataLength( (short) 4);
    }

    public COPSDecision() {
        _objHdr = new COPSObjHeader();
        _cmdCode = 0;
        _flags = 0;
        _objHdr.setCNum(COPSObjHeader.COPS_DEC);
        _objHdr.setCType(DEC_DEF);
        _objHdr.setDataLength( (short) 4);
    }

    /**
          Initialize the decision object with values from COPSObj header
     */
    protected COPSDecision(byte[] dataPtr) {
        _objHdr = new COPSObjHeader();
        _objHdr.parse(dataPtr);
        // _objHdr.checkDataLength();

        _cmdCode = 0;
        _flags = 0;
        if (_objHdr.getCType() == DEC_DEF) {
            _cmdCode |= ((short) dataPtr[4]) << 8;
            _cmdCode |= ((short) dataPtr[5]) & 0xFF;
            _flags |= ((short) dataPtr[6]) << 8;
            _flags |= ((short) dataPtr[7]) & 0xFF;

            _objHdr.setDataLength((short) 4);
        } else {
            int dLen = _objHdr.getDataLength() - 4;
            COPSData d = new COPSData(dataPtr, 4, dLen);
            setData(d);
        }
    }

    /**
     * Method getDataLength
     *
     * @return   a short
     *
     */
    public short getDataLength() {
        int lpadding = 0;
        if (_padding != null) lpadding = _padding.length();
        return ((short) (_objHdr.getDataLength() + lpadding));
    }



    /**
     * Get the associated data if decision object is of cType 2 or higher
     *
     * @return   a COPSData
     *
     */
    public COPSData getData() {
        return (_data);
    }

    /**
     * Set the decision data if decision object is of cType 2 or higher
     *
     * @param    data                a  COPSData
     *
     */
    public void setData(COPSData data) {
        if (data.length() % 4 != 0) {
            int padLen = 4 - data.length() % 4;
            _padding = getPadding(padLen);
        }
        _data = data;
        _objHdr.setDataLength((short) data.length());
    }

    /**
     * Retruns true if cType = 1
     *
     * @return   a boolean
     *
     */
    public boolean isFlagSet() {
        return ( _objHdr.getCType() == 1);
    };

    /**
     * If cType == 1 , get the flags associated
     *
     * @return   a short
     *
     */
    public short getFlags() {
        return (_flags);
    };

    /**
     * If cType == 1 ,set the cmd code
     *
     * @param    cCode               a  byte
     *
     */
    public void setCmdCode(byte cCode) {
        _cmdCode = (short) cCode;
    }

    /**
     * If cType == 1 ,set the cmd flags
     *
     * @param    flags               a  short
     *
     */
    public void setFlags(short flags) {
        _flags = flags;
    }

    /**
     * Method isNullDecision
     *
     * @return   a boolean
     *
     */
    public boolean isNullDecision() {
        return ( _cmdCode == 0);
    };

    /**
     * Method isInstallDecision
     *
     * @return   a boolean
     *
     */
    public boolean isInstallDecision() {
        return ( _cmdCode == 1);
    };

    /**
     * Method isRemoveDecision
     *
     * @return   a boolean
     *
     */
    public boolean isRemoveDecision() {
        return ( _cmdCode == 2);
    };

    /**
     * Method getTypeStr
     *
     * @return   a String
     *
     */
    public String getTypeStr() {
        switch (_objHdr.getCType()) {
        case DEC_DEF:
            return "Default";
        case DEC_STATELESS:
            return "Stateless data";
        case DEC_REPL:
            return "Replacement data";
        case DEC_CSI:
            return "Client specific decision data";
        case DEC_NAMED:
            return "Named decision data";
        default:
            return "Unknown";
        }
    }

    /**
     * Method isDecision
     *
     * @return   a boolean
     *
     */
    public boolean isDecision() {
        return true;
    };

    /**
     * Method isLocalDecision
     *
     * @return   a boolean
     *
     */
    public boolean isLocalDecision() {
        return false;
    };

    /**
     * Writes data to a given network socket
     *
     * @param    id                  a  Socket
     *
     * @throws   IOException
     *
     */
    public void writeData(Socket id) throws IOException {
        _objHdr.writeData(id);

        if (_objHdr.getCType() >= 2) {
            COPSUtil.writeData(id, _data.getData(), _data.length());
            if (_padding != null) {
                COPSUtil.writeData(id, _padding.getData(), _padding.length());
            }
        } else {
            byte[] buf = new byte[4];
            buf[0] = (byte) (_cmdCode >> 8);
            buf[1] = (byte) _cmdCode;
            buf[2] = (byte) (_flags >> 8);
            buf[3] = (byte) _flags;
            COPSUtil.writeData(id, buf, 4);
        }
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

        if (_objHdr.getCType() == 1) {
            os.write(new String("Decision (" + getTypeStr() + ")\n").getBytes());
            os.write(new String("Command code: " + _cmdCode + "\n").getBytes());
            os.write(new String("Command flags: " + _flags + "\n").getBytes());
        } else {
            os.write(new String("Decision (" + getTypeStr() + ")\n").getBytes());
            os.write(new String("Data: " + _data.str() + "\n").getBytes());
        }
    }
}











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

/**
 * COPS PEP Identification Object (RFC 2748)
 *
 * The PEP Identification Object is used to identify the PEP client to
 * the remote PDP. It is required for Client-Open messages.
 *
 * C-Num = 11, C-Type = 1
 *
 * Variable-length field. It is a NULL terminated ASCII string that is
 * also zero padded to a 32-bit word boundary (so the object length is a
 * multiple of 4 octets). The PEPID MUST contain an ASCII string that
 * uniquely identifies the PEP within the policy domain in a manner that
 * is persistent across PEP reboots. For example, it may be the PEP's
 * statically assigned IP address or DNS name. This identifier may
 * safely be used by a PDP as a handle for identifying the PEP in its
 * policy rules.
 */
public class COPSPepId extends COPSObjBase {

    /**
     * The payload data
     */
    final COPSData _data;

    /**
     * Bytes to add to outbound payload to ensure the length is divisible by 4 bytes
     */
    final COPSData _padding;

    /**
     * Constructor generally used for sending messages
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    public COPSPepId(final COPSData data) {
        this(new COPSObjHeader(CNum.PEPID, CType.DEF), data);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param data - the data
     * @throws java.lang.IllegalArgumentException
     */
    public COPSPepId(final COPSObjHeader hdr, final COPSData data) {
        super(hdr);

        if (!hdr.getCNum().equals(CNum.PEPID))
            throw new IllegalArgumentException("CNum must be equal to " + CNum.PEPID);
        if (!hdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("CType must be equal to " + CType.DEF);
        if (data == null) throw new IllegalArgumentException("Data must not be null");

        _data = data;
        if ((_data.length() % 4) != 0) {
            final int padLen = 4 - (_data.length() % 4);
            _padding = COPSObjectParser.getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
    }

    @Override
    protected int getDataLength() {
        return _data.length() + _padding.length();
    }

    /**
     * Method getData
     * @return   a COPSData
     */
    public COPSData getData() {
        return _data;
    }

    @Override
    protected void writeBody(final Socket socket) throws IOException {
        COPSUtil.writeData(socket, _data.getData(), _data.length());
        if (_padding != null) {
            COPSUtil.writeData(socket, _padding.getData(), _padding.length());
        }
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        os.write(("PEPID: " + _data.str() + "\n").getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSPepId)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSPepId that = (COPSPepId) o;

        return _data.equals(that._data) && _padding.equals(that._padding) ||
                COPSUtil.copsDataPaddingEquals(this._data, this._padding, that._data, that._padding);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _data.hashCode();
        result = 31 * result + _padding.hashCode();
        return result;
    }

    /**
     * Parses bytes to return a COPSPepId object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSPepId parse(final COPSObjHeaderData objHdrData, byte[] dataPtr) {
        short dLen = (short)(objHdrData.msgByteCount - 4);
        return new COPSPepId(objHdrData.header, new COPSData (dataPtr, 4, dLen));
    }

}




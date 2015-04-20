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
 * COPS Integrity Object (RFC 2748)
 *
 * The integrity object includes a sequence number and a message digest
 * useful for authenticating and validating the integrity of a COPS
 * message. When used, integrity is provided at the end of a COPS
 * message as the last COPS object. The digest is then computed over all
 * of a particular COPS message up to but not including the digest value
 * itself. The sender of a COPS message will compute and fill in the
 * digest portion of the Integrity object. The receiver of a COPS
 * message will then compute a digest over the received message and
 * verify it matches the digest in the received Integrity object.
 *
 * C-Num = 16,
 *
 * C-Type = 1, HMAC digest
 *
 * The HMAC integrity object employs HMAC (Keyed-Hashing for Message
 * Authentication) [HMAC] to calculate the message digest based on a key
 * shared between the PEP and its PDP.
 *
 * This Integrity object specifies a 32-bit Key ID used to identify a
 * specific key shared between a particular PEP and its PDP and the
 * cryptographic algorithm to be used. The Key ID allows for multiple
 * simultaneous keys to exist on the PEP with corresponding keys on the
 * PDP for the given PEPID. The key identified by the Key ID was used to
 * compute the message digest in the Integrity object. All
 * implementations, at a minimum, MUST support HMAC-MD5-96, which is
 * HMAC employing the MD5 Message-Digest Algorithm [MD5] truncated to
 * 96-bits to calculate the message digest.
 *
 * This object also includes a sequence number that is a 32-bit unsigned
 * integer used to avoid replay attacks. The sequence number is
 * initiated during an initial Client-Open Client-Accept message
 * exchange and is then incremented by one each time a new message is
 * sent over the TCP connection in the same direction. If the sequence
 * number reaches the value of 0xFFFFFFFF, the next increment will
 * simply rollover to a value of zero.
 *
 * The variable length digest is calculated over a COPS message starting
 * with the COPS Header up to the Integrity Object (which MUST be the
 * last object in a COPS message) INCLUDING the Integrity object's
 * header, Key ID, and Sequence Number. The Keyed Message Digest field
 * is not included as part of the digest calculation. In the case of
 * HMAC-MD5-96, HMAC-MD5 will produce a 128-bit digest that is then to
 * be truncated to 96-bits before being stored in or verified against
 * the Keyed Message Digest field as specified in [HMAC]. The Keyed
 * Message Digest MUST be 96-bits when HMAC-MD5-96 is used.
 */
public class COPSIntegrity extends COPSObjBase {

    private final int _keyId;
    private final int _seqNum;
    private final COPSData _keyDigest;
    private final COPSData _padding;

    /**
     * Constructor generally used for sending messages
     * // TODO - why aren't any classes requiring injection of the keyId, seqNum, or keyDigest members???
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIntegrity() {
        this(0, 0, new COPSData());
    }

    public COPSIntegrity(final int keyId, final int seqNum, final COPSData keyDigest) {
        this(new COPSObjHeader(CNum.MSG_INTEGRITY, CType.DEF), keyId, seqNum, keyDigest);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param keyId - the keyId
     * @param seqNum - the sequence number
     * @param keyDigest - the data
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIntegrity(final COPSObjHeader hdr, final int keyId, final int seqNum, final COPSData keyDigest) {
        super(hdr);
        if (!hdr.getCNum().equals(CNum.MSG_INTEGRITY))
            throw new IllegalArgumentException("CNum must be of type - " + CNum.MSG_INTEGRITY);
        if (!hdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("CType must be of type - " + CType.DEF);

        _keyId = keyId;
        _seqNum = seqNum;

        if (keyDigest == null) _keyDigest = new COPSData();
        else _keyDigest = keyDigest;
        if ((_keyDigest.length() % 4) != 0) {
            final int padLen = 4 - (_keyDigest.length() % 4);
            _padding = COPSObjectParser.getPadding(padLen);
        } else {
            _padding = new COPSData();
        }
    }

    // Getters
    public int getKeyId() { return _keyId; }
    public int getSeqNum() { return _seqNum; }
    public COPSData getKeyDigest() { return _keyDigest; }

    @Override
    public int getDataLength() {
        return 8 + _keyDigest.length() + _padding.length();
    }

    @Override
    public void writeBody(final Socket socket) throws IOException {
        final byte[] buf = new byte[8];
        buf[0] = (byte) (_keyId >> 24);
        buf[1] = (byte) (_keyId >> 16);
        buf[2] = (byte) (_keyId >> 8);
        buf[3] = (byte) _keyId;
        buf[4] = (byte) (_seqNum >> 24);
        buf[5] = (byte) (_seqNum >> 16);
        buf[6] = (byte) (_seqNum >> 8);
        buf[7] = (byte) _seqNum;
        COPSUtil.writeData(socket, buf, 8);

        COPSUtil.writeData(socket, _keyDigest.getData(), _keyDigest.length());
        if (_padding != null) {
            COPSUtil.writeData(socket, _padding.getData(), _padding.length());
        }
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("Key Id: " + _keyId + "\n").getBytes());
        os.write(("Sequence: " + _seqNum + "\n").getBytes());
        os.write(("Key digest: " + _keyDigest.str() + "\n").getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSIntegrity)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSIntegrity integrity = (COPSIntegrity) o;

        if (_keyId != integrity._keyId) {
            return false;
        }
        if (_seqNum != integrity._seqNum) {
            return false;
        }
        if (_keyDigest.equals(integrity._keyDigest) && _padding.equals(integrity._padding)) return true;
        else
            return COPSUtil.copsDataPaddingEquals(this._keyDigest, this._padding,
                    integrity._keyDigest, integrity._padding);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + _keyId;
        result = 31 * result + _seqNum;
        result = 31 * result + _keyDigest.hashCode();
        result = 31 * result + _padding.hashCode();
        return result;
    }

    /**
     * Parses bytes to return a COPSIntegrity object
     * @param objHdrData - the associated header
     * @param dataPtr - the data to parse
     * @return - the object
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSIntegrity parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        int keyId = 0;
        keyId |= ((short) dataPtr[4]) << 24;
        keyId |= ((short) dataPtr[5]) << 16;
        keyId |= ((short) dataPtr[6]) << 8;
        keyId |= ((short) dataPtr[7]) & 0xFF;

        int seqNum = 0;
        seqNum |= ((short) dataPtr[8]) << 24;
        seqNum |= ((short) dataPtr[9]) << 16;
        seqNum |= ((short) dataPtr[10]) << 8;
        seqNum |= ((short) dataPtr[11]) & 0xFF;

        final int usedBytes = objHdrData.header.getHdrLength() + 8;
        if (objHdrData.msgByteCount > usedBytes)
            return new COPSIntegrity(objHdrData.header, keyId, seqNum,
                    new COPSData(dataPtr, usedBytes, objHdrData.msgByteCount - usedBytes));
        else return new COPSIntegrity(objHdrData.header, keyId, seqNum, new COPSData());
    }

}



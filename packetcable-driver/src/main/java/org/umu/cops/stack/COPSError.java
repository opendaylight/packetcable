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
 * COPS Error (RFC 2748)
 *
 * This object is used to identify a particular COPS protocol error.
 * The error sub-code field contains additional detailed client specific
 * error codes. The appropriate Error Sub-codes for a particular
 * client-type SHOULD be specified in the relevant COPS extensions
 * document.
 *
 * C-Num = 8, C-Type = 1
 *
 * Error-Code:
 *
 * 1 = Bad handle
 * 2 = Invalid handle reference
 * 3 = Bad message format (Malformed Message)
 * 4 = Unable to process (server gives up on query)
 * 5 = Mandatory client-specific info missing
 * 6 = Unsupported client-type
 * 7 = Mandatory COPS object missing
 * 8 = Client Failure
 * 9 = Communication Failure
 * 10= Unspecified
 * 11= Shutting down
 * 12= Redirect to Preferred Server
 * 13= Unknown COPS Object:
 * Sub-code (octet 2) contains unknown object's C-Num
 * and (octet 3) contains unknown object's C-Type.
 * 14= Authentication Failure
 * 15= Authentication Required
 */
public class COPSError extends COPSObjBase {

    public final static Map<Integer, ErrorTypes> ERROR_CODE_TO_TYPE = new ConcurrentHashMap<>();
    static {
        ERROR_CODE_TO_TYPE.put(ErrorTypes.NA.ordinal(), ErrorTypes.NA);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.BAD_HANDLE.ordinal(), ErrorTypes.BAD_HANDLE);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.BAD_HANDLE_REF.ordinal(), ErrorTypes.BAD_HANDLE_REF);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.BAD_MSG_FORMAT.ordinal(), ErrorTypes.BAD_MSG_FORMAT);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.FAIL_PROCESS.ordinal(), ErrorTypes.FAIL_PROCESS);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.MISSING_INFO.ordinal(), ErrorTypes.MISSING_INFO);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.UNSUPPORTED_CLIENT_TYPE.ordinal(), ErrorTypes.UNSUPPORTED_CLIENT_TYPE);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.MANDATORY_OBJECT_MISSING.ordinal(), ErrorTypes.MANDATORY_OBJECT_MISSING);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.CLIENT_FAILURE.ordinal(), ErrorTypes.CLIENT_FAILURE);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.COMM_FAILURE.ordinal(), ErrorTypes.COMM_FAILURE);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.UNKNOWN.ordinal(), ErrorTypes.UNKNOWN);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.SHUTTING_DOWN.ordinal(), ErrorTypes.SHUTTING_DOWN);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.PDP_REDIRECT.ordinal(), ErrorTypes.PDP_REDIRECT);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.UNKNOWN_OBJECT.ordinal(), ErrorTypes.UNKNOWN_OBJECT);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.AUTH_FAILURE.ordinal(), ErrorTypes.AUTH_FAILURE);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.AUTH_REQUIRED.ordinal(), ErrorTypes.AUTH_REQUIRED);
        ERROR_CODE_TO_TYPE.put(ErrorTypes.MA.ordinal(), ErrorTypes.MA);
    }

    private final static Map<ErrorTypes, String> ERROR_TYPE_TO_STRING = new ConcurrentHashMap<>();
    static {
        ERROR_TYPE_TO_STRING.put(ErrorTypes.NA, "Unknown.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.BAD_HANDLE, "Bad handle.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.BAD_HANDLE_REF, "Invalid handle reference.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.BAD_MSG_FORMAT, "Bad message format (Malformed message).");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.FAIL_PROCESS, "Unable to process.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.MISSING_INFO, "Mandatory client-specific info missing.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.UNSUPPORTED_CLIENT_TYPE, "Unsupported client-type");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.MANDATORY_OBJECT_MISSING, "Mandatory COPS object missing.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.CLIENT_FAILURE, "Client failure.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.COMM_FAILURE, "Communication failure.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.UNKNOWN, "Unknown.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.SHUTTING_DOWN, "Shutting down.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.PDP_REDIRECT, "Redirect to preferred server.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.UNKNOWN_OBJECT, "Unknown COPS object");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.AUTH_FAILURE, "Authentication failure.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.AUTH_REQUIRED, "Authentication required.");
        ERROR_TYPE_TO_STRING.put(ErrorTypes.MA, "Authentication required.");
    }

    /**
     * The error code
     */
    private ErrorTypes _errCode;

    /**
     * Additional detailed client specific error codes
     */
    private ErrorTypes _errSubCode;

    /**
     * Constructor generally used for sending messages
     * @param errCode - the error code
     * @param subCode - the type of message
     * @throws java.lang.IllegalArgumentException
     */
    public COPSError(final ErrorTypes errCode, final ErrorTypes subCode) {
        this(new COPSObjHeader(CNum.ERROR, CType.DEF), errCode, subCode);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param errCode - the error code
     * @param subCode - the type of message
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSError(final COPSObjHeader hdr, final ErrorTypes errCode, final ErrorTypes subCode) {
        super(hdr);
        if (!hdr.getCNum().equals(CNum.ERROR))
            throw new IllegalArgumentException("Must have a CNum value of " + CNum.ERROR);
        if (!hdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Must have a CType value of " + CType.DEF);
        if (errCode == null || subCode == null) throw new IllegalArgumentException("Error codes must not be null");
        if (errCode.equals(ErrorTypes.NA))
            throw new IllegalArgumentException("Error code must not be of type " + ErrorTypes.NA);

        _errCode = errCode;
        _errSubCode = subCode;
    }

    public ErrorTypes getErrCode() {
        return _errCode;
    }

    public ErrorTypes getErrSubCode() {
        return _errSubCode;
    }

    @Override
    public int getDataLength() {
        return 4;
    }

    /**
     * Method getDescription
     * @return   a String
     */
    public String getDescription() {
        String errStr1;
        String errStr2;

        ///Get the details from the error code
        errStr1 = ERROR_TYPE_TO_STRING.get(_errCode);
        //TODO - define error sub-codes
        errStr2 = "";
        return (errStr1 + ":" + errStr2);
    }

    @Override
    public void writeBody(final Socket socket) throws IOException {
        final byte[] buf = new byte[4];

        buf[0] = (byte) ((byte)_errCode.ordinal() >> 8);
        buf[1] = (byte)_errCode.ordinal();
        buf[2] = (byte) ((byte)_errSubCode.ordinal() >> 8);
        buf[3] = (byte)_errSubCode.ordinal();

        COPSUtil.writeData(socket, buf, 4);
    }

    @Override
    public void dumpBody(final OutputStream os) throws IOException {
        os.write(("Error Code: " + _errCode + "\n").getBytes());
        os.write(("Error Sub Code: " + _errSubCode + "\n").getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof COPSError)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final COPSError copsError = (COPSError) o;

        return _errCode == copsError._errCode && _errSubCode == copsError._errSubCode;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_errCode != null ? _errCode.hashCode() : 0);
        result = 31 * result + (_errSubCode != null ? _errSubCode.hashCode() : 0);
        return result;
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSError parse(final COPSObjHeaderData objHdrData, byte[] dataPtr) {
        int errCode = 0;
        errCode |= ((short) dataPtr[4]) << 8;
        errCode |= ((short) dataPtr[5]) & 0xFF;

        int errSubCode = 0;
        errSubCode |= ((short) dataPtr[6]) << 8;
        errSubCode |= ((short) dataPtr[7]) & 0xFF;

        return new COPSError(objHdrData.header, ERROR_CODE_TO_TYPE.get(errCode), ERROR_CODE_TO_TYPE.get(errSubCode));
    }

    /**
     * The different error types and the ordinal value will be serialized
     */
    public enum ErrorTypes {
        NA,
        BAD_HANDLE,
        BAD_HANDLE_REF,
        BAD_MSG_FORMAT,
        FAIL_PROCESS,
        MISSING_INFO,
        UNSUPPORTED_CLIENT_TYPE,
        MANDATORY_OBJECT_MISSING,
        CLIENT_FAILURE,
        COMM_FAILURE,
        UNKNOWN,
        SHUTTING_DOWN,
        PDP_REDIRECT,
        UNKNOWN_OBJECT,
        AUTH_FAILURE,
        AUTH_REQUIRED,
        MA
    }

}


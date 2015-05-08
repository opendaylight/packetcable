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
 * COPS Report Type (RFC 2748 pg. 16)
 *
 *   The Type of Report on the request state associated with a handle:
 *
 *           C-Num = 12, C-Type = 1
 *
 *               0             1              2             3
 *       +--------------+--------------+--------------+--------------+
 *       |         Report-Type         |        /////////////        |
 *       +--------------+--------------+--------------+--------------+
 *
 *           Report-Type:
 *               1 = Success   : Decision was successful at the PEP
 *               2 = Failure   : Decision could not be completed by PEP
 *               3 = Accounting: Accounting update for an installed state
 *
 *
 * @version COPSReportType.java, v 1.00 2003
 *
 */
public class COPSReportType extends COPSObjBase {

    private final static Map<Integer, ReportType> VAL_TO_RPT_TYPE = new ConcurrentHashMap<>();
    static {
        VAL_TO_RPT_TYPE.put(ReportType.NA.ordinal(), ReportType.NA);
        VAL_TO_RPT_TYPE.put(ReportType.SUCCESS.ordinal(), ReportType.SUCCESS);
        VAL_TO_RPT_TYPE.put(ReportType.FAILURE.ordinal(), ReportType.FAILURE);
        VAL_TO_RPT_TYPE.put(ReportType.ACCOUNTING.ordinal(), ReportType.ACCOUNTING);
    }

    private final static Map<ReportType, String> RPT_TYPE_TO_STRING = new ConcurrentHashMap<>();
    static {
        RPT_TYPE_TO_STRING.put(ReportType.NA, "Unknown.");
        RPT_TYPE_TO_STRING.put(ReportType.SUCCESS, "Success.");
        RPT_TYPE_TO_STRING.put(ReportType.FAILURE, "Failure.");
        RPT_TYPE_TO_STRING.put(ReportType.ACCOUNTING, "Accounting.");
    }

    /**
     * The type of report
     */
    private final ReportType _rType;

    /**
     * What is this attribute used for???
     */
    private final short _reserved;

    /**
     * Constructor generally used for sending messages
     * @param rType - the report type
     * @throws java.lang.IllegalArgumentException
     */
    public COPSReportType(final ReportType rType) {
        this(new COPSObjHeader(CNum.RPT, CType.DEF), rType, (short)0);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param hdr - the object header
     * @param rType - the report type
     * @param reserved - ???
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSReportType(final COPSObjHeader hdr, final ReportType rType, final short reserved) {
        super(hdr);
        if (!hdr.getCNum().equals(CNum.RPT))
            throw new IllegalArgumentException("Must have a CNum value of " + CNum.RPT);
        if (!hdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("Must have a CType value of " + CType.DEF);
        if (rType == null) throw new IllegalArgumentException("Report type must not be null");
        if (rType.equals(ReportType.NA))
            throw new IllegalArgumentException("Report type must not be of type " + ReportType.NA);

        _rType = rType;
        _reserved = reserved;
    }

    public ReportType getReportType() { return _rType; }

    @Override
    public int getDataLength() {
        return 4;
    }

    @Override
    protected void writeBody(final Socket id) throws IOException {
        final byte[] buf = new byte[4];

        buf[0] = (byte) (_rType.ordinal() >> 8);
        buf[1] = (byte) _rType.ordinal();
        buf[2] = (byte) (_reserved >> 8);
        buf[3] = (byte) _reserved;

        COPSUtil.writeData(id, buf, 4);
    }

    @Override
    protected void dumpBody(final OutputStream os) throws IOException {
        os.write(("Report: " + RPT_TYPE_TO_STRING.get(_rType) + "\n").getBytes());
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSReportType parse(final COPSObjHeaderData objHdrData, byte[] dataPtr) {
        short rType = 0;
        rType |= ((short) dataPtr[4]) << 8;
        rType |= ((short) dataPtr[5]) & 0xFF;

        short reserved = 0;
        reserved |= ((short) dataPtr[6]) << 8;
        reserved |= ((short) dataPtr[7]) & 0xFF;

        return new COPSReportType(objHdrData.header, VAL_TO_RPT_TYPE.get((int)rType), reserved);
    }

    /**
     * The supported report types
     */
    public enum ReportType {
        NA, SUCCESS, FAILURE, ACCOUNTING
    }

}






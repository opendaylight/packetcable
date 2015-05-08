/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS IPv4 Input Address (RFC 2748)
 *
 * The In-Interface Object is used to identify the incoming interface on
 * which a particular request applies and the address where the received
 * message originated. For flows or messages generated from the PEP's
 * local host, the loop back address and ifindex are used.
 *
 * This Interface object is also used to identify the incoming
 * (receiving) interface via its ifindex. The ifindex may be used to
 * differentiate between sub-interfaces and unnumbered interfaces (see
 * RSVP's LIH for an example). When SNMP is supported by the PEP, this
 * ifindex integer MUST correspond to the same integer value for the
 * interface in the SNMP MIB-II interface index table.
 *
 * Note: The ifindex specified in the In-Interface is typically relative
 * to the flow of the underlying protocol messages. The ifindex is the
 * interface on which the protocol message was received.
 *
 * C-Num = 3
 *
 * C-Type = 1, IPv4 Address + Interface
 *
 * 0             1              2             3
 * +--------------+--------------+--------------+--------------+
 * |                   IPv4 Address format                     |
 * +--------------+--------------+--------------+--------------+
 * |                          ifindex                          |
 * +--------------+--------------+--------------+--------------+
 *
 * For this type of the interface object, the IPv4 address specifies the
 * IP address that the incoming message came from.
 */
public class COPSIpv4InInterface extends COPSIpv4Interface {

    /**
     * Constructor generally used for sending messages
     * @param ifindex - the interface value
     * @param addr - the IPv4 address
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv4InInterface(final COPSIpv4Address addr, final int ifindex) {
        this(new COPSObjHeader(CNum.ININTF, CType.DEF), addr, ifindex);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param objHdr - the object header
     * @param ifindex - the interface value
     * @param addr - the IPv4 address
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv4InInterface(final COPSObjHeader objHdr, final COPSIpv4Address addr, final int ifindex) {
        super(objHdr, addr, ifindex);
        if (!objHdr.getCNum().equals(CNum.ININTF))
            throw new IllegalArgumentException("CNum must be of type - " + CNum.ININTF);
        if (!objHdr.getCType().equals(CType.DEF))
            throw new IllegalArgumentException("CType must be of type - " + CType.DEF);
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSIpv4InInterface parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        return new COPSIpv4InInterface(objHdrData.header, COPSIpv4Interface.parseAddress(dataPtr),
                COPSIpv4Interface.parseIfIndex(dataPtr));
    }

    public boolean isInInterface() { return true; }
}

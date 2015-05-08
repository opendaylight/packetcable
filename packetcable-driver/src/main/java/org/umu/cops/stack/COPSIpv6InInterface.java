/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS IPv6 Input Address (RFC 2748)
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
 * C-Type = 2, IPv6 Address + Interface
 *
 * 0             1              2             3
 * +--------------+--------------+--------------+--------------+
 * |                                                           |
 * +                                                           +
 * |                                                           |
 * +                    IPv6 Address format                    +
 * |                                                           |
 * +                                                           +
 * |                                                           |
 * +--------------+--------------+--------------+--------------+
 * |                          ifindex                          |
 * +--------------+--------------+--------------+--------------+
 *
 * For this type of the interface object, the IPv6 address specifies the
 * IP address that the incoming message came from. The ifindex is used
 * to refer to the MIB-II defined local incoming interface on the PEP as
 * described above.
 */
public class COPSIpv6InInterface extends COPSIpv6Interface {

    /**
     * Constructor generally used for sending messages
     * @param ifindex - the interface value
     * @param addr - the IPv6 address
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv6InInterface(final COPSIpv6Address addr, final int ifindex) {
        super(new COPSObjHeader(CNum.ININTF, CType.STATELESS), addr, ifindex);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param objHdr - the object header
     * @param ifindex - the interface value
     * @param addr - the IPv6 address
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv6InInterface(final COPSObjHeader objHdr, final COPSIpv6Address addr, final int ifindex) {
        super(objHdr, addr, ifindex);
        if (!objHdr.getCNum().equals(CNum.ININTF))
            throw new IllegalArgumentException("CNum must be of type - " + CNum.ININTF);
        if (!objHdr.getCType().equals(CType.STATELESS))
            throw new IllegalArgumentException("CType must be of type - " + CType.STATELESS);
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSIpv6InInterface parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        return new COPSIpv6InInterface(objHdrData.header, COPSIpv6Interface.parseAddress(dataPtr),
                COPSIpv6Interface.parseIfIndex(dataPtr));
    }

    public boolean isInInterface() { return true; }
}



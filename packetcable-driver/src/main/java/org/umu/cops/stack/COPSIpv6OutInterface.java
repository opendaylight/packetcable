/*
 * Copyright (c) 2003 University of Murcia.  All rights reserved.
 * --------------------------------------------------------------
 * For more information, please see <http://www.umu.euro6ix.org/>.
 */

package org.umu.cops.stack;

import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

/**
 * COPS IPv6 Output Interface (RFC 2748)
 *
 * The Out-Interface is used to identify the outgoing interface to which
 * a specific request applies and the address for where the forwarded
 * message is to be sent. For flows or messages destined to the PEP's
 * local host, the loop back address and ifindex are used.  The Out-
 * Interface has the same formats as the In-Interface Object.
 *
 * This Interface object is also used to identify the outgoing
 * (forwarding) interface via its ifindex. The ifindex may be used to
 * differentiate between sub-interfaces and unnumbered interfaces (see
 * RSVP's LIH for an example). When SNMP is supported by the PEP, this
 * ifindex integer MUST correspond to the same integer value for the
 * interface in the SNMP MIB-II interface index table.
 *
 * Note: The ifindex specified in the Out-Interface is typically
 * relative to the flow of the underlying protocol messages. The ifindex
 * is the one on which a protocol message is about to be forwarded.
 *
 * C-Num = 4
 *
 * C-Type = 1, IPv4 Address + Interface
 *
 * Same C-Type format as the In-Interface object. The IPv4 address
 * specifies the IP address to which the outgoing message is going. The
 * ifindex is used to refer to the MIB-II defined local outgoing
 * interface on the PEP.
 * C-Type = 2, IPv6 Address + Interface
 *
 * Same C-Type format as the In-Interface object. For this type of the
 * interface object, the IPv6 address specifies the IP address to which
 * the outgoing message is going. The ifindex is used to refer to the
 * MIB-II defined local outgoing interface on the PEP.
 */
public class COPSIpv6OutInterface extends COPSIpv6Interface {

    /**
     * Constructor generally used for sending messages
     * @param ifindex - the interface value
     * @param addr - the IPv6 address
     * @throws java.lang.IllegalArgumentException
     */
    public COPSIpv6OutInterface(final COPSIpv6Address addr, final int ifindex) {
        this(new COPSObjHeader(CNum.OUTINTF, CType.STATELESS), addr, ifindex);
    }

    /**
     * Constructor generally used when parsing the bytes of an inbound COPS message but can also be used when the
     * COPSObjHeader information is known
     * @param objHdr - the object header
     * @param ifindex - the interface value
     * @param addr - the IPv6 address
     * @throws java.lang.IllegalArgumentException
     */
    protected COPSIpv6OutInterface(final COPSObjHeader objHdr, final COPSIpv6Address addr, final int ifindex) {
        super(objHdr, addr, ifindex);
    }

    /**
     * Creates this object from a byte array
     * @param objHdrData - the header
     * @param dataPtr - the data to parse
     * @return - a new Timer
     * @throws java.lang.IllegalArgumentException
     */
    public static COPSIpv6OutInterface parse(final COPSObjHeaderData objHdrData, final byte[] dataPtr) {
        return new COPSIpv6OutInterface(objHdrData.header, COPSIpv6Interface.parseAddress(dataPtr),
                COPSIpv6Interface.parseIfIndex(dataPtr));
    }

    public boolean isInInterface() { return false; }
}

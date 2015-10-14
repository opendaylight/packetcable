package org.opendaylight.controller.packetcable.provider;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author c3oe.de, based on snippets from Scott Plante, John Kugelmann
 */
public class Subnet
{
    /** Minimum length of a v4 or v6 subnet mask */
    private final static int MIN_MASK_BITS = 0;

    /** Maximum length of a v4 subnet mask */
    private final static int MAX_MASK_BITS_V4 = 32;

    /** Maximum length of a v6 subnet mask */
    private final static int MAX_MASK_BITS_V6 = 128;

    /** The length of the subnet prefix */
    private final int prefixLen;

    /** The subnet mask */
    private final BigInteger mask;

    /** The actual routing prefix leading to this subnet */
    private final BigInteger routingPrefix;

    /** The number of bytes in an address. Will be 4 or 16 corresponding to ipv4 and ipv6 respectively */
    private final int addressByteCount;

    /**
     * Generates a Subnet from CIDR style notation. <br>
     * Eg. "192.168.0.0/24" or "2001:db8:85a3:880:0:0:0:0/57"
     * @param subnetAddress An address in this subnet or the routing prefix leading to this subnet.
     * @param prefixLength The number of prefix bits that are set, must be between [0..32] for ipv4 and [0..128] for ipv6
     * @throws NullPointerException if the subnetAddress is null
     * @throws IllegalArgumentException if the bits argument is not in the allowable range.
     * @see <a href="http://wikipedia.org/wiki/Classless_Inter-Domain_Routing#CIDR_notation">http://wikipedia.org/wiki/Classless_Inter-Domain_Routing#CIDR_notation</a>
     */
    public Subnet(@Nonnull final InetAddress subnetAddress, final int prefixLength )
    {
        checkNotNull(subnetAddress, "subnetAddress can not be null");
        final int maxMaskBits = (subnetAddress instanceof Inet4Address ? MAX_MASK_BITS_V4 : MAX_MASK_BITS_V6);
        checkArgument(prefixLength >= MIN_MASK_BITS && prefixLength <= maxMaskBits, "The prefixLength must be in range [%s..%s] but was %s", MIN_MASK_BITS, maxMaskBits, prefixLength);

        this.prefixLen = prefixLength;
        this.addressByteCount = subnetAddress.getAddress().length; // 4 or 16
        this.mask = BigInteger.valueOf( -1 ).shiftLeft(this.addressByteCount * 8 - prefixLength);

        // ensure subnetAddress is properly masked and is not an address in the subnet
        this.routingPrefix = new BigInteger( subnetAddress.getAddress() ).and( this.mask);
    }

    /**
     * Generates a subnet from an address and subnet mask. The old ipv4 way.
     * Eg: "192.168.0.0/255.255.255.0" or single address with no subnet
     * @param subnetAddress An address in the subnet or the routing prefix leading to this subnet.
     * @param subnetMask The subnet mask or null. If null the subnet will be a host identifier.
     * */
    public Subnet(@Nonnull final InetAddress subnetAddress, @Nullable final InetAddress subnetMask )
    {
        this(subnetAddress, maskToPrefixLen(subnetAddress, subnetMask));
    }

    /**
     * Helper method that computes the prefix length an address and subnet mask pair.
     * @param subnetAddress An address in the subnet
     * @param subnetMask The mask or null. Expected to be CIDR compliant (mask is a continuous prefix).
     *                   If null this will return the length of a host identifier.
     * @return The prefix length
     * @throws NullPointerException if subnetAddress is null
     * @throws IllegalArgumentException if subnetMask is not null and
     *              either (subnetAddress and subnetMask are not the same type)
     *              or (if subnetMask is not CIDR compliant).
     */
    private static int maskToPrefixLen(@Nonnull final InetAddress subnetAddress, @Nullable final InetAddress subnetMask)
    {
        checkNotNull(subnetAddress, "subnetAddress can not be null");
        if (subnetMask == null) {
            return (subnetAddress instanceof Inet4Address ? MAX_MASK_BITS_V4 : MAX_MASK_BITS_V6);
        }
        else {
            // address and mask must both be ipv4 or ipv6
            checkArgument(subnetAddress.getClass().equals(subnetMask.getClass()));

            // validate subnet mask. All leading bits should be set
            final BigInteger m = new BigInteger(subnetMask.getAddress());
            final BigInteger allOnes = BigInteger.valueOf(-1);
            final int rightMostBit = m.getLowestSetBit();
            final BigInteger validSubnet = allOnes.shiftLeft(rightMostBit);

            checkArgument(validSubnet.equals(m), "Subnet should have contiguous prefix bits, mask: %s(%s)", subnetMask);

            return (subnetAddress instanceof Inet4Address
                    ? MAX_MASK_BITS_V4 - rightMostBit
                    : MAX_MASK_BITS_V6 - rightMostBit);
        }
    }

    /**
     * Subnet factory method.
     * @param addressAndSubnetStr format: "192.168.0.0/24" or "192.168.0.0/255.255.255.0"
     *      or single address or "2001:db8:85a3:880:0:0:0:0/57"
     * @return a new instance
     * @throws UnknownHostException thrown if unsupported subnet mask.
     */
    public static Subnet createInstance(@Nonnull final String addressAndSubnetStr )
            throws UnknownHostException
    {
        final String[] stringArr = addressAndSubnetStr.split("/");
        if ( 2 > stringArr.length ) {
            return new Subnet(InetAddress.getByName(stringArr[0]), null);
        }
        else if ( stringArr[ 1 ].contains(".") || stringArr[ 1 ].contains(":") ) {
            return new Subnet(InetAddress.getByName(stringArr[0]), InetAddress.getByName(stringArr[1]));
        }
        else {
            return new Subnet(InetAddress.getByName(stringArr[0]), Integer.parseInt(stringArr[1]));
        }
    }

    /**
     * Returns the length of the routing prefix length.
     * @return the routing prefix length.
     */
    public int getPrefixLen() {
		return prefixLen;
	}

    /**
     * Determins if the passed in address is contained in this subnet.
     * @param address The address to test.
     * @return true if the address is in this subnet.
     */
	public boolean isInNet(@Nonnull final InetAddress address )
    {
        checkNotNull(address, "address must not be null");

        final byte[] bytesAddress = address.getAddress();
        if ( this.addressByteCount != bytesAddress.length ) {
            return false;
        }
        final BigInteger bigAddress = new BigInteger( bytesAddress );
        return bigAddress.and(this.mask).equals(this.routingPrefix);
    }

    @Override
    final public boolean equals( Object obj )
    {
        if (null == obj) return false;
        if (this == obj) return true;
        if (!(obj instanceof Subnet)) return false;

        final Subnet other = (Subnet)obj;
        return  this.prefixLen == other.prefixLen &&
                this.routingPrefix.equals(other.routingPrefix) &&
                this.mask.equals(other.mask) &&
                this.addressByteCount == other.addressByteCount;
    }

    @Override
    final public int hashCode()
    {
        return new HashCodeBuilder(997, 311)
                .append(prefixLen)
                .append(mask)
                .append(routingPrefix)
                .append(addressByteCount)
                .build();
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        bigInteger2IpString( buf, this.routingPrefix, this.addressByteCount);
        buf.append( '/' );
        bigInteger2IpString( buf, this.mask, this.addressByteCount);
        return buf.toString();
    }

    static private void bigInteger2IpString( final StringBuilder buf, final BigInteger bigInteger, final int displayBytes )
    {
        final boolean isIPv4 = 4 == displayBytes;
        byte[] bytes = bigInteger.toByteArray();
        int diffLen = displayBytes - bytes.length;
        final byte fillByte = 0 > (int)bytes[ 0 ] ? (byte)0xFF : (byte)0x00;

        int integer;
        for ( int i = 0; i < displayBytes; i++ )
        {
            if ( 0 < i && ! isIPv4 && i % 2 == 0 ) {
                buf.append(':');
            }
            else if ( 0 < i && isIPv4 ) {
                buf.append('.');
            }
            integer = 0xFF & (i < diffLen ? fillByte : bytes[ i - diffLen ]);
            if ( ! isIPv4 && 0x10 > integer ) {
                buf.append('0');
            }
            buf.append( isIPv4 ? integer : Integer.toHexString( integer ) );
        }
    }
}
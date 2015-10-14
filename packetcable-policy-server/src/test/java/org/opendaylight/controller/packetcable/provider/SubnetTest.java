package org.opendaylight.controller.packetcable.provider;

import com.google.common.net.InetAddresses;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SubnetTest {

    // Various address class level prefix lengths
    private static final int CLASS_A = 8;
    private static final int CLASS_B = 16;
    private static final int CLASS_C = 24;

    private InetAddress addressV4 = null;
    private InetAddress addressV6 = null;

    private InetAddress v4MaskClassA;
    private InetAddress v4MaskClassB;
    private InetAddress v4MaskClassC;

    @Before
    public final void setup()
    {
        addressV4 = InetAddresses.forString("192.168.0.1");
        addressV6 = InetAddresses.forString("2001:db8::1");

        v4MaskClassA = InetAddresses.forString("255.0.0.0");
        v4MaskClassB = InetAddresses.forString("255.255.0.0");
        v4MaskClassC = InetAddresses.forString("255.255.255.0");
    }

    @After
    public final void tearDown()
    {
        addressV4 = null;
        addressV6 = null;

        v4MaskClassA = null;
        v4MaskClassB = null;
        v4MaskClassC = null;
    }

    @Test
    public final void testCtorCidr()
    {
        assertThat(new Subnet(addressV4, 24), is(notNullValue()));
        assertThat(new Subnet(addressV6, 24), is(notNullValue()));
    }

    @Test
    public final void testCtorAddressAndMask()
    {
        assertThat(new Subnet(addressV4, InetAddresses.forString("255.255.255.0")), is(notNullValue()));
        assertThat(new Subnet(addressV4, null), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSubnetCtorCidrBadMaskv4_1()
    {
        new Subnet(addressV4, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSubnetCtorCidrBadMaskv4_2()
    {
        new Subnet(addressV4, 33);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSubnetCtorCidrBadMaskv6_1()
    {
        new Subnet(addressV6, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSubnetCtorCidrBadMaskv6_2()
    {
        new Subnet(addressV6, 129);
    }

    @Test
    public final void testGetPrefixLen()
    {
        Subnet subnet = new Subnet(addressV4, CLASS_C);
        assertThat(subnet, is(notNullValue()) );
        assertThat(subnet.getPrefixLen(), is(CLASS_C) );

        subnet = new Subnet(addressV4, CLASS_B);
        assertThat(subnet, is(notNullValue()) );
        assertThat(subnet.getPrefixLen(), is(CLASS_B) );

        subnet = new Subnet(addressV4, CLASS_A);
        assertThat(subnet, is(notNullValue()) );
        assertThat(subnet.getPrefixLen(), is(CLASS_A) );

        subnet = new Subnet(addressV4, v4MaskClassC);
        assertThat(subnet, is(notNullValue()) );
        assertThat(subnet.getPrefixLen(), is(CLASS_C) );

        subnet = new Subnet(addressV4, v4MaskClassB);
        assertThat(subnet, is(notNullValue()) );
        assertThat(subnet.getPrefixLen(), is(CLASS_B) );

        subnet = new Subnet(addressV4, v4MaskClassA);
        assertThat(subnet, is(notNullValue()) );
        assertThat(subnet.getPrefixLen(), is(CLASS_A) );

    }

    @Test
    public final void testSubnetCtorCidrAllValidMasks()
    {
        // validating no exceptions are thrown for valid masks
        for (int iMask = 0; iMask < 33; ++iMask) {
            final Subnet subnet = new Subnet(addressV4, iMask);
            assertThat(subnet.getPrefixLen(),  is(iMask) );
        }

        for (int iMask = 0; iMask < 129; ++iMask) {
            final Subnet subnet = new Subnet(addressV6, iMask);
            assertThat(subnet.getPrefixLen(),  is(iMask) );
        }
    }


    @Test
    public final void testEquals()
    {
        final Subnet s41 = new Subnet(addressV4, 24);
        final Subnet s42 = new Subnet(addressV4, 24);
        final Subnet s43 = new Subnet(addressV4, 16);

        final Subnet s61 = new Subnet(addressV6, 24);
        final Subnet s62 = new Subnet(addressV6, 56);
        final Subnet s63 = new Subnet(addressV6, 56);

        assertThat(s41, is(equalTo(s41)));
        assertThat(s41, is(equalTo(s42)));
        assertThat(s41, is(not(equalTo(s43))));
        assertThat(s41, is(not(equalTo(s61))));
        assertThat(s41, is(not(equalTo(s62))));
        assertThat(s41, is(not(equalTo(s63))));
        assertThat(s41, is(not(equalTo(null))));
        assertNotEquals(s41, "random string");

        assertThat(s42, is(equalTo(s41)));
        assertThat(s42, is(equalTo(s42)));
        assertThat(s42, is(not(equalTo(s43))));
        assertThat(s42, is(not(equalTo(s61))));
        assertThat(s42, is(not(equalTo(s62))));
        assertThat(s42, is(not(equalTo(s63))));
        assertThat(s42, is(not(equalTo(null))));
        assertNotEquals(s42, "random string");

        assertThat(s43, is(not(equalTo(s41))));
        assertThat(s43, is(not(equalTo(s42))));
        assertThat(s43, is(equalTo(s43)));
        assertThat(s43, is(not(equalTo(s61))));
        assertThat(s43, is(not(equalTo(s62))));
        assertThat(s43, is(not(equalTo(s63))));
        assertThat(s43, is(not(equalTo(null))));
        assertNotEquals(s43, "random string");

        assertThat(s61, is(not(equalTo(s41))));
        assertThat(s61, is(not(equalTo(s42))));
        assertThat(s61, is(not(equalTo(s43))));
        assertThat(s61, is(equalTo(s61)));
        assertThat(s61, is(not(equalTo(s62))));
        assertThat(s61, is(not(equalTo(s63))));
        assertThat(s61, is(not(equalTo(null))));
        assertNotEquals(s61, "random string");

        assertThat(s62, is(not(equalTo(s41))));
        assertThat(s62, is(not(equalTo(s42))));
        assertThat(s62, is(not(equalTo(s43))));
        assertThat(s62, is(not(equalTo(s61))));
        assertThat(s62, is(equalTo(s62)));
        assertThat(s62, is(equalTo(s63)));
        assertThat(s62, is(not(equalTo(null))));
        assertNotEquals(s62, "random string");

        assertThat(s63, is(not(equalTo(s41))));
        assertThat(s63, is(not(equalTo(s42))));
        assertThat(s63, is(not(equalTo(s43))));
        assertThat(s63, is(not(equalTo(s61))));
        assertThat(s63, is(equalTo(s62)));
        assertThat(s63, is(equalTo(s63)));
        assertThat(s63, is(not(equalTo(null))));
        assertNotEquals(s63, "random string");
    }

    @Test
    public final void testHashcode()
    {
        final Subnet s41 = new Subnet(addressV4, 24);
        final Subnet s42 = new Subnet(addressV4, 24);
        final Subnet s43 = new Subnet(addressV4, 16);

        final Subnet s61 = new Subnet(addressV6, 24);
        final Subnet s62 = new Subnet(addressV6, 56);
        final Subnet s63 = new Subnet(addressV6, 56);

        assertThat(s41.hashCode(), is(equalTo(s41.hashCode())));
        assertThat(s41.hashCode(), is(equalTo(s42.hashCode())));
        assertThat(s41.hashCode(), is(not(equalTo(s43.hashCode()))));
        assertThat(s41.hashCode(), is(not(equalTo(s61.hashCode()))));
        assertThat(s41.hashCode(), is(not(equalTo(s62.hashCode()))));
        assertThat(s41.hashCode(), is(not(equalTo(s63.hashCode()))));

        assertThat(s42.hashCode(), is(equalTo(s41.hashCode())));
        assertThat(s42.hashCode(), is(equalTo(s42.hashCode())));
        assertThat(s42.hashCode(), is(not(equalTo(s43.hashCode()))));
        assertThat(s42.hashCode(), is(not(equalTo(s61.hashCode()))));
        assertThat(s42.hashCode(), is(not(equalTo(s62.hashCode()))));
        assertThat(s42.hashCode(), is(not(equalTo(s63.hashCode()))));

        assertThat(s43.hashCode(), is(not(equalTo(s41.hashCode()))));
        assertThat(s43.hashCode(), is(not(equalTo(s42.hashCode()))));
        assertThat(s43.hashCode(), is(equalTo(s43.hashCode())));
        assertThat(s43.hashCode(), is(not(equalTo(s61.hashCode()))));
        assertThat(s43.hashCode(), is(not(equalTo(s62.hashCode()))));
        assertThat(s43.hashCode(), is(not(equalTo(s63.hashCode()))));

        assertThat(s61.hashCode(), is(not(equalTo(s41.hashCode()))));
        assertThat(s61.hashCode(), is(not(equalTo(s42.hashCode()))));
        assertThat(s61.hashCode(), is(not(equalTo(s43.hashCode()))));
        assertThat(s61.hashCode(), is(equalTo(s61.hashCode())));
        assertThat(s61.hashCode(), is(not(equalTo(s62.hashCode()))));
        assertThat(s61.hashCode(), is(not(equalTo(s63.hashCode()))));

        assertThat(s62.hashCode(), is(not(equalTo(s41.hashCode()))));
        assertThat(s62.hashCode(), is(not(equalTo(s42.hashCode()))));
        assertThat(s62.hashCode(), is(not(equalTo(s43.hashCode()))));
        assertThat(s62.hashCode(), is(not(equalTo(s61.hashCode()))));
        assertThat(s62.hashCode(), is(equalTo(s62.hashCode())));
        assertThat(s62.hashCode(), is(equalTo(s63.hashCode())));

        assertThat(s63.hashCode(), is(not(equalTo(s41.hashCode()))));
        assertThat(s63.hashCode(), is(not(equalTo(s42.hashCode()))));
        assertThat(s63.hashCode(), is(not(equalTo(s43.hashCode()))));
        assertThat(s63.hashCode(), is(not(equalTo(s61.hashCode()))));
        assertThat(s63.hashCode(), is(equalTo(s62.hashCode())));
        assertThat(s63.hashCode(), is(equalTo(s63.hashCode())));
    }

    @Test
    public final void testCreateInstance() throws UnknownHostException {
        Subnet subnet = Subnet.createInstance("192.168.0.0/255.255.255.0");
        assertThat(subnet, is(notNullValue(Subnet.class)));
        assertThat(subnet.toString(), is("192.168.0.0/255.255.255.0"));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.0/255.255.255.0"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.0/24"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.10/255.255.255.0"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.10/24"))));

        subnet = Subnet.createInstance("192.168.0.1");
        assertThat(subnet, is(notNullValue(Subnet.class)));
        assertThat(subnet.toString(), is("192.168.0.1/255.255.255.255"));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.1"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.1/32"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.1/255.255.255.255"))));

        subnet = Subnet.createInstance("192.168.0.0/8");
        assertThat(subnet, is(notNullValue(Subnet.class)));
        assertThat(subnet.toString(), is("192.0.0.0/255.0.0.0"));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.168.0.0/8"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.175.0.10/8"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.10.0.0/255.0.0.0"))));
        assertThat(subnet, is(equalTo(Subnet.createInstance("192.1.0.100/255.0.0.0"))));

    }

    @Test
    public final void testIsInNet()
    {
        Subnet subnet = new Subnet(addressV4, CLASS_C);
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.1")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.100")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.168.1.1")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.167.0.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("191.168.0.1")));
        assertFalse(subnet.isInNet(addressV6));

        subnet = new Subnet(addressV4, CLASS_B);
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.1")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.100")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.255")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.1.1")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.1.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.167.0.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("191.168.0.1")));
        assertFalse(subnet.isInNet(addressV6));

        subnet = new Subnet(addressV4, CLASS_A);
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.1")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.100")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.255")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.1.1")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.1.255")));
        assertTrue(subnet.isInNet(InetAddresses.forString("192.167.0.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("191.168.0.1")));
        assertFalse(subnet.isInNet(addressV6));

        subnet = new Subnet(addressV4, 32);
        assertTrue(subnet.isInNet(InetAddresses.forString("192.168.0.1")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.168.0.100")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.168.0.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.168.1.1")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.168.1.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("192.167.0.255")));
        assertFalse(subnet.isInNet(InetAddresses.forString("191.168.0.1")));
        assertFalse(subnet.isInNet(addressV6));

        subnet = new Subnet(addressV6, 64);
        assertTrue(subnet.isInNet(InetAddresses.forString("2001:db8::1")));
        assertTrue(subnet.isInNet(InetAddresses.forString("2001:db8::100")));
        assertTrue(subnet.isInNet(InetAddresses.forString("2001:db8::ffff")));
        assertFalse(subnet.isInNet(InetAddresses.forString("2001:db7::1")));
        assertFalse(subnet.isInNet(InetAddresses.forString("2000:db8::")));
        assertTrue(subnet.isInNet(InetAddresses.forString("2001:db8:0:0:1::1")));
        assertFalse(subnet.isInNet(addressV4));

    }
}

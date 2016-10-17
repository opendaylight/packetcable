/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.test.rules.Params;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.ccap.attributes.ConnectionBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class ConnectionValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private ConnectionValidator validator = new ConnectionValidator();

    @Test(expected = ValidationException.class)
    public void testNullConnection() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void testNullExtent() throws ValidationException {
        validator.validate(null, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullIpAddress() throws ValidationException {
        final Connection connection = new ConnectionBuilder(buildValidConnectionTree())
                .setIpAddress(null)
                .build();

        validator.validate(connection, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNullPortNumber() throws ValidationException {
        final Connection connection = new ConnectionBuilder(buildValidConnectionTree())
                .setPort(null)
                .build();

        validator.validate(connection, extentParams.getCurrentParam());
    }

    @Test
    public void testValid() throws ValidationException {
        Connection connection = buildValidConnectionTree();
        validator.validate(connection, extentParams.getCurrentParam());
    }

    public static Connection buildValidConnectionTree() {
        return new ConnectionBuilder()
                .setIpAddress(new IpAddress(new Ipv4Address("192.168.1.100")))
                .setPort(new PortNumber(6500))
                .build();
    }
}

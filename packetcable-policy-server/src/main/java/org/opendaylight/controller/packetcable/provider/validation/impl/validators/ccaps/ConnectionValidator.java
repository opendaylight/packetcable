/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.ccap.attributes.Connection;

/**
 * @author rvail
 */
public class ConnectionValidator extends AbstractValidator<Connection> {

    private static final String IP_ADDRESS = "connection.ipAddress";
    private static final String PORT = "connection.port";

    @Override
    public void validate(final Connection connection, Extent extent) throws ValidationException {
        if (connection == null) {
            throw new ValidationException("connection must exist");
        }

        mustExist(connection.getIpAddress(), IP_ADDRESS);

        // Note PortNumber validates range on creation so only existence needs to be checked
        mustExist(connection.getPort(), PORT);


        throwErrorsIfNeeded();
    }
}

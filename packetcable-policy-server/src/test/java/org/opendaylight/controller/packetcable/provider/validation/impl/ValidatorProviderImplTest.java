/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.ConnectionValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.ConnectionValidatorTest;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.attributes.Connection;

/**
 * @author rvail
 */
public class ValidatorProviderImplTest {

    private final ValidatorProviderImpl validatorProvider = new ValidatorProviderImpl();

    @Test
    public void testPutAndGetValidator() {
        final ConnectionValidator validator = new ConnectionValidator();
        validatorProvider.put(Connection.class, validator);
        assertNotNull(validatorProvider.validatorFor(Connection.class));
        assertSame(validator, validatorProvider.validatorFor(Connection.class));
    }

    @Test
    public void testValidate() throws ValidationException {
        final ConnectionValidator validator = new ConnectionValidator();
        validatorProvider.put(Connection.class, validator);
        validatorProvider.validate(Connection.class, ConnectionValidatorTest.buildValidConnectionTree(),
                Validator.Extent.NODE_ONLY);
    }

}

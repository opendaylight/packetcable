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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.ccap.attributes.AmIdBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class AmIdValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final AmIdValidator validator = new AmIdValidator();

    @Test
    public void testValid() throws Exception {
        AmId amId = new AmIdBuilder()
                .setAmTag(1)
                .setAmType(1)
                .build();

        // this throw an exception if invalid
        validator.validate(amId, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNoAmTag() throws Exception {
        AmId amId = new AmIdBuilder()
                .setAmType(1)
                .build();

        // this throw an exception if invalid
        validator.validate(amId, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNoAmType() throws Exception {
        AmId amId = new AmIdBuilder()
                .setAmTag(1)
                .build();

        // this throw an exception if invalid
        validator.validate(amId, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNullAmId() throws Exception {
        // this throw an exception if invalid
        validator.validate(null, extentParams.getCurrentParam());
    }

    public static AmId buildValidAmIdTree() {
        return new AmIdBuilder()
                .setAmType(1)
                .setAmTag(2)
                .build();
    }
}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.test.rules.Params;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ccaps.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.ccaps.CcapBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class CcapValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final CcapValidator validator = new CcapValidator();

    @Test(expected = ValidationException.class)
    public void testNullCcap() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void testNullExtent() throws ValidationException {
        validator.validate(buildValidCcapTree(), null);
    }

    @Test(expected = ValidationException.class)
    public void testNullCcapId() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                // key is based off of CcapId so clear that too
                .setKey(null)
                .setCcapId(null)
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNullDownstreamScns() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                .setDownstreamScns(null)
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testEmptyDownstreamScns() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                .setDownstreamScns(Collections.<ServiceClassName>emptyList())
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNullUpstreamScns() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                .setUpstreamScns(null)
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testEmptyUpstreamScns() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                .setUpstreamScns(Collections.<ServiceClassName>emptyList())
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNullAmId() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                .setAmId(null)
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void testNullConnection() throws ValidationException {
        final Ccap ccap = new CcapBuilder(buildValidCcapTree())
                .setConnection(null)
                .build();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    @Test
    public void testValid() throws ValidationException {
        final Ccap ccap = buildValidCcapTree();

        validator.validate(ccap, extentParams.getCurrentParam());
    }

    public static Ccap buildValidCcapTree() {
        return new CcapBuilder()
                .setCcapId("aCcapId")
                .setDownstreamScns(Collections.singletonList(new ServiceClassName("down_scn")))
                .setUpstreamScns(Collections.singletonList(new ServiceClassName("up_scn")))
                .setAmId(AmIdValidatorTest.buildValidAmIdTree())
                .setConnection(ConnectionValidatorTest.buildValidConnectionTree())
                .build();
    }
}

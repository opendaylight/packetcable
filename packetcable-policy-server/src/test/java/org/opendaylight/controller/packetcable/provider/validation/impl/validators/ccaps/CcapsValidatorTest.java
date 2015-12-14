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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.CcapsBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.ccaps.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.ccaps.CcapBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class CcapsValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final CcapsValidator validator = new CcapsValidator();

    @Test(expected = ValidationException.class)
    public void testNullCcaps() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void testNullExtent() throws ValidationException {
        validator.validate(buildValidCcapsTree(), null);
    }

    @Test
    public void testNullListValid() throws ValidationException {
        final Ccaps ccaps = new CcapsBuilder()
                .setCcap(null)
                .build();

        validator.validate(ccaps, extentParams.getCurrentParam());
    }

    @Test
    public void testEmptyListValid() throws ValidationException {
        final Ccaps ccaps = new CcapsBuilder()
                .setCcap(Collections.<Ccap>emptyList())
                .build();

        validator.validate(ccaps, extentParams.getCurrentParam());
    }

    @Test
    public void testValid() throws ValidationException {
        final Ccap ccap = new CcapBuilder().setCcapId("aCcapId").build();

        final Ccaps ccaps = new CcapsBuilder()
                .setCcap(Collections.singletonList(ccap))
                .build();

        validator.validate(ccaps, extentParams.getCurrentParam());
    }

    public static Ccaps buildValidCcapsTree() {
        final Ccap ccap = CcapValidatorTest.buildValidCcapTree();
        return new CcapsBuilder()
                .setCcap(Collections.singletonList(ccap))
                .build();
    }


}

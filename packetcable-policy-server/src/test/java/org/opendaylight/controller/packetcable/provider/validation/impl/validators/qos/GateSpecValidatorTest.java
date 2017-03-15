/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.test.rules.Params;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.gate.spec.GateSpecBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class GateSpecValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final GateSpecValidator validator = new GateSpecValidator();

    @Test(expected = ValidationException.class)
    public void nullGateSpec() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidGateSpec(), null);
    }

    @Test
    public void nullDirection() throws ValidationException {
        // optional expect no exception
        GateSpec gateSpec = new GateSpecBuilder(buildValidGateSpec())
                .setDirection(null)
                .build();
        validator.validate(gateSpec, extentParams.getCurrentParam());
    }

    @Test
    public void nullDscpTosMask() throws ValidationException {
        // optional expect no exception
        GateSpec gateSpec = new GateSpecBuilder(buildValidGateSpec())
                .setDscpTosMask(null)
                .build();
        validator.validate(gateSpec, extentParams.getCurrentParam());
    }

    @Test
    public void nullDscpTosOverwrite() throws ValidationException {
        // optional expect no exception
        GateSpec gateSpec = new GateSpecBuilder(buildValidGateSpec())
                .setDscpTosOverwrite(null)
                .build();
        validator.validate(gateSpec, extentParams.getCurrentParam());
    }

    @Test
     public void valid() throws ValidationException {
        validator.validate(buildValidGateSpec(), extentParams.getCurrentParam());
    }

    public static GateSpec buildValidGateSpec() {
        return new GateSpecBuilder()
                .setDirection(ServiceFlowDirection.Us)
                .setDscpTosMask(new TosByte((short) 0xff))
                .setDscpTosOverwrite(new TosByte((short) 0xa0))
                .setInactivityTimer(new Long(900))
                .setSessionClassId(new Short((short)38))
                .build();
    }

}

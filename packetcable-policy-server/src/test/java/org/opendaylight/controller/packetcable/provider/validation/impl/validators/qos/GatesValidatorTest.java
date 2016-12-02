/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.test.rules.Params;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.gates.apps.app.subscribers.subscriber.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.gates.apps.app.subscribers.subscriber.GatesBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class GatesValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final GatesValidator validator = new GatesValidator();

    @Test(expected = ValidationException.class)
    public void nullGates() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidGates(), null);
    }

    @Test
    public void valid() throws ValidationException {
        validator.validate(buildValidGates(), extentParams.getCurrentParam());
    }

    public static Gates buildValidGates() {
        return new GatesBuilder()
                .setGate(Collections.singletonList(GateValidatorTest.buildValidGate()))
                .build();
    }
}

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
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ClassifiersValidatorTest;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161219.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161219.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.GateBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class GateValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final GateValidator validator = new GateValidator();

    @Test(expected = ValidationException.class)
    public void nullGate() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidGate(), null);
    }

    @Test(expected = ValidationException.class)
    public void nullGateId() throws ValidationException {
        Gate gate = new GateBuilder(buildValidGate()).setKey(null).setGateId(null).build();
        validator.validate(gate, extentParams.getCurrentParam());
    }

    @Test
    public void nullGateSpec() throws ValidationException {
        // gate spec is all optional, no exception expected
        Gate gate = new GateBuilder(buildValidGate()).setGateSpec(null).build();
        validator.validate(gate, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullTrafficProfile() throws ValidationException {
        Gate gate = new GateBuilder(buildValidGate()).setTrafficProfile(null).build();
        validator.validate(gate, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullClassifiers() throws ValidationException {
        Gate gate = new GateBuilder(buildValidGate()).setClassifiers(null).build();
        validator.validate(gate, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        validator.validate(buildValidGate(), extentParams.getCurrentParam());
    }


    public static Gate buildValidGate() {
        return new GateBuilder()
                .setGateId("unit-test-gate-id")
                .setGateSpec(GateSpecValidatorTest.buildValidGateSpec())
                .setTrafficProfile(TrafficProfileValidatorTest.buildValidTrafficProfile())
                .setClassifiers(ClassifiersValidatorTest.buildValidClassifiers())
                .build();
    }
}

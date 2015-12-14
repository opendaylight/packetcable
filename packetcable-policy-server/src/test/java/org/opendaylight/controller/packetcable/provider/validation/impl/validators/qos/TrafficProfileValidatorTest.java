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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.traffic.profile.TrafficProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.traffic.profile.TrafficProfileBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class TrafficProfileValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final TrafficProfileValidator validator = new TrafficProfileValidator();

    @Test(expected = ValidationException.class)
    public void nullTrafficProfile() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidTrafficProfile(), null);
    }

    @Test
    public void nullServiceClassName() throws ValidationException {
        final TrafficProfile trafficProfile = new TrafficProfileBuilder(buildValidTrafficProfile())
                .setServiceClassName(null)
                .build();
        validator.validate(buildValidTrafficProfile(), extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        validator.validate(buildValidTrafficProfile(), extentParams.getCurrentParam());
    }


    public static TrafficProfile buildValidTrafficProfile() {
        return new TrafficProfileBuilder()
                .setServiceClassName(new ServiceClassName("unit-test-scn"))
                .build();
    }
}

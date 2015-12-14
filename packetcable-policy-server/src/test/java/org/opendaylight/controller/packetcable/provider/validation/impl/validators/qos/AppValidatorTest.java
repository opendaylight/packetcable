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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.gates.apps.App;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.gates.apps.AppBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class AppValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final AppValidator validator = new AppValidator();

    @Test(expected = ValidationException.class)
    public void nullSubscribers() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidApp(), null);
    }

    @Test(expected = ValidationException.class)
    public void nullAppId() throws ValidationException {
        final App app = new AppBuilder(buildValidApp())
                .setAppId(null).setKey(null)
                .build();

        validator.validate(app, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        validator.validate(buildValidApp(), extentParams.getCurrentParam());
    }

    public static App buildValidApp() {
        return new AppBuilder()
                .setAppId("unit-test-app")
                .setSubscribers(SubscribersValidatorTest.buildValidSubscriber())
                .build();
    }

}

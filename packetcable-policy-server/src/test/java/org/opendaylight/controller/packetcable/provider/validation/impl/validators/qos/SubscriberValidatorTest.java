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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.Subscriber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.SubscriberBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class SubscriberValidatorTest {


    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final SubscriberValidator validator = new SubscriberValidator();

    @Test(expected = ValidationException.class)
    public void nullSubscriber() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidSubscriber(), null);
    }

    @Test(expected = ValidationException.class)
    public void nullSubscriberId() throws ValidationException {
        final Subscriber subscriber = new SubscriberBuilder(buildValidSubscriber())
                .setSubscriberId(null).setKey(null)
                .build();
        validator.validate(subscriber, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullGates() throws ValidationException {
        final Subscriber subscriber = new SubscriberBuilder(buildValidSubscriber())
                .setGates(null)
                .build();
        validator.validate(subscriber, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        validator.validate(buildValidSubscriber(), extentParams.getCurrentParam());
    }

    public static Subscriber buildValidSubscriber() {
        return new SubscriberBuilder()
                .setSubscriberId("76.76.76.76")
                .setGates(GatesValidatorTest.buildValidGates())
                .build();
    }
}

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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.Subscribers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.SubscribersBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class SubscribersValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final SubscribersValidator validator = new SubscribersValidator();

    @Test(expected = ValidationException.class)
    public void nullSubscribers() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidSubscriber(), null);
    }

    @Test
    public void valid() throws ValidationException {
        validator.validate(buildValidSubscriber(), extentParams.getCurrentParam());
    }

    public static Subscribers buildValidSubscriber() {
        return new SubscribersBuilder()
                .setSubscriber(Collections.singletonList(SubscriberValidatorTest.buildValidSubscriber()))
                .build();
    }

}

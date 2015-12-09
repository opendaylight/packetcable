/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.gates.apps.app.subscribers.Subscriber;

/**
 * @author rvail
 */
public class SubscriberValidator extends AbstractValidator<Subscriber> {

    private static final String SUBSCRIBER_ID = "subscriber.subscriberId";
    private static final String GATES = "subscriber.gates";

    private final GatesValidator gatesValidator = new GatesValidator();

    @Override
    protected void doValidate(final Subscriber subscriber, final Extent extent) {
        if (subscriber == null) {
            getErrorMessages().add("subscriber must exist");
            return;
        }

        mustExist(subscriber.getSubscriberId(), SUBSCRIBER_ID);
        mustExist(subscriber.getGates(), GATES);

        if (extent == Extent.NODE_AND_SUBTREE) {
            validateChild(gatesValidator, subscriber.getGates());
        }
    }
}

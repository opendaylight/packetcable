/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.gates.apps.app.Subscribers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.gates.apps.app.subscribers.Subscriber;

/**
 * @author rvail
 */
public class SubscribersValidator extends AbstractValidator<Subscribers> {

    private final SubscriberValidator subscriberValidator = new SubscriberValidator();

    @Override
    public void validate(final Subscribers subscribers, final Extent extent) throws ValidationException {
        if (subscribers == null) {
            throw new ValidationException("subscribers must exist");
        }

        if (extent == Extent.NODE_AND_SUBTREE) {
            for (Subscriber subscriber : subscribers.getSubscriber()) {
                validateChild(subscriberValidator , subscriber);
            }
        }

        throwErrorsIfNeeded();
    }

}

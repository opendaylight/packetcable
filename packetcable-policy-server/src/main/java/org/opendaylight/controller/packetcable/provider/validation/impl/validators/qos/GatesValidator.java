/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.subscriber.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;

/**
 * @author rvail
 */
public class GatesValidator extends AbstractValidator<Gates> {

    private final GateValidator gateValidator = new GateValidator();

    @Override
    protected void doValidate(final Gates gates, final Extent extent) {
        if (gates == null) {
            getErrorMessages().add("gates must exist");
            return;
        }

        if (extent == Extent.NODE_AND_SUBTREE) {
            if (gates.getGate() != null) {
                for (Gate gate : gates.getGate()) {
                    validateChild(gateValidator, gate);
                }
            }
        }
    }
}

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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.gate.spec.GateSpec;

/**
 * @author rvail
 */
public class GateSpecValidatator extends AbstractValidator<GateSpec> {

    private static final String DIRECTION = "gate-spec.direction";

    @Override
    public void validate(final GateSpec gateSpec, final Extent extent) throws ValidationException {
        if (gateSpec == null) {
            throw new ValidationException("gate-spec must exist");
        }

        // everything is optional

//        mustExist(gateSpec.getDirection(), DIRECTION);
//
//        // dscp-tos-overwrite & dscp-tos-mask are optional
        throwErrorsIfNeeded();
    }

}

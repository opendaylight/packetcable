/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.ccaps.Ccap;

/**
 * @author rvail
 */
public class CcapsValidator extends AbstractValidator<Ccaps> {

    private final CcapValidator ccapValidator = new CcapValidator();

    @Override
    public void validate(final Ccaps ccaps, Extent extent) throws ValidationException {
        if (ccaps == null) {
            throw new ValidationException("ccaps must exist");
        }

        if (extent == Extent.NODE_AND_SUBTREE) {
            for (Ccap ccap : ccaps.getCcap()) {
                validateChild(ccapValidator, ccap);
            }
        }
    }

}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccaps.Ccap;

/**
 * @author rvail
 */
public class CcapsValidator extends AbstractValidator<Ccaps> {

    private final CcapValidator ccapValidator = new CcapValidator();

    @Override
    public void doValidate(final Ccaps ccaps, Extent extent) {
        if (ccaps == null) {
            getErrorMessages().add("ccaps must exist");
            return;
        }

        if (extent == Extent.NODE_AND_SUBTREE) {
            if (ccaps.getCcap() != null) {
                for (Ccap ccap : ccaps.getCcap()) {
                    validateChild(ccapValidator, ccap);
                }
            }
        }
    }

}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.ccap.attributes.AmId;

/**
 * @author rvail
 */
public class AmIdValidator extends AbstractValidator<AmId> {

    private static final String AM_TYPE = "amId.am-type";
    private static final String AM_TAG = "amId.am-tag";

    @Override
    protected void doValidate(final AmId amId, Extent extent) {
        if (amId == null) {
            getErrorMessages().add("amId must exist");
            return;
        }

        mustExist(amId.getAmTag(), AM_TAG);
        mustExist(amId.getAmType(), AM_TYPE);
    }
}

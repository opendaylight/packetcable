/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.gates.Apps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.gates.apps.App;

/**
 * @author rvail
 */
public class AppsValidator extends AbstractValidator<Apps> {

    private final AppValidator appValidator = new AppValidator();

    @Override
    protected void doValidate(final Apps apps, final Extent extent) {
        if (apps == null) {
            getErrorMessages().add("apps must exist");
            return;
        }
        if (extent == Extent.NODE_AND_SUBTREE) {
            for (App app : apps.getApp()) {
                validateChild(appValidator, app);
            }
        }
    }

}

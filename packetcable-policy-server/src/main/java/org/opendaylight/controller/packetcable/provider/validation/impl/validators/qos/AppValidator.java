/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.App;

/**
 * @author rvail
 */
public class AppValidator extends AbstractValidator<App> {

    private static final String APP_ID = "app.appId";
    private static final String SUBSCRIBERS = "app.subscribers";

    private final SubscribersValidator subscribersValidator = new SubscribersValidator();

    @Override
    protected void doValidate(final App app, final Extent extent) {
        if (app == null) {
            getErrorMessages().add("app must exist");
            return;
        }

        mustExist(app.getAppId(), APP_ID);
        mustExist(app.getSubscribers(), SUBSCRIBERS);

        if (extent == Extent.NODE_AND_SUBTREE) {
            validateChild(subscribersValidator, app.getSubscribers());
        }
    }
}

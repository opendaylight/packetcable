/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.traffic.profile.TrafficProfile;

/**
 * @author rvail
 */
public class TrafficProfileValidator extends AbstractValidator<TrafficProfile> {

    private static final String SCN = "service-class-name";

    @Override
    protected void doValidate(final TrafficProfile trafficProfile, final Extent extent) {
       if (trafficProfile == null) {
           getErrorMessages().add("traffic-profile must exist");
           return;
       }

        mustExist(trafficProfile.getServiceClassName(), SCN);

    }

}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.traffic.profile.TrafficProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.traffic.profile.traffic.profile.TrafficProfileChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.FlowSpecChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.ServiceClassNameChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.RtpChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.UgsChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.flow.spec.profile.FlowSpecProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.pcmm.serviceclass.name.profile.ServiceClassNameProfile;

/**
 * @author rvail
 */
public class TrafficProfileValidator extends AbstractValidator<TrafficProfile> {

    private static final String FS = "flow-spec-profile";
    private static final String SCN = "service-class-name";
    private static final String SCP = "service-class-name-profile";
    private static final String UGS = "ugs-profile";
    private static final String RTP = "rtp-profile";

    @Override
    protected void doValidate(final TrafficProfile trafficProfile, final Extent extent) {
        if (trafficProfile == null) {
           getErrorMessages().add("traffic-profile must exist");
           return;
        }
        if (trafficProfile.getTrafficProfileChoice() instanceof ServiceClassNameChoice) {
            mustExist(((ServiceClassNameChoice)trafficProfile.getTrafficProfileChoice()).getServiceClassNameProfile(), SCP);
            mustExist(((ServiceClassNameChoice)trafficProfile.getTrafficProfileChoice()).getServiceClassNameProfile().getServiceClassName(), SCN);
        } else if (trafficProfile.getTrafficProfileChoice() instanceof FlowSpecChoice) {
            mustExist(((FlowSpecChoice)trafficProfile.getTrafficProfileChoice()).getFlowSpecProfile(), SCP);
        } else if (trafficProfile.getTrafficProfileChoice() instanceof RtpChoice) {
            mustExist(((RtpChoice)trafficProfile.getTrafficProfileChoice()).getRtpProfile(), RTP);
        } else if (trafficProfile.getTrafficProfileChoice() instanceof UgsChoice) {
            mustExist(((UgsChoice)trafficProfile.getTrafficProfileChoice()).getUgsProfile(), UGS);
        } else {
           getErrorMessages().add("Unknown traffic profile");
           return;
        }
    }

}

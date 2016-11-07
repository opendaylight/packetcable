/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ClassifiersValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;

/**
 * @author rvail
 */
public class GateValidator extends AbstractValidator<Gate> {

    private static final String GATE_ID = "gate.gateId";
    private static final String GATE_SPEC = "gate.gate-spec";
    private static final String TRAFFIC_PROFILE = "gate.traffic-profile";
    private static final String CLASSIFIERS = "gate.classifiers";

//    private final GateSpecValidator gateSpecValidatator = new GateSpecValidator();
    private final TrafficProfileValidator trafficProfileValidator = new TrafficProfileValidator();
    private final ClassifiersValidator classifiersValidator = new ClassifiersValidator();

    @Override
    protected void doValidate(final Gate gate, final Extent extent) {
        if (gate == null) {
            getErrorMessages().add("gate must exist");
            return;
        }

        mustExist(gate.getGateId(), GATE_ID);

        // all leafs in GateSpec are optional
        // mustExist(gate.getGateSpec(), GATE_SPEC);

        // Classifiers
        mustExist(gate.getClassifiers(), CLASSIFIERS);

        mustExist(gate.getTrafficProfile(), TRAFFIC_PROFILE);
        if (extent == Extent.NODE_AND_SUBTREE) {
            validateChild(trafficProfileValidator, gate.getTrafficProfile());
            validateChild(classifiersValidator, gate.getClassifiers());
        }
    }
}

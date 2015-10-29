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
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ClassifierValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ExtClassifierValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.Ipv6ClassifierValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;

/**
 * @author rvail
 */
public class GateValidator extends AbstractValidator<Gate> {

    private static final String GATE_ID = "gate.gateId";
    private static final String GATE_SPEC = "gate.gate-spec";
    private static final String TRAFFIC_PROFILE = "gate.traffic-profile";

//    private final GateSpecValidatator gateSpecValidatator = new GateSpecValidatator();
    private final TrafficProfileValidator trafficProfileValidator = new TrafficProfileValidator();
    private final ClassifierValidator classifierValidator = new ClassifierValidator();
    private final ExtClassifierValidator extClassifierValidator = new ExtClassifierValidator();
    private final Ipv6ClassifierValidator ipv6ClassifierValidator = new Ipv6ClassifierValidator();

    @Override
    public void validate(final Gate gate, final Extent extent) throws ValidationException {
        if (gate == null) {
            throw new ValidationException("gate must exist");
        }

        mustExist(gate.getGateId(), GATE_ID);

        // all leafs in GateSpec are optional
        // mustExist(gate.getGateSpec(), GATE_SPEC);

        mustExist(gate.getTrafficProfile(), TRAFFIC_PROFILE);
        if (extent == Extent.NODE_AND_SUBTREE) {
//            validateChild(gateSpecValidatator, gate.getGateSpec());
            validateChild(trafficProfileValidator, gate.getTrafficProfile());
        }

        // Classifiers

        if (gate.getClassifier() != null) {

            // classifer is not null, ext and ipv6 must be null

            if (gate.getExtClassifier() != null || gate.getIpv6Classifier() != null) {
                getErrorMessages().add("Only one type of classifier is allowed");
            }
            else if (extent == Extent.NODE_AND_SUBTREE) {
                validateChild(classifierValidator, gate.getClassifier());
            }

        }
        else if (gate.getExtClassifier() != null) {

            // classifer is null; ext is not null and ipv6 must be null

            if (gate.getIpv6Classifier() != null) {
                getErrorMessages().add("Only one type of classifier is allowed");
            }
            else if (extent == Extent.NODE_AND_SUBTREE) {
                validateChild(extClassifierValidator, gate.getExtClassifier());
            }

        }
        else if (gate.getIpv6Classifier() != null) {

            // classifer and ext are null; ipv6 is not
            if (extent == Extent.NODE_AND_SUBTREE) {
                validateChild(ipv6ClassifierValidator, gate.getIpv6Classifier());
            }

        }
        else {
            getErrorMessages().add("a classifer is required");
        }

        throwErrorsIfNeeded();
    }
}

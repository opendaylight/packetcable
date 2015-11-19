/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier;

import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.classifier.container.ClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.classifier.container.classifier.choice.ExtClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.classifier.container.classifier.choice.Ipv6ClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.classifier.container.classifier.choice.QosClassifierChoice;

/**
 * @author rvail
 */
public class ClassifierChoiceValidator extends AbstractValidator<ClassifierChoice> {

    private final ClassifierValidator classifierValidator = new ClassifierValidator();
    private final ExtClassifierValidator extClassifierValidator = new ExtClassifierValidator();
    private final Ipv6ClassifierValidator ipv6ClassifierValidator = new Ipv6ClassifierValidator();

    @Override
    public void validate(final ClassifierChoice choice, final Extent extent) throws ValidationException {
        if (choice == null) {
            throw new ValidationException("classifier-choice must exist");
        }

        // Determine what type this choice is then validate it
        if (choice instanceof QosClassifierChoice) {
            validateChild(classifierValidator, ((QosClassifierChoice) choice).getClassifier());
        }
        else if (choice instanceof ExtClassifierChoice) {
            validateChild(extClassifierValidator, ((ExtClassifierChoice) choice).getExtClassifier());
        }
        else if (choice instanceof Ipv6ClassifierChoice) {
            validateChild(ipv6ClassifierValidator, ((Ipv6ClassifierChoice) choice).getIpv6Classifier());
        }
        else {
            throw new IllegalStateException("Unknown ClassifierChoice Type: " + choice.getClass().getName());
        }

        throwErrorsIfNeeded();
    }

}

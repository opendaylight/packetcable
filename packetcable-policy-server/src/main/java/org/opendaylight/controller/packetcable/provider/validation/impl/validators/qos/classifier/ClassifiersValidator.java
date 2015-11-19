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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.Classifiers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.ClassifierContainer;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.classifier.container.classifier.choice.QosClassifierChoice;

/**
 * @author rvail
 */
public class ClassifiersValidator extends AbstractValidator<Classifiers> {

    private static final String CLASSIFER_CONTAINER = "classifers.classifer-container";

    private ClassifierContainerValidator classifierContainerValidator = new ClassifierContainerValidator();

    @Override
    public void validate(final Classifiers classifiers, final Extent extent) throws ValidationException {
        if (classifiers == null) {
            throw new ValidationException("classifiers must exist");
        }

        mustExistAndNotBeEmpty(classifiers.getClassifierContainer(), CLASSIFER_CONTAINER);

        boolean hasBasic = false;
        boolean hasExtOrIpv6 = false;
        for (ClassifierContainer classifier : classifiers.getClassifierContainer()) {
            if (classifier.getClassifierChoice() instanceof QosClassifierChoice) {
                hasBasic = true;
            } else {
                hasExtOrIpv6 = true;
            }
        }
        if (hasBasic && hasExtOrIpv6) {
            getErrorMessages().add("Legacy classifiers cannot be used in conjunction with Extended or Ipv6 Classifiers");
        }

        if (extent == Extent.NODE_AND_SUBTREE) {
            for (ClassifierContainer classifier : classifiers.getClassifierContainer()) {
                validateChild(classifierContainerValidator, classifier);
            }
        }

        throwErrorsIfNeeded();
    }

}

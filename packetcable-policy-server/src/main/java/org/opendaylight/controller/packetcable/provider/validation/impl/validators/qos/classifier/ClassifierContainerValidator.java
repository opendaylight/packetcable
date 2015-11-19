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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.classifier.attributes.classifiers.ClassifierContainer;

/**
 * @author rvail
 */
public class ClassifierContainerValidator extends AbstractValidator<ClassifierContainer> {

    private static final String CLASSIFIER_ID = "classifier-container.classifier-id";
    private static final String CLASSIFIER_CHOICE = "classifier-container.classifier-choice";

    private final ClassifierChoiceValidator classifierChoiceValidator = new ClassifierChoiceValidator();

    @Override
    public void validate(final ClassifierContainer container, final Extent extent) throws ValidationException {
        if (container == null) {
            throw new ValidationException("classifer-container must exist");
        }

        mustExist(container.getClassifierChoice(), CLASSIFIER_CHOICE);

        mustExist(container.getClassifierId(), CLASSIFIER_ID);

        if (extent == Extent.NODE_AND_SUBTREE) {
            validateChild(classifierChoiceValidator, container.getClassifierChoice());
        }

        throwErrorsIfNeeded();
    }

}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.test.rules.Params;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.classifier.attributes.Classifiers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.classifier.attributes.ClassifiersBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.classifier.attributes.classifiers.ClassifierContainer;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class ClassifiersValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final ClassifiersValidator validator = new ClassifiersValidator();

    @Test(expected = ValidationException.class)
    public void nullClassifiers() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void badClassifiersList() throws ValidationException {
        List<ClassifierContainer> classifierChoices = Lists.newArrayList(
                ClassifierContainerValidatorTest.buildValidClassifierContainer((short) 1),
                ClassifierContainerValidatorTest.buildValidIpv6ClassifierContainer((short)2)
        );
        Classifiers classifiers = new ClassifiersBuilder()
                .setClassifierContainer(classifierChoices)
                .build();

        validator.validate(classifiers, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws Exception {
        Classifiers classifiers = buildValidClassifiers();

        validator.validate(classifiers, extentParams.getCurrentParam());
    }

    public static Classifiers buildValidClassifiers() {
        List<ClassifierContainer> classifierChoices = Lists.newArrayList(
                ClassifierContainerValidatorTest.buildValidExtClassifierContainer((short)1),
                ClassifierContainerValidatorTest.buildValidIpv6ClassifierContainer((short) 2)
        );
        return new ClassifiersBuilder()
                .setClassifierContainer(classifierChoices)
                .build();
    }
}

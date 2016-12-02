/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier;

import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.test.rules.Params;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.ClassifierContainer;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.ClassifierContainerBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class ClassifierContainerValidatorTest {

    private static final short DEFAULT_ID = (short)1;

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final ClassifierContainerValidator validator = new ClassifierContainerValidator();

    @Test(expected = ValidationException.class)
    public void testNullClassifierContainer() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Test(expected = NullPointerException.class)
    public void testNullExtent() throws ValidationException {
        validator.validate(buildValidClassifierContainer(DEFAULT_ID), null);
    }

    @Test
    public void validQos() throws ValidationException {
        ClassifierContainer classifierContainer = buildValidClassifierContainer(DEFAULT_ID);

        validator.validate(classifierContainer, extentParams.getCurrentParam());
    }

    @Test
    public void validExt() throws ValidationException {
        ClassifierContainer classifierContainer = buildValidExtClassifierContainer(DEFAULT_ID);

        validator.validate(classifierContainer, extentParams.getCurrentParam());
    }

    @Test
    public void validIpv6() throws ValidationException {
        ClassifierContainer classifierContainer = buildValidIpv6ClassifierContainer(DEFAULT_ID);

        validator.validate(classifierContainer, extentParams.getCurrentParam());
    }


    public static ClassifierContainer buildValidClassifierContainer(short id) {
        return new ClassifierContainerBuilder()
                .setClassifierId(id)
                .setClassifierChoice(ClassifierChoiceValidatorTest.buildValidQosClassifierChoice())
                .build();
    }

    public static ClassifierContainer buildValidExtClassifierContainer(short id) {
        return new ClassifierContainerBuilder()
                .setClassifierId(id)
                .setClassifierChoice(ClassifierChoiceValidatorTest.buildValidExtClassifierChoice())
                .build();
    }

    public static ClassifierContainer buildValidIpv6ClassifierContainer(short id) {
        return new ClassifierContainerBuilder()
                .setClassifierId(id)
                .setClassifierChoice(ClassifierChoiceValidatorTest.buildValidIpv6ClassifierChoice())
                .build();
    }

}

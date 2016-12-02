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
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.ClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.classifier.choice.ExtClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.classifier.choice.ExtClassifierChoiceBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.classifier.choice.Ipv6ClassifierChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.classifier.choice.Ipv6ClassifierChoiceBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.classifier.attributes.classifiers.classifier.container.classifier.choice.QosClassifierChoiceBuilder;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class ClassifierChoiceValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final ClassifierChoiceValidator validator = new ClassifierChoiceValidator();

    @Test(expected = ValidationException.class)
    public void testNullClassifierChoice() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void testNullExtent() throws ValidationException {
        validator.validate(buildValidQosClassifierChoice(), null);
    }

    @Test(expected = IllegalStateException.class)
    public void testUnknownClassifierChoiceClass() throws ValidationException {
        ClassifierChoice unknownChoice = new ClassifierChoice() {
            @Override
            public Class<? extends DataContainer> getImplementedInterface() {
                return null;
            }
        };
        validator.validate(unknownChoice, extentParams.getCurrentParam());
    }

    @Test
    public void testValid() throws ValidationException {
       validator.validate(buildValidQosClassifierChoice(),extentParams.getCurrentParam());
    }

    @Test
    public void testValidExt() throws ValidationException {
        validator.validate(buildValidExtClassifierChoice(), extentParams.getCurrentParam());
    }

    @Test
    public void testValidIpv6() throws ValidationException {
        validator.validate(buildValidIpv6ClassifierChoice(), extentParams.getCurrentParam());
    }

    public static ClassifierChoice buildValidQosClassifierChoice() {
        return new QosClassifierChoiceBuilder()
                .setClassifier(ClassifierValidatorTest.buildValidClassifierTree())
                .build();
    }

    public static ExtClassifierChoice buildValidExtClassifierChoice() {
        return new ExtClassifierChoiceBuilder()
                .setExtClassifier(ExtClassifierValidatorTest.buildValidExtClassifier())
                .build();
    }

    public static Ipv6ClassifierChoice buildValidIpv6ClassifierChoice() {
        return new Ipv6ClassifierChoiceBuilder()
                .setIpv6Classifier(Ipv6ClassifierValidatorTest.buildValidIpv6Classifier())
                .build();
    }

}

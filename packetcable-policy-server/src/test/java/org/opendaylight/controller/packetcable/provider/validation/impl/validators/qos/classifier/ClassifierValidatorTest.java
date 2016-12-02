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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.TpProtocol;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161128.pcmm.qos.classifier.ClassifierBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class ClassifierValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final ClassifierValidator validator= new ClassifierValidator();

    @Test(expected = ValidationException.class)
    public void nullClassifier() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Params.DoNotUseParams
    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidClassifierTree(), null);
    }

    @Test
    public void nullSrcIp() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setSrcIp(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
     public void nullSrcPort() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setSrcPort(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullDstIp() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setDstIp(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullDstPort() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setDstPort(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullProtocol() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setProtocol(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullTosByte() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setTosByte(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
     public void nullTosMask() throws ValidationException {
        Classifier classifier = new ClassifierBuilder(buildValidClassifierTree())
                .setTosMask(null)
                .build();

        validator.validate(classifier, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        Classifier classifier = buildValidClassifierTree();
        validator.validate(classifier, extentParams.getCurrentParam());
    }

    public static Classifier buildValidClassifierTree() {
        return new ClassifierBuilder()
                .setSrcIp(new Ipv4Address("10.0.0.100"))
                .setSrcPort(new PortNumber(7000))
                .setDstIp(new Ipv4Address("10.0.0.200"))
                .setDstPort(new PortNumber(8000))
                .setProtocol(new TpProtocol(127))
                .setTosByte(new TosByte((short)0x10))
                .setTosMask(new TosByte((short)0xf0))
                .build();
    }
}

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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.TpProtocol;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.ext.classifier.ExtClassifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.ext.classifier.ExtClassifierBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class ExtClassifierValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final ExtClassifierValidator validator = new ExtClassifierValidator();

    @Test(expected = ValidationException.class)
      public void nullExtClassifier() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidExtClassifier(), null);
    }

    @Test(expected = ValidationException.class)
    public void nullSrcIp() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setSrcIp(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullSrcPortStart() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setSrcPortStart(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullSrcPortEnd() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setSrcPortEnd(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullDstIp() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setDstIp(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullDstPortStart() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setDstPortStart(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullDstPortEnd() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setDstPortEnd(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullProtocol() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setProtocol(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullTosByte() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setTosByte(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test(expected = ValidationException.class)
    public void nullTosMask() throws ValidationException {
        ExtClassifier extClassifier = new ExtClassifierBuilder(buildValidExtClassifier())
                .setTosMask(null)
                .build();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        ExtClassifier extClassifier = buildValidExtClassifier();

        validator.validate(extClassifier, extentParams.getCurrentParam());
    }

    public static ExtClassifier buildValidExtClassifier() {
        return new ExtClassifierBuilder()
                .setSrcIp(new Ipv4Address("10.0.0.100"))
                .setSrcIpMask(new Ipv4Address("255.255.255.0"))
                .setSrcPortStart(new PortNumber(7000))
                .setSrcPortEnd(new PortNumber((7005)))
                .setDstIp(new Ipv4Address("10.0.0.200"))
                .setDstIpMask(new Ipv4Address("255.255.255.0"))
                .setDstPortStart(new PortNumber(7000))
                .setDstPortEnd(new PortNumber(7005))
                .setProtocol(new TpProtocol(127))
                .setTosByte(new TosByte((short)0x10))
                .setTosMask(new TosByte((short)0xf0))
                .build();
    }
}

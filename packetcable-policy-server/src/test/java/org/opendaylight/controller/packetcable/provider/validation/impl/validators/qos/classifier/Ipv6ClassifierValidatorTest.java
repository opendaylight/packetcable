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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.TosByte;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.TpProtocol;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.ipv6.classifier.Ipv6Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.ipv6.classifier.Ipv6ClassifierBuilder;

/**
 * @author rvail
 */
@Params.AlwaysUseParams
public class Ipv6ClassifierValidatorTest {

    @Rule
    public Params<Validator.Extent> extentParams = Params.of(Validator.Extent.class);

    private final Ipv6ClassifierValidator validator = new Ipv6ClassifierValidator();

    @Test(expected = ValidationException.class)
    public void nullIpv6Classifier() throws ValidationException {
        validator.validate(null, extentParams.getCurrentParam());
    }

    @Test(expected = NullPointerException.class)
    public void nullExtent() throws ValidationException {
        validator.validate(buildValidIpv6Classifier(), null);
    }

    @Test
    public void nullSrcIp6() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setSrcIp6(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullSrcPortStart() throws ValidationException {
        Ipv6Classifier ipv6Classifier =
                new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setSrcPortStart(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullSrcPortEnd() throws ValidationException {
        Ipv6Classifier ipv6Classifier =
                new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setSrcPortEnd(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullDstIp6() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setDstIp6(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullDstPortStart() throws ValidationException {
        Ipv6Classifier ipv6Classifier =
                new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setDstPortStart(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullDstPortEnd() throws ValidationException {
        Ipv6Classifier ipv6Classifier =
                new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setDstPortEnd(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullFlowLabel() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setFlowLabel(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullNextHdr() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setNextHdr(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullTcHigh() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setTcHigh(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullTcLow() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setTcLow(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void nullTcMask() throws ValidationException {
        Ipv6Classifier ipv6Classifier = new Ipv6ClassifierBuilder(buildValidIpv6Classifier()).setTcMask(null).build();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    @Test
    public void valid() throws ValidationException {
        Ipv6Classifier ipv6Classifier = buildValidIpv6Classifier();

        validator.validate(ipv6Classifier, extentParams.getCurrentParam());
    }

    public static Ipv6Classifier buildValidIpv6Classifier() {
        return new Ipv6ClassifierBuilder().setSrcIp6(new Ipv6Prefix("2001:4978:030d:1000:0:0:0:0/64"))
                .setSrcPortStart(new PortNumber(7000))
                .setSrcPortEnd(new PortNumber((7005)))
                .setDstIp6(new Ipv6Prefix("2001:4978:030d:1100:0:0:0:0/64"))
                .setDstPortStart(new PortNumber(7000))
                .setDstPortEnd(new PortNumber(7005))
                .setFlowLabel(101L)
                .setNextHdr(new TpProtocol(256))
                .setTcHigh(new TosByte((short) 0xc0))
                .setTcLow(new TosByte((short) 0x01))
                .setTcMask(new TosByte((short) 0xe0))
                .build();
    }
}

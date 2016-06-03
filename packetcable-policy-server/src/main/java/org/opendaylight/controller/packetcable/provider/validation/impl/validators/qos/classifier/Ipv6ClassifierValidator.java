/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.ipv6.classifier.Ipv6Classifier;

/**
 * @author rvail
 */
public class Ipv6ClassifierValidator extends AbstractValidator<Ipv6Classifier> {


    private static final String SRC_IP6 = "ipv6-classifer.srcIp6";
    private static final String DST_IP6 = "ipv6-classifer.dstIp6";

    private static final String TC_LOW = "ipv6-classifer.tc-low";
    private static final String TC_HIGH = "ipv6-classifer.tc-high";
    private static final String TC_MASK = "ipv6-classifer.tc-mask";

    private static final String NEXT_HEADER = "ipv6-classifer.next-hdr";

    private static final String FLOW_LABEL = "ipv6-classifer.flow-label";

    private static final String SRC_PORT_START = "ipv6-classifer.srcPort-start";
    private static final String SRC_PORT_END = "ipv6-classifer.srcPort-end";

    private static final String DST_PORT_START = "ipv6-classifer.dstPort-start";
    private static final String DST_PORT_END = "ipv6-classifer.dstPort-end";

    @Override
    protected void doValidate(final Ipv6Classifier ipv6Classifier, final Extent extent) {
        if (ipv6Classifier == null) {
            getErrorMessages().add("ipv6-classifer must exist");
            return;
        }

        // all values are optional
    }
}

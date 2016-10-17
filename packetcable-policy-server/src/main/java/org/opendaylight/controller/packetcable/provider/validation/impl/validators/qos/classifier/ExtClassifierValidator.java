/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.pcmm.qos.ext.classifier.ExtClassifier;

/**
 * @author rvail
 */
public class ExtClassifierValidator extends AbstractValidator<ExtClassifier> {

    private static final String SRC_IP = "ext-classifer.srcIp";
    private static final String SRC_MASK = "ext-classifer.srcIpMask";

    private static final String DST_IP = "ext-classifer.dstIp";
    private static final String DST_MASK = "ext-classifer.dstIpMask";

    private static final String TOS_BYTE = "ext-classifer.tos-byte";
    private static final String TOS_MASK = "ext-classifer.tos-mask";

    private static final String PROTOCOL = "ext-classifer.protocol";

    private static final String SRC_PORT_START = "ext-classifer.srcPort-start";
    private static final String SRC_PORT_END = "ext-classifer.srcPort-end";

    private static final String DST_PORT_START = "ext-classifer.dstPort-start";
    private static final String DST_PORT_END = "ext-classifer.dstPort-end";

    @Override
    protected void doValidate(final ExtClassifier extClassifier, final Extent extent) {
        if (extClassifier == null) {
            getErrorMessages().add("ext-classifier must exist");
            return;
        }

        // all values are optional and have defaults

    }
}

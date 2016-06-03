/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier;

import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.pcmm.qos.classifier.Classifier;


/**
 * @author rvail
 */
public class ClassifierValidator extends AbstractValidator<Classifier> {

    private static final String SRC_IP = "classifer.srcIp";
    private static final String SRC_PORT = "classifer.srcPort";

    private static final String DST_IP = "classifer.dstIp";
    private static final String DST_PORT = "classifer.dstPort";

    private static final String TOS_BYTE = "classifer.tos-byte";
    private static final String TOS_MASK = "classifer.tos-mask";

    private static final String PROTOCOL = "classifer.protocol";

    @Override
    protected void doValidate(final Classifier classifier, final Extent extent) {
        if (classifier == null) {
            getErrorMessages().add("classifer must exist");
            return;
        }

        // all values are optional
    }
}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps;

import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.AbstractValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151101.ccaps.Ccap;

/**
 * @author rvail
 */
public class CcapValidator extends AbstractValidator<Ccap> {

    private static final String CCAPID = "ccap.ccapid";
    private static final String CONNECTION = "ccap.connection";
    private static final String AM_ID = "ccap.amId";
    private static final String UP_STREAM_SCNS = "ccap.upstream-scns";
    private static final String DOWN_STREAM_SCNS = "ccap.downstream-scns";

    private final AmIdValidator amIdValidator = new AmIdValidator();
    private final ConnectionValidator connectionValidator = new ConnectionValidator();

    @Override
    public void validate(final Ccap ccap, Extent extent) throws ValidationException {
        if (ccap == null) {
            throw new ValidationException("ccap must exist");
        }

        mustExist(ccap.getCcapId(), CCAPID);

        mustExistAndNotBeEmpty(ccap.getUpstreamScns(), UP_STREAM_SCNS);
        mustExistAndNotBeEmpty(ccap.getDownstreamScns(), DOWN_STREAM_SCNS);

        if (extent == Extent.NODE_AND_SUBTREE) {
            validateChild(amIdValidator, ccap.getAmId());
            validateChild(connectionValidator, ccap.getConnection());
        } else {
            mustExist(ccap.getAmId(), AM_ID);
            mustExist(ccap.getConnection(), CONNECTION);
        }

        throwErrorsIfNeeded();
    }


}

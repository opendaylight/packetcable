/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.validation.ValidatorProvider;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.ccaps.Ccap;

/**
 * @author rvail
 */
public class CcapsValidatorProviderFactoryTest {

    private final CcapsValidatorProviderFactory factory = new CcapsValidatorProviderFactory();

    @Test
    public void validBuild() {
        assertNotNull(factory.build());
        assertThat(factory.build(),  instanceOf(ValidatorProvider.class));

        assertNotNull(factory.build().validatorFor(Ccaps.class));
        assertNotNull(factory.build().validatorFor(Ccap.class));
        assertNotNull(factory.build().validatorFor(AmId.class));
        assertNotNull(factory.build().validatorFor(Connection.class));
    }

}

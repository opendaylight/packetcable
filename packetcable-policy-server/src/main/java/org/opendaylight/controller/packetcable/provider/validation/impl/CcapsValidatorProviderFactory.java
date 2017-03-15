/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl;

import org.opendaylight.controller.packetcable.provider.validation.ValidatorProvider;
import org.opendaylight.controller.packetcable.provider.validation.ValidatorProviderFactory;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.AmIdValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.CcapValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.CcapsValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.ConnectionValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.ccap.attributes.AmId;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170224.ccaps.Ccap;

/**
 * A ValidatorProviderFactory that can provide validators for types under packetcable:ccaps.
 *
 * @author rvail
 */
public class CcapsValidatorProviderFactory implements ValidatorProviderFactory {

    @Override
    public ValidatorProvider build() {
        return addCcapsValidators(new ValidatorProviderImpl());
    }

    public static ValidatorProvider addCcapsValidators(ValidatorProvider provider) {
        provider.put(Ccaps.class, new CcapsValidator());
        provider.put(Ccap.class, new CcapValidator());
        provider.put(AmId.class, new AmIdValidator());
        provider.put(Connection.class, new ConnectionValidator());

        return provider;
    }
}

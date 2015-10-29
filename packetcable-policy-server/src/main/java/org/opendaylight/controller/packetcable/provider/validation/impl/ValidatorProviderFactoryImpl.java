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

/**
 * A ValidatorProviderFactory that returns providers that can handle all known types.
 *
 * @author rvail
 */
public class ValidatorProviderFactoryImpl implements ValidatorProviderFactory {

    @Override
    public ValidatorProvider build() {
        ValidatorProvider provider = new ValidatorProviderImpl();

        CcapsValidatorProviderFactory.addCcapsValidators(provider);
        QosValidatorProviderFactory.addQosValidators(provider);

        return provider;
    }
}

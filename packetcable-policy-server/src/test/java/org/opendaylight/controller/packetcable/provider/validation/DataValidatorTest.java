/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.ccaps.CcapValidatorTest;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161017.ccaps.Ccap;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author rvail
 */
public class DataValidatorTest {

    @Test
    public void badData() throws ValidationException {
        final ValidatorProvider validatorProvider = mock(ValidatorProvider.class);

        doThrow(new ValidationException("unit-test-exception"))
                .when(validatorProvider)
                .validate(any(Class.class), any(Ccap.class), any(Validator.Extent.class));


        final Ccap ccap = mock(Ccap.class);
        doReturn(Ccap.class)
                .when(ccap)
                .getImplementedInterface();


        DataValidator dataValidator = new DataValidator(validatorProvider);


        Map<InstanceIdentifier<?>, DataObject> dataMap = Maps.newHashMap();
        InstanceIdentifier iid = mock(InstanceIdentifier.class);
        dataMap.put(iid, ccap);

        Map<InstanceIdentifier<?>, ValidationException> validationMap = dataValidator.validate(dataMap, Validator.Extent.NODE_ONLY);

        assertThat(validationMap, is(not(nullValue())));
        assertThat(validationMap.size(), is(1));
        assertThat(validationMap.containsKey(iid), is(true));
        ValidationException ex = validationMap.get(iid);
        assertThat(ex, is(not(nullValue())));

    }

    @Test
    public void validCcap() {
        final ValidatorProvider validatorProvider = mock(ValidatorProvider.class);

        Map<InstanceIdentifier<?>, DataObject> dataMap = Maps.newHashMap();
        InstanceIdentifier iid = mock(InstanceIdentifier.class);
        dataMap.put(iid, CcapValidatorTest.buildValidCcapTree());

        final DataValidator dataValidator = new DataValidator(validatorProvider);
        Map<InstanceIdentifier<?>, ValidationException> validationMap = dataValidator.validate(dataMap, Validator.Extent.NODE_ONLY);

        assertThat(validationMap.isEmpty(), is(true));

    }

    @Test
    public void validCcapExplictTyping() {
        final ValidatorProvider validatorProvider = mock(ValidatorProvider.class);

        Map<InstanceIdentifier<Ccap>, Ccap> dataMap = Maps.newHashMap();
        InstanceIdentifier<Ccap> iid = mock(InstanceIdentifier.class);
        dataMap.put(iid, CcapValidatorTest.buildValidCcapTree());

        final DataValidator dataValidator = new DataValidator(validatorProvider);
        Map<InstanceIdentifier<Ccap>, ValidationException> validationMap = dataValidator.validateOneType(dataMap,
                Validator.Extent.NODE_ONLY);

        assertThat(validationMap.isEmpty(), is(true));

    }

}

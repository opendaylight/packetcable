/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.controller.packetcable.provider.validation.ValidatorProvider;
import org.opendaylight.yangtools.yang.binding.DataObject;


/**
 * {@inheritDoc}
 *
 * @author rvail
 */
public class ValidatorProviderImpl implements ValidatorProvider {

    private final Map<Class<? extends DataObject>, Validator<? extends DataObject>> validatorMap;

    public ValidatorProviderImpl() {
        this.validatorMap = Maps.newHashMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends DataObject> void put(@Nonnull final Class<T> tClass, @Nonnull final Validator<T> validator) {
        checkNotNull(tClass);
        checkNotNull(validator);
        validatorMap.put(tClass, validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends DataObject> void validate(@Nonnull final Class<T> tClass, @Nonnull final DataObject data,
            @Nonnull final Validator.Extent extent) throws ValidationException {
        if (!tClass.isAssignableFrom(data.getClass())) {
            throw new IllegalArgumentException(
                    String.format("data must be the same type as tClass, got=%s : expected=%s",
                            data.getImplementedInterface(), tClass));
        }
        // We are checking the type of data above
        @SuppressWarnings("unchecked") T tData = (T) data;
        validatorFor(tClass).validate(tData, extent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends DataObject> Validator<T> validatorFor(@Nonnull final Class<T> tClass) {
        checkNotNull(tClass);
        if (validatorMap.containsKey(tClass)) {
            // validation is done via the put method all key/value pairs are for the same type T
            @SuppressWarnings("unchecked") Validator<T> result = (Validator<T>) validatorMap.get(tClass);
            return result;
        }
        throw new NoSuchElementException("Entry not found for key: " + tClass);
    }


}

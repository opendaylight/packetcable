/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation;

import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Helper class to hide the casting needed to store all the validators in one collection.
 * Types remain consistent by using generics on the put method.
 *
 * @author rvail
 */
public interface ValidatorProvider {

    /**
     * Add a new validator or replace an old validator to this provider.
     *
     * @param tClass
     *         The Class of T
     * @param validator
     *         The validator for the Class T
     * @param <T>
     *         The type being validated
     */
    <T extends DataObject> void put(@Nonnull final Class<T> tClass, @Nonnull final Validator<T> validator);

    /**
     * Gets the validator for a particular type
     *
     * @param tClass
     *         The Class of T
     * @param <T>
     *         The type to be validated
     * @return a Validator instance
     * @throws NoSuchElementException
     *         if a Validator for the passed in type does not exist on this provider.
     */
    <T extends DataObject> Validator<T> validatorFor(@Nonnull final Class<T> tClass);

    /**
     * Helper method to get a validator and then call validate with the supplied DataObject.
     *
     * @param tClass
     *         The class type to validate
     * @param data
     *         The DataObject instance to validate
     * @param extent
     *         The extend to validate with
     * @param <T>
     *         The type of tClass
     * @throws ValidationException
     *         if validation fails
     * @throws IllegalArgumentException
     *         if data is not assignable from tClass
     */
    <T extends DataObject> void validate(@Nonnull final Class<T> tClass, @Nonnull final DataObject data,
            @Nonnull final Validator.Extent extent) throws ValidationException;
}

/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates restconf data that is supplied by users
 *
 * @author rvail
 */
public class DataValidator {
    private static final Logger logger = LoggerFactory.getLogger(DataValidator.class);

    private final ValidatorProvider validatorProvider;

    public DataValidator(@Nonnull final ValidatorProvider validatorProvider) {
        this.validatorProvider = checkNotNull(validatorProvider);
    }

    public Map<InstanceIdentifier<?>, ValidationException> validate(
            @Nonnull final Map<InstanceIdentifier<?>, DataObject> dataObjectMap,
            @Nonnull final Validator.Extent extent) {
        checkNotNull(dataObjectMap);

        Map<InstanceIdentifier<?>, ValidationException> exceptionMap = Maps.newHashMap();

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dataObjectMap.entrySet()) {
            final InstanceIdentifier<?> iid = entry.getKey();
            final DataObject data = entry.getValue();

            try {
                validate(iid, data, extent);
            } catch (ValidationException e) {
                exceptionMap.put(iid, e);
                logger.debug("invalid data: {}", data, e);
            } catch (NoSuchElementException e) {
                logger.error("Unable to find validator for data: {}", data, e);
            }
        }

        return exceptionMap;
    }

    public void validate(@Nonnull InstanceIdentifier<?> iid, @Nonnull final DataObject dataObject,
            @Nonnull final Validator.Extent extent) throws ValidationException {
        checkNotNull(iid);
        checkNotNull(dataObject);
        validatorProvider.validate(iid.getTargetType(), dataObject, extent);
    }

    public <T extends DataObject> Map<InstanceIdentifier<T>, ValidationException> validateOneType(
            @Nonnull final Map<InstanceIdentifier<T>, T> dataObjectMap, @Nonnull final Validator.Extent extent) {
        checkNotNull(dataObjectMap);

        Map<InstanceIdentifier<T>, ValidationException> exceptionMap = Maps.newHashMap();

        for (Map.Entry<InstanceIdentifier<T>, T> entry : dataObjectMap.entrySet()) {
            final InstanceIdentifier<T> iid = entry.getKey();
            final T data = entry.getValue();

            try {
                validate(iid, data, extent);
            } catch (ValidationException e) {
                exceptionMap.put(iid, e);
                logger.debug("invalid data: {}", data, e);
            } catch (NoSuchElementException e) {
                logger.error("Unable to find validator for data: {}", data, e);
            }
        }

        return exceptionMap;
    }


}

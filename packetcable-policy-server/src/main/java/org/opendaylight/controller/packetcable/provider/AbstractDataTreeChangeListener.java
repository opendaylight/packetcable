/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.packetcable.provider.validation.DataValidator;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rvail
 * @author mmakati
 */
public abstract class AbstractDataTreeChangeListener<T extends DataObject> implements DataTreeChangeListener<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> tClass;

    private final DataValidator dataValidator;

    public AbstractDataTreeChangeListener(Class<T> tClass,DataValidator dataValidator) {
        this.tClass = checkNotNull(tClass);
        this.dataValidator = checkNotNull(dataValidator);
    }

    private ValidationException validateData(final DataTreeModification<T> change){
        InstanceIdentifier iid = change.getRootPath().getRootIdentifier();

        try {
            // getDataAfter should only remove null if the data was removed, but we don't validate on remove.
            dataValidator.validate(iid, change.getRootNode().getDataAfter(), Validator.Extent.NODE_AND_SUBTREE);
        }
        catch (ValidationException e) {
            logger.debug("invalid data: {}", change.getRootNode().getDataAfter(), e);
            return e;
        }
        return null;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<T>> changes)
    {
        Map<DataTreeModification<T>, ValidationException> exceptionMap = Maps.newHashMap();
        for (final DataTreeModification<T> change : changes) {
            final DataObjectModification<T> root = change.getRootNode();
            switch (root.getModificationType()) {
                case SUBTREE_MODIFIED:
                    try{
                        ValidationException validationException = validateData(change);
                        if(validationException != null){
                            handleInvalidData(change,validationException);
                        }
                        else {
                            handleUpdatedData(change);
                        }
                    }catch (NoSuchElementException e) {
                        logger.error("Unable to find validator for data: {}", change.getRootNode().getDataAfter(), e);
                    }
                    break;
                case WRITE:
                    try{
                        ValidationException validationException = validateData(change);
                        if(validationException != null){
                            handleInvalidData(change,validationException);
                        }
                        else {
                            // Treat an overwrite as an update
                            boolean update = change.getRootNode().getDataBefore() != null;
                            if (update) {
                                handleUpdatedData(change);
                            } else {
                                handleCreatedData(change);
                            }
                        }
                    }catch (NoSuchElementException e) {
                        logger.error("Unable to find validator for data: {}", change.getRootNode().getDataAfter(), e);
                    }
                    break;
                case DELETE:
                    handleRemovedData(change);
                    break;
                default:
                    break;
            }
        }
    }
    protected abstract void handleCreatedData(final DataTreeModification<T> change);

    protected abstract void handleUpdatedData(final DataTreeModification<T> change);

    protected abstract void handleRemovedData(final DataTreeModification<T> change);

    protected abstract void handleInvalidData(final DataTreeModification<T> change, ValidationException validationException);


}

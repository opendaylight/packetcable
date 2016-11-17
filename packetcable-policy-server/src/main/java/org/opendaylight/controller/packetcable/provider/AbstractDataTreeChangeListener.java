/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.opendaylight.controller.packetcable.provider.DataChangeUtils.collectTypeFromMap;
import static org.opendaylight.controller.packetcable.provider.DataChangeUtils.collectTypeFromSet;
import static org.opendaylight.controller.packetcable.provider.DataChangeUtils.logChange;
import static org.opendaylight.controller.packetcable.provider.DataChangeUtils.relativeComplement;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.packetcable.provider.validation.DataValidator;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.controller.packetcable.provider.validation.ValidatorProvider;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * @author rvail
 */
public abstract class AbstractDataTreeChangeListener<T extends DataObject> implements DataTreeChangeListener<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> tClass;

    private final DataValidator dataValidator;

    public AbstractDataTreeChangeListener(Class<T> tClass,DataValidator dataValidator) {
        this.tClass = checkNotNull(tClass);
        this.dataValidator = checkNotNull(dataValidator);
    }

    private boolean validateCollectionData(Collection<DataTreeModification<T>> collection){
        Map<DataTreeModification<T>, ValidationException> exceptionMap = Maps.newHashMap();
        for (final DataTreeModification<T> change : collection) {
            final DataObjectModification<T> root = change.getRootNode();
            switch (root.getModificationType()) {
                case SUBTREE_MODIFIED:
                case WRITE:
                    try{
                        ValidationException validationException = validateData(change);
                        if(validationException != null){
                            exceptionMap.put(change, validationException);
                        }
                    }catch (NoSuchElementException e) {
                        logger.error("Unable to find validator for data: {}", change.getRootNode().getDataAfter(), e);
                        return false;
                    }
                    break;
            }
        }
        if (!exceptionMap.isEmpty()) {
            // bad data write errors to operational datastore
            handleInvalidData(exceptionMap);
            return false;
        }
        return true;
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
        if(!validateCollectionData(changes)){
            return;
        }
        for (final DataTreeModification<T> change : changes) {
            final DataObjectModification<T> root = change.getRootNode();
            switch (root.getModificationType()) {
                case SUBTREE_MODIFIED:
                    handleUpdatedData(change);
                    break;
                case WRITE:
                    // Treat an overwrite as an update
                    boolean update = change.getRootNode().getDataBefore() != null;
                    if (update) {
                        handleUpdatedData(change);
                    } else {
                        handleCreatedData(change);
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

    protected abstract void handleInvalidData(Map<DataTreeModification<T>, ValidationException> exceptionMap);


}

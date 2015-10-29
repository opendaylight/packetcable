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
import java.util.Set;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rvail
 */
public abstract class AbstractDataChangeListener<T extends DataObject> implements DataChangeListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> tClass;

    public AbstractDataChangeListener(Class<T> tClass) {
        this.tClass = checkNotNull(tClass);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> asyncDataChangeEvent) {
        logger.debug("############{}.onDataChanged()", getClass().getSimpleName());
        logChange(logger, asyncDataChangeEvent);

        final Map<InstanceIdentifier<?>, DataObject> allCreatedData = asyncDataChangeEvent.getCreatedData();
        final Map<InstanceIdentifier<?>, DataObject> allOriginalData = asyncDataChangeEvent.getOriginalData();
        final Map<InstanceIdentifier<?>, DataObject> allUpdatedData = asyncDataChangeEvent.getUpdatedData();

        // UpdatedData also contains all data that was created, remove it to get the set of only updated data
        final Map<InstanceIdentifier<?>, DataObject> trueUpdatedData =
                relativeComplement(allCreatedData, allUpdatedData);
        final Map<InstanceIdentifier<?>, DataObject> trueOriginalData =
                relativeComplement(allCreatedData, allOriginalData);

        if (!allCreatedData.isEmpty()) {
            final Map<InstanceIdentifier<T>, T> createdTs = collectTypeFromMap(tClass, allCreatedData);

            if (createdTs.isEmpty()) {
                // this should not happen since this object only listens for changes in one tree
                logger.warn("Expected created {}(s) but none were found: {}", tClass.getSimpleName(), allCreatedData);
            }
            else {
                handleCreatedData(createdTs);
            }
        }

        if (!trueUpdatedData.isEmpty()) {
            final Map<InstanceIdentifier<T>, T> updatedTs = collectTypeFromMap(tClass, trueUpdatedData);
            if (updatedTs.isEmpty()) {
                // this should not happen since this object should only listen for changes in its tree
                logger.warn("Expected updated {}(s) but none were found: {}", tClass.getSimpleName(), trueUpdatedData);
            }
            else {

                final Map<InstanceIdentifier<T>, T> originalTs = collectTypeFromMap(tClass, trueOriginalData);
                for (InstanceIdentifier<T> iid : updatedTs.keySet()) {
                    if (!originalTs.containsKey(iid)) {
                        logger.warn("No original data for updated object {}", iid);
                    }
                }

                handleUpdatedData(updatedTs, originalTs);
            }
        }

        final Set<InstanceIdentifier<?>> allRemovedPaths = asyncDataChangeEvent.getRemovedPaths();
        if (!allRemovedPaths.isEmpty()) {
            final Set<InstanceIdentifier<T>> removedTPaths = collectTypeFromSet(tClass, allRemovedPaths);
            if (removedTPaths.isEmpty()) {
                // this should not happen since this object should only listen for changes in its tree
                logger.warn("Expected removed {} but none were found: {}", tClass.getSimpleName(), allRemovedPaths);
            }

            Map<InstanceIdentifier<T>, T> originalTData = Maps.newHashMapWithExpectedSize(removedTPaths.size());
            for (InstanceIdentifier<T> iid : removedTPaths) {
                if (allOriginalData.containsKey(iid)) {

                    originalTData.put(iid, (T) allOriginalData.get(iid));
                }
            }

            handleRemovedData(removedTPaths, originalTData);
        }
    }

    protected abstract void handleCreatedData(final Map<InstanceIdentifier<T>, T> createdData);

    protected abstract void handleUpdatedData(final Map<InstanceIdentifier<T>, T> updatedData,
            final Map<InstanceIdentifier<T>, T> originalData);

    protected abstract void handleRemovedData(final Set<InstanceIdentifier<T>> removedPaths,
            final Map<InstanceIdentifier<T>, T> originalData);



}

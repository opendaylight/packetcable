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
import java.util.Collection;

import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rvail
 */
public abstract class AbstractDataTreeChangeListener<T extends DataObject> implements DataTreeChangeListener<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> tClass;

    public AbstractDataTreeChangeListener(Class<T> tClass) {
        this.tClass = checkNotNull(tClass);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<T>> collection)
    {
        for (final DataTreeModification<T> change : collection) {
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



}

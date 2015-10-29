/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;

/**
 * @author rvail
 */
public class DataChangeUtils {

    @SuppressWarnings("unchecked")
    public static <T extends DataObject> Map<InstanceIdentifier<T>, T> collectTypeFromMap(Class<T> tClass,
            Map<InstanceIdentifier<?>, DataObject> map) {
        Map<InstanceIdentifier<T>, T> result = Maps.newHashMap();

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : map.entrySet()) {
            if (tClass.isAssignableFrom(entry.getValue().getImplementedInterface())) {
                final InstanceIdentifier<T> tIID = (InstanceIdentifier<T>) entry.getKey();
                final T tObj = (T) entry.getValue();
                result.put(tIID, tObj);
            }
        }

        return result;
    }

    public static <T extends DataObject> Set<InstanceIdentifier<T>> collectTypeFromSet(Class<T> tClass,
            Set<InstanceIdentifier<?>> set) {
        Set<InstanceIdentifier<T>> result = Sets.newHashSet();

        for (InstanceIdentifier<?> iid : set) {
            if (tClass.isAssignableFrom(iid.getTargetType())) {
                @SuppressWarnings("unchecked")
                final InstanceIdentifier<T> tIID = (InstanceIdentifier<T>) iid;
                result.add(tIID);
            }
        }

        return result;
    }

    /**
     * Computes the relative complement of A in B. (aka get everything in B that is not in A)
     * @param setA The first set
     * @param setB The second set
     * @return the relative complement
     */
    public static Map<InstanceIdentifier<?>, DataObject> relativeComplement(Map<InstanceIdentifier<?>, DataObject> setA,
            Map<InstanceIdentifier<?>, DataObject> setB){

        Map<InstanceIdentifier<?>, DataObject> result = Maps.newHashMap();

        for (InstanceIdentifier<?> iid: setB.keySet()) {
            if (!setA.containsKey(iid)) {
                result.put(iid, setB.get(iid));
            }
        }

        return result;
    }



    public static void logChange(final Logger logger, final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        appendMap(sb, "CREATED", change.getCreatedData());
        appendSet(sb, "REMOVED", change.getRemovedPaths());
        appendMap(sb, "UPDATED", change.getUpdatedData());
        appendMap(sb, "ORIGINAL", change.getOriginalData());

        logger.debug("onDataChanged data:\n{}", sb.toString());
    }


    private static <E> void appendSet(StringBuilder sb, String name, Set<E> set) {
        sb.append("====").append(name).append("====\n");
        if (set == null || set.isEmpty()) {
            sb.append("None\n");
        } else {
            for (E data : set) {
                sb.append(data).append("\n");
                sb.append("----------\n");
            }
        }
        sb.append("===============\n");
    }

    private static void appendMap(StringBuilder sb, String name, Map<InstanceIdentifier<?>, DataObject> dataMap) {
        sb.append("====").append(name).append("====\n");
        if (dataMap == null || dataMap.isEmpty()) {
            sb.append("None\n");
        } else {
            for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : dataMap.entrySet()) {
                sb.append(entry.getValue()).append("\n");
                sb.append("----------\n");
            }
        }
        sb.append("===============\n");
    }
}

/**
 
 * Copyright (c) 2014 CableLabs.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html

 */
package org.pcmm.objects;

import java.util.HashMap;
import java.util.Map;

/**
 * This stores and handles the PCMM resources.
 *
 */
public class PCMMResourceSet {

    private Map<Object, PCMMResourcesMapper<?, ?>> mapper;

    private static PCMMResourceSet instance;

    private PCMMResourceSet() {
        mapper = new HashMap<Object, PCMMResourcesMapper<?, ?>>();
    }

    public static PCMMResourceSet getInstance() {
        if (instance == null)
            instance = new PCMMResourceSet();
        return instance;
    }

    /**
     * adds a new mapping
     *
     * @param key
     *            to be used for identifying mapped structure
     * @return resource mapper
     */
    @SuppressWarnings("unchecked")
    public <M, T extends PCMMResource> PCMMResourcesMapper<M, T> getMappedResources(
        Object key) {
        return (PCMMResourcesMapper<M, T>) mapper.get(key);
    }

    public <M, T extends PCMMResource> void mapResources(Object key,
            PCMMResourcesMapper<M, T> resources) {
        mapper.put(key, resources);
    }

    public void removeMapping(Object key) {
        mapper.remove(key);
    }

    public void removeAllMappings() {
        mapper.clear();
    }

}

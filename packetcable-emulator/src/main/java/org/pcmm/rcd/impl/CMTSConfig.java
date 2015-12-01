/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.pcmm.gates.IGateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold configuration settings in a YAML file
 */
public class CMTSConfig {

    private static final Logger logger = LoggerFactory.getLogger(CMTSConfig.class);

    /**
     * Returns the object that represents the YAML file
     * @param uri - the location of the YAML file
     * @return - the config object
     * @throws IOException - when the URI does not contain the proper YAML file
     */
    public static CMTSConfig loadConfig(final String uri) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final CmtsYmal cmtsYmal = mapper.readValue(new FileInputStream(uri), CmtsYmal.class);

        final Map<IGateSpec.Direction, Set<String>> scns = cmtsYmal.getServiceClassNames();

        final Set<String> upstreamSCNs = scns.containsKey(IGateSpec.Direction.UPSTREAM)
                ? scns.get(IGateSpec.Direction.UPSTREAM)
                : Collections.<String>emptySet();

        final Set<String> downstreamSCNs = scns.containsKey(IGateSpec.Direction.DOWNSTREAM)
                ? scns.get(IGateSpec.Direction.DOWNSTREAM)
                : Collections.<String>emptySet();

        if (upstreamSCNs.isEmpty() && downstreamSCNs.isEmpty()) {
            logger.error("No upstream or downstream service class names defined in config");
        }

        return new CMTSConfig(cmtsYmal.getPort(),
                cmtsYmal.getNumberOfSupportedClassifiers(),
                upstreamSCNs,
                downstreamSCNs,
                cmtsYmal.getCmStatus());
    }

    private final int port;

    private final short numberOfSupportedClassifiers;

    private final ImmutableSet<String> upstreamServiceClassNames;

    private final ImmutableSet<String> downstreamServiceClassNames;

    private final ImmutableMap<String, Boolean> modemStatus;

    public CMTSConfig(final int port, final short numberOfSupportedClassifiers,
            final Set<String> upstreamServiceClassNames, final Set<String> downstreamServiceClassNames,
            final Map<String, Boolean> modemStatus) {
        checkNotNull(upstreamServiceClassNames, "upstreamServiceClassNames must not be null");
        checkNotNull(downstreamServiceClassNames, "downstreamServiceClassNames must not be null");
        checkNotNull(modemStatus, "modemStatus must not be null");

        this.port = port;
        this.numberOfSupportedClassifiers = numberOfSupportedClassifiers;
        this.upstreamServiceClassNames =  ImmutableSet.copyOf(upstreamServiceClassNames);
        this.downstreamServiceClassNames = ImmutableSet.copyOf(downstreamServiceClassNames);
        this.modemStatus = ImmutableMap.copyOf(modemStatus);
    }

    public int getPort() {
        return port;
    }

    public short getNumberOfSupportedClassifiers() {
        return numberOfSupportedClassifiers;
    }

    public ImmutableSet<String> getUpstreamServiceClassNames() {
        return upstreamServiceClassNames;
    }

    public ImmutableSet<String> getDownstreamServiceClassNames() {
        return downstreamServiceClassNames;
    }

    public ImmutableMap<String, Boolean> getModemStatus() {
        return modemStatus;
    }
}

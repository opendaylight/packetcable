/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.rcd.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.pcmm.gates.IGateSpec;

/**
 * @author rvail
 */
public class CmtsYmal {
    @JsonProperty("port")
    private int port;

    @JsonProperty("numberOfSupportedClassifiers")
    private short numberOfSupportedClassifiers;

    @JsonProperty("serviceClassNames")
    private Collection<ServiceClassNamesYaml> serviceClassNames;

    @JsonProperty("cmStatuses")
    private Collection<CmStatusYaml> cmStatuses;

    public int getPort() {
        return port;
    }

    public short getNumberOfSupportedClassifiers() {
        return numberOfSupportedClassifiers;
    }

    public Map<IGateSpec.Direction, Set<String>> getServiceClassNames() {
        final Map<IGateSpec.Direction, Set<String>> out = new HashMap<>();

        for (final ServiceClassNamesYaml scns : serviceClassNames) {
            Set<String> names;
            if (out.containsKey(scns.direction)) {
                names = out.get(scns.direction);
            } else {
                names = new HashSet<>(scns.gateNames.size());
                out.put(scns.direction, names);
            }
            names.addAll(scns.gateNames);
        }
        return out;
    }

    public Map<String, Boolean> getCmStatus() {
        final Map<String, Boolean> out = new HashMap<>();

        for (final CmStatusYaml cmStatus : cmStatuses) {
            out.put(cmStatus.hostIp, cmStatus.status);
        }

        return out;
    }


    /**
     * Class to hold the YAML gate configuration values
     */
    public static class ServiceClassNamesYaml {
        @JsonProperty("direction")
        private IGateSpec.Direction direction;

        @JsonProperty("names")
        private Set<String> gateNames;

        public IGateSpec.Direction getDirection() {
            return direction;
        }

        public Set<String> getGateNames() {
            return Collections.unmodifiableSet(gateNames);
        }
    }


    /**
     * Class to hold the YAML Cable Modem configuration values
     */
    public static class CmStatusYaml {
        @JsonProperty("host")
        private String hostIp;

        @JsonProperty("status")
        private boolean status;
    }
}

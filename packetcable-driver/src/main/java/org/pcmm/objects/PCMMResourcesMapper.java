/*
 * Copyright (c) 2014 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.objects;

/**
 *
 * Resources mapper used to associate a key to a set of values
 */
public class PCMMResourcesMapper<M, T extends PCMMResource> {

    private M key;

    private T value;

    public PCMMResourcesMapper() {
    }

    public PCMMResourcesMapper(M key, T value) {
        this.key = key;
        this.value = value;
    }

    public M getKey() {
        return key;
    }

    public void setKey(M key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

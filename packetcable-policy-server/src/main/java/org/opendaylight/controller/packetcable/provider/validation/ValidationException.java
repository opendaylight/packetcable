/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author rvail
 */
public class ValidationException extends Exception {

    private final ImmutableList<String> errorMessages;

    public ValidationException(final String... errorMessages) {
        super(concat(Arrays.asList(errorMessages)));
        this.errorMessages = ImmutableList.copyOf(errorMessages);
    }

    private static String concat(Collection<String> strings) {
        checkNotNull(strings);

        final Iterator<String> iter = strings.iterator();
        if (!iter.hasNext()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            sb.append(" : ").append(iter.next());
        }

        return sb.toString();
    }

    public ValidationException(final Collection<String> errorMessages) {
        super(concat(errorMessages));
        this.errorMessages = ImmutableList.copyOf(errorMessages);
    }

    public ImmutableList<String> getErrorMessages() {
        return errorMessages;
    }
}

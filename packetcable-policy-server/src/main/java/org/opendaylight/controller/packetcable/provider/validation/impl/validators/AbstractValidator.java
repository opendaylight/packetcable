/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl.validators;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;

/**
 * Helper class to help support lazy initialization of error message array.<br>
 * This delays array creation until bad data is found.
 * <br><br>
 * <strong>Subclasses must call {@link #throwErrorsIfNeeded()} at the end of validate()</strong>
 *
 * @author rvail
 */
@NotThreadSafe
public abstract class AbstractValidator<T> implements Validator<T> {

    private ArrayList<String> errorMessages = null;

    /**
     * If any error messages have been added to the list returned by {@link #getErrorMessages()}
     * then a ValidationException will be thrown with those error messages.
     *
     * @throws ValidationException
     */
    protected void throwErrorsIfNeeded() throws ValidationException {
        if (errorMessages != null && !errorMessages.isEmpty()) {
            ValidationException exception = new ValidationException(errorMessages);
            resetErrorMessages();
            throw exception;
        }
    }

    /**
     * sets the error message list to null.
     */
    protected void resetErrorMessages() {
        errorMessages = null;
    }

    /**
     * Checks if the passed in object is null. If it is, then an error message will be
     * appended to the current list of errors.
     *
     * @param obj
     *         The object that must not be null.
     * @param name
     *         The name of the object (will be used in the error message).
     */
    protected void mustExist(Object obj, String name) {
        if (obj == null) {
            getErrorMessages().add(name + " must exist");
        }
    }

    /**
     * Lazy initalizer of an array list of error messages.
     *
     * @return The array list of error messages
     */
    protected ArrayList<String> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>(2);
        }
        return errorMessages;
    }

    /**
     * Checks if the passed in collection is null or empty. If it is then an error
     * will be appended to the current list of errors.
     *
     * @param collection
     *         The collection to test
     * @param name
     *         The name of the object (will be used in the error message)
     */
    protected void mustExistAndNotBeEmpty(Collection<?> collection, String name) {
        if (collection == null) {
            getErrorMessages().add(name + " must exist");
        } else if (collection.isEmpty()) {
            getErrorMessages().add(name + " must not be empty");
        }
    }

    protected <C> void validateChild(Validator<C> validator, C child) {
        try {
            validator.validate(child, Extent.NODE_AND_SUBTREE);
        } catch (ValidationException e) {
            getErrorMessages().addAll(e.getErrorMessages());
        }
    }
}

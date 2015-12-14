/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.test.rules;

import com.google.common.base.Optional;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Rule that allows a test to be run with multiple parameters.<br><br>
 * Two ways to use
 * <ol>
 *     <li>Individual Method Annotation - {@link Params.UseParams} <br>Used if only a few methods need to use the rule</li>
 *     <li>Class Annotation - {@link Params.AlwaysUseParams} <br> Used if most/all methods will use the rule. Exceptions should be marked with {@link Params.DoNotUseParams}</li>
 * </ol>
 *
 * @author rvail
 */
public class Params<T> implements TestRule {

    private final List<T> allParams;
    private final VerifiableErrorCollector errorCollector = new VerifiableErrorCollector();
    private Optional<T> currentParam = null;



    public static <E> Params<E> of(E... allParams) {
        return new Params<>(allParams);
    }

    public static <E extends Enum<E>> Params<E> of(Class<E> enumClass) {
        return new Params<>(enumClass.getEnumConstants());
    }

    private Params(List<T> allParams) {
        this.allParams = allParams;
    }

    private Params(T[] allParams) {
        this(Arrays.asList(allParams));
    }

    public T getCurrentParam() {
        if (currentParam == null) {
            throw new IllegalStateException("Params.getCurrentParam called from unannotated method/class");
        }
        return currentParam.get();
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        if (!shouldUseParams(description)) {
            return base;
        }
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {

                for (final T param : allParams) {
                    currentParam = Optional.of(param);
                    try {
                        base.evaluate();
                    } catch (Throwable t) {
                        errorCollector.addError(new ParamsAssertionError(currentParam.orNull(), t));
                    }
                }
                currentParam = null;
                errorCollector.verify();
            }
        };
    }

    private boolean shouldUseParams(final Description description) {
        final UseParams useParamsAnnotation = description.getAnnotation(UseParams.class);
        boolean testUsesParams = (useParamsAnnotation != null);
        if (!testUsesParams) {
            final AlwaysUseParams alwaysUseParams = description.getTestClass().getAnnotation(AlwaysUseParams.class);
            testUsesParams = (alwaysUseParams != null);
            if (testUsesParams) {
                testUsesParams = (description.getAnnotation(DoNotUseParams.class) == null);
            }
        }

        return testUsesParams;
    }

    /**
     * Method will use Params
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface UseParams {
    }


    /**
     * Method will not use Params
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface DoNotUseParams {
    }


    /**
     * All Methods in Class will use Params unless they are annotated with
     * {@link Params.DoNotUseParams}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface AlwaysUseParams {
    }


    public static class ParamsAssertionError extends AssertionError {
        ParamsAssertionError(Object param, Throwable t) {
            super(String.format("\nParam: %s\n%s", Objects.toString(param), t));
            // We don't care where this is thrown from we care about the passed in cause
            // use its stack trace
            this.setStackTrace(t.getStackTrace());
        }
    }


    /**
     * ErrorCollector.verify() is protected so extend ErrorCollector so we can call it.
     */
    private class VerifiableErrorCollector extends ErrorCollector {
        public void verify() throws Throwable {
            super.verify();
        }
    }

}

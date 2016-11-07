/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider.validation.impl;

import org.opendaylight.controller.packetcable.provider.validation.ValidatorProvider;
import org.opendaylight.controller.packetcable.provider.validation.ValidatorProviderFactory;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.AppValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.AppsValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.GateSpecValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.GateValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.GatesValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.SubscriberValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.SubscribersValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.TrafficProfileValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ClassifierContainerValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ClassifierValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ClassifiersValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.ExtClassifierValidator;
import org.opendaylight.controller.packetcable.provider.validation.impl.validators.qos.classifier.Ipv6ClassifierValidator;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.classifier.attributes.Classifiers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.classifier.attributes.classifiers.ClassifierContainer;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.classifier.Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.ext.classifier.ExtClassifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.Apps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.App;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.Subscribers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.Subscriber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.subscriber.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.ipv6.classifier.Ipv6Classifier;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev161107.pcmm.qos.traffic.profile.TrafficProfile;

/**
 * * A ValidatorProviderFactory that can provide validators for types under packetcable:qos.
 *
 * @author rvail
 */
public class QosValidatorProviderFactory implements ValidatorProviderFactory {

    @Override
    public ValidatorProvider build() {
        return addQosValidators(new ValidatorProviderImpl());
    }

    public static ValidatorProvider addQosValidators(ValidatorProvider provider) {
        provider.put(Apps.class, new AppsValidator());
        provider.put(App.class, new AppValidator());

        provider.put(Subscribers.class, new SubscribersValidator());
        provider.put(Subscriber.class, new SubscriberValidator());

        provider.put(Gates.class, new GatesValidator());
        provider.put(Gate.class, new GateValidator());
        provider.put(GateSpec.class, new GateSpecValidator());

        provider.put(TrafficProfile.class, new TrafficProfileValidator());

        provider.put(Classifiers.class, new ClassifiersValidator());
        provider.put(ClassifierContainer.class, new ClassifierContainerValidator());
        provider.put(Classifier.class, new ClassifierValidator());
        provider.put(ExtClassifier.class, new ExtClassifierValidator());
        provider.put(Ipv6Classifier.class, new Ipv6ClassifierValidator());


        return provider;
    }
}

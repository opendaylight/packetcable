/*
 * Copyright (c) 2015 Cable Television Laboratories, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.pcmm.gates.impl;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import java.util.Collections;
import org.pcmm.base.impl.PCMMBaseObject.SNum;
import org.pcmm.gates.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code
 * <Gate-set>=<Decision Header><TransactionID><AMID> <SubscriberID> [<GateI>]
 * <GateSpec> <Traffic Profile> <classifier>[<classifier>...]
 * }
 *
 */
public class PCMMGateReq implements IPCMMGate {

    public static final Logger logger = LoggerFactory.getLogger(PCMMGateReq.class);

    // Immutable references
    private final boolean multicast;
    private final IAMID iamid;
    private final ISubscriberID subscriberID;
    private transient ITransactionID transactionID;
    private transient IGateSpec gateSpec;
    private transient ITrafficProfile trafficProfile;
    private transient List<IClassifier> classifiers;

    // These values are transient as objects of these type will be updated asynchronously and will be used for
    // synchronization purposes
    private IGateID gateID;
    private IPCMMError error;

    /**
     * Constructor
     * @param iamid - the Application Manager ID
     * @param subscriberID - the Subscriber ID
     * @param transactionID - the transaction ID
     * @param gateSpec - the Gate specification
     * @param trafficProfile - the traffic profile
     * @param classifiers - the classifier
     * @param gateID - the gate ID
     * @param error - the error
     */
    public PCMMGateReq(IAMID iamid, ISubscriberID subscriberID, ITransactionID transactionID,
                       IGateSpec gateSpec, ITrafficProfile trafficProfile, List<IClassifier> classifiers, IGateID gateID,
                       IPCMMError error) {
        // TODO - determine if and when this attribute should be used
        this.multicast = false;

        this.iamid = iamid;
        this.subscriberID = subscriberID;
        this.transactionID = transactionID;
        this.gateSpec = gateSpec;
        this.trafficProfile = trafficProfile;
        this.classifiers = Lists.newArrayList(classifiers);
        this.gateID = gateID;
        this.error = error;
    }

    /**
     * Creates a PCMM Gate Request object from parsing a byte array
     * @param data - the data to parse
     * @return - the request
     */
    public static PCMMGateReq parse(byte[] data) {
        GateID gateID = null;
        AMID amid = null;
        SubscriberID subscriberID = null;
        TransactionID transactionID = null;
        GateSpec gateSpec = null;
        ITrafficProfile trafficProfile = null;
        List<IClassifier> classifiers = Lists.newArrayListWithExpectedSize(4);
        PCMMError error = null;

        short offset = 0;
        while (offset + 5 < data.length) {
            short len = 0;
            len |= ((short) data[offset]) << 8;
            len |= ((short) data[offset + 1]) & 0xFF;
            final SNum sNum = SNum.valueOf(data[offset + 2]);
            final byte sType = data[offset + 3];
            final int dataIndx = offset + 4;
            byte[] dataBuffer = Arrays.copyOfRange(data, dataIndx, dataIndx + len - 4);
            switch (sNum) {
                case GATE_ID:
                    gateID = GateID.parse(dataBuffer);
                    break;
                case AMID:
                    amid = AMID.parse(dataBuffer);
                    break;
                case SUBSCRIBER_ID:
                    subscriberID = SubscriberID.parse(dataBuffer);
                    break;
                case TRANSACTION_ID:
                    transactionID = TransactionID.parse(dataBuffer);
                    break;
                case GATE_SPEC:
                    gateSpec = GateSpec.parse(dataBuffer);
                    break;
                case TRAFFIC_PROFILE:
                    switch (sType) {
                        case DOCSISServiceClassNameTrafficProfile.STYPE:
                            trafficProfile = DOCSISServiceClassNameTrafficProfile.parse(dataBuffer);
                            break;
                        case BestEffortService.STYPE:
                            trafficProfile = BestEffortService.parse(dataBuffer);
                            break;
                    }
                    break;
                case CLASSIFIERS:
                    switch (sType) {
                        case IClassifier.STYPE:
                            classifiers.add(Classifier.parse(dataBuffer));
                            break;
                        case IExtendedClassifier.STYPE:
                            classifiers.add(ExtendedClassifier.parse(dataBuffer));
                            break;
                        case IIPv6Classifier.STYPE:
                            classifiers.add(IPv6Classifier.parse(dataBuffer));
                            break;
                    }
                    break;
                case PCMM_ERROR:
                    error = PCMMError.parse(dataBuffer);
                    break;
            default:
                logger.warn("Unhandled Object skept : S-NUM=" + sNum
                                   + "  S-TYPE=" + sType + "  LEN=" + len);
            }
            offset += len;
        }

        return new PCMMGateReq(amid, subscriberID, transactionID, gateSpec, trafficProfile, classifiers, gateID, error);
    }

    @Override
    public boolean isMulticast() {
        // TODO Auto-generated method stub
        return multicast;
    }

    @Override
    public void setGateID(IGateID gateid) {
        this.gateID = gateid;

    }

    @Override
    public void setTransactionID(ITransactionID transactionID) {
        this.transactionID = transactionID;

    }

    @Override
    public void setGateSpec(IGateSpec gateSpec) {
        this.gateSpec = gateSpec;
    }

    @Override
    public void setClassifiers(List<IClassifier> classifiers) {
        if (classifiers == null) {
            this.classifiers = null;
        }
        else {
            this.classifiers = new ArrayList<>(classifiers);
        }
    }

    @Override
    public void setTrafficProfile(ITrafficProfile profile) {
        this.trafficProfile = profile;
    }

    @Override
    public IGateID getGateID() {
        return gateID;
    }

    @Override
    public IAMID getAMID() {
        return iamid;
    }

    @Override
    public ISubscriberID getSubscriberID() {
        return subscriberID;
    }

    @Override
    public IGateSpec getGateSpec() {
        return gateSpec;
    }

    @Override
    public List<IClassifier> getClassifiers() {
        if (classifiers == null) {
            return null;
        }
        return Collections.unmodifiableList(classifiers);
    }

    @Override
    public ITrafficProfile getTrafficProfile() {
        return trafficProfile;
    }

    @Override
    public ITransactionID getTransactionID() {
        return transactionID;
    }

    public IPCMMError getError() {
        return error;
    }

    public void setError(IPCMMError error) {
        this.error = error;
    }

    @Override
    public byte[] getData() {
        final List<Byte> byteList = new ArrayList<>();
        if (getTransactionID() != null) {
            byteList.addAll(Bytes.asList(getTransactionID().getAsBinaryArray()));
        }
        if (getGateID() != null) {
            byteList.addAll(Bytes.asList(getGateID().getAsBinaryArray()));
        }
        if (getAMID() != null) {
            byteList.addAll(Bytes.asList(getAMID().getAsBinaryArray()));
        }
        if (getSubscriberID() != null) {
            byteList.addAll(Bytes.asList(getSubscriberID().getAsBinaryArray()));
        }
        if (getGateSpec() != null) {
            byteList.addAll(Bytes.asList(getGateSpec().getAsBinaryArray()));
        }
        if (getTrafficProfile() != null) {
            byteList.addAll(Bytes.asList(getTrafficProfile().getAsBinaryArray()));
        }
        if (getClassifiers() != null) {
            for (IClassifier classifier : getClassifiers()) {
                byteList.addAll(Bytes.asList(classifier.getAsBinaryArray()));
            }
        }
        return Bytes.toArray(byteList);
    }

/*
    private byte[] fill(byte[] array, IPCMMBaseObject obj) {
        byte[] a = obj.getAsBinaryArray();
        int offset = array.length;
        array = Arrays.copyOf(array, offset + a.length);
        System.arraycopy(a, 0, array, offset, a.length);
        return array;
    }
*/
}

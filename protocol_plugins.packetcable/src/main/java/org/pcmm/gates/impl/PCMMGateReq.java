/**
 @header@
 */
package org.pcmm.gates.impl;

import java.util.Arrays;

import org.pcmm.base.IPCMMBaseObject;
import org.pcmm.gates.IAMID;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IGateID;
import org.pcmm.gates.IGateSpec;
import org.pcmm.gates.IPCMMError;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ISubscriberID;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.gates.ITransactionID;

/**
 * <p>
 * <Gate-set>=<Decision Header><TransactionID><AMID> <SubscriberID> [<GateI>]
 * <GateSpec> <Traffic Profile> <classifier>
 * </p>
 */
public class PCMMGateReq implements IPCMMGate {

    private boolean multicast;
    private IGateID gateID;
    private IAMID iamid;
    private IPCMMError error;
    private ISubscriberID subscriberID;
    private ITransactionID transactionID;
    private IGateSpec gateSpec;
    private ITrafficProfile trafficProfile;
    private IClassifier classifier;

    public PCMMGateReq() {
    }

    public PCMMGateReq(byte[] data) {
        short len, offset;
        byte sNum, sType;
        len = offset = 0;
        sNum = sType = (byte) 0;
        while (offset + 5 < data.length) {
            len = 0;
            len |= ((short) data[offset]) << 8;
            len |= ((short) data[offset + 1]) & 0xFF;
            sNum = data[offset + 2];
            sType = data[offset + 3];
            byte[] dataBuffer = Arrays.copyOfRange(data, offset, offset + len);
            switch (sNum) {
            case IGateID.SNUM:
                setGateID(new GateID(dataBuffer));
                break;
            case IAMID.SNUM:
                setAMID(new AMID(dataBuffer));
                break;
            case ISubscriberID.SNUM:
                setSubscriberID(new SubscriberID(dataBuffer));
                break;
            case ITransactionID.SNUM:
                setTransactionID(new TransactionID(dataBuffer));
                break;
            case IGateSpec.SNUM:
                setGateSpec(new GateSpec(dataBuffer));
                break;
            case ITrafficProfile.SNUM:
                setTrafficProfile(new BestEffortService(dataBuffer));
                break;
            case IClassifier.SNUM:
                setClassifier(new Classifier(dataBuffer));
                break;
            case IPCMMError.SNUM:
                error = new PCMMError(dataBuffer);
                break;
            default:
                System.out.println("unhandled Object skept : S-NUM=" + sNum
                                   + "  S-TYPE=" + sType + "  LEN=" + len);
            }
            offset += len;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#isMulticast()
     */
    @Override
    public boolean isMulticast() {
        // TODO Auto-generated method stub
        return multicast;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#setGateID(short)
     */
    @Override
    public void setGateID(IGateID gateid) {
        this.gateID = gateid;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#setAMID(org.pcmm.gates.IAMID)
     */
    @Override
    public void setAMID(IAMID iamid) {
        this.iamid = iamid;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.pcmm.gates.IPCMMGate#getSubscriberID(org.pcmm.gates.ISubscriberID)
     */
    @Override
    public void setSubscriberID(ISubscriberID subscriberID) {
        this.subscriberID = subscriberID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getGateSpec(org.pcmm.gates.IGateSpec)
     */
    @Override
    public void setGateSpec(IGateSpec gateSpec) {
        this.gateSpec = gateSpec;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getClassifier(org.pcmm.gates.IClassifier)
     */
    @Override
    public void setClassifier(IClassifier classifier) {
        this.classifier = classifier;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.pcmm.gates.IPCMMGate#getTrafficProfile(org.pcmm.gates.ITrafficProfile
     * )
     */
    @Override
    public void setTrafficProfile(ITrafficProfile profile) {
        this.trafficProfile = profile;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getGateID()
     */
    @Override
    public IGateID getGateID() {
        return gateID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getAMID()
     */
    @Override
    public IAMID getAMID() {
        return iamid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getSubscriberID()
     */
    @Override
    public ISubscriberID getSubscriberID() {
        return subscriberID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getGateSpec()
     */
    @Override
    public IGateSpec getGateSpec() {
        return gateSpec;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getClassifier()
     */
    @Override
    public IClassifier getClassifier() {
        return classifier;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pcmm.gates.IPCMMGate#getTrafficProfile()
     */
    @Override
    public ITrafficProfile getTrafficProfile() {
        return trafficProfile;
    }

    @Override
    public void setTransactionID(ITransactionID transactionID) {
        this.transactionID = transactionID;
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
        byte[] array = new byte[0];
        if (getTransactionID() != null) {
            array = fill(array, getTransactionID());
        }
        if (getGateID() != null) {
            array = fill(array, getGateID());
        }
        if (getAMID() != null) {
            array = fill(array, getAMID());

        }
        if (getSubscriberID() != null) {
            array = fill(array, getSubscriberID());
        }
        if (getGateSpec() != null) {
            array = fill(array, getGateSpec());
        }
        if (getTrafficProfile() != null) {
            array = fill(array, getTrafficProfile());
        }
        if (getClassifier() != null) {
            array = fill(array, getClassifier());
        }
        return array;
    }

    private byte[] fill(byte[] array, IPCMMBaseObject obj) {
        byte[] a = obj.getAsBinaryArray();
        int offset = array.length;
        array = Arrays.copyOf(array, offset + a.length);
        System.arraycopy(a, 0, array, offset, a.length);
        return array;
    }
}

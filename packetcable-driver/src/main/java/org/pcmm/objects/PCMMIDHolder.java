/**
 @header@
 */
package org.pcmm.objects;

/**
 * this class holds and maps flow ID to PCMM gate ID and Transaction ID
 *
 */
public class PCMMIDHolder extends PCMMResource {

    /**
     * flow id.
     */
    private int flowID;
    /**
     * gate id.
     */
    private int gateID;
    /**
     * transaction id.
     */
    private short transactionID;

    public PCMMIDHolder(int flowID, int gateID, short transactionID) {
        this.flowID = flowID;
        this.gateID = gateID;
        this.transactionID = transactionID;

    }

    public PCMMIDHolder() {

    }

    public int getFlowID() {
        return flowID;
    }

    public void setFlowID(int flowID) {
        this.flowID = flowID;
    }

    public int getGateID() {
        return gateID;
    }

    public void setGateID(int gateID) {
        this.gateID = gateID;
    }

    public short getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(short transactionID) {
        this.transactionID = transactionID;
    }

}

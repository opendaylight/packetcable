/**
 @header@
 */
package org.pcmm.gates.impl;

import org.pcmm.base.impl.PCMMBaseObject;
import org.pcmm.gates.IGateSpec;
import org.pcmm.gates.ISessionClassID;

/**
 *
 */
public class GateSpec extends PCMMBaseObject implements IGateSpec {

    public GateSpec() {
        super(LENGTH, STYPE, SNUM);
    }

    public GateSpec(byte[] data) {
        super(data);
    }

    @Override
    public ISessionClassID getSessionClassID() {
        return new SessionClassID(getByte((short) 3));
    }

    @Override
    public void setSessionClassID(ISessionClassID id) {
        setByte(id.toSingleByte(), (short) 3);
    }

    @Override
    public Direction getDirection() {
        return Direction.valueOf(getByte((short) 0));
    }

    @Override
    public void setDirection(Direction direction) {
        setByte(direction.getValue(), (short) 0);
    }

    @Override
    public short getTimerT1() {
        return getShort((short) 4);
    }

    @Override
    public void setTimerT1(short authTimer) {
        setShort(authTimer, (short) 4);
    }

    @Override
    public short getTimerT2() {
        return getShort((short) 6);
    }

    @Override
    public void setTimerT2(short timer) {
        setShort(timer, (short) 6);

    }

    @Override
    public short getTimerT3() {
        return getShort((short) 8);
    }

    @Override
    public void setTimerT3(short t) {
        setShort(t, (short) 8);

    }

    @Override
    public short getTimerT4() {
        return getShort((short) 10);
    }

    @Override
    public void setTimerT4(short t) {
        setShort(t, (short) 10);
    }

    @Override
    public void setDSCP_TOSOverwrite(DSCPTOS dscpTos) {
        setByte(dscpTos.getValue(), (short) 1);
    }

    @Override
    public DSCPTOS getDSCP_TOSOverwrite() {
        return DSCPTOS.valueOf(getByte((short) 1));
    }

    @Override
    public byte getDSCP_TOSMask() {
        return getByte((short) 2);
    }

    @Override
    public void setDSCP_TOSMask(byte dscp_tos_mask) {
        setByte(dscp_tos_mask, (short) 2);
    }

}

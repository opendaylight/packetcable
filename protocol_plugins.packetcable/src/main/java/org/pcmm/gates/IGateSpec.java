/**
 @header@
 */


package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 * <p>
 * The GateSpec describes some high-level attributes of the Gate, and contains
 * information regarding the treatment of other objects specified in the Gate
 * message.
 * </p>
 *
 *
 *
 *
 */
public interface IGateSpec extends IPCMMBaseObject {

    static final byte SNUM = 5;
    static final byte STYPE = 1;
    static final short LENGTH = 16;

    /**
     * <p>
     * Direction indicates whether the Gate is for an upstream or downstream
     * flow. Depending on this direction, the CMTS MUST reserve and activate the
     * DOCSIS flows accordingly. For Multicast Gates the CMTS needs to only
     * support flows or gates in the downstream direction.
     * </p>
     *
     *
     */
    public enum Direction {

        UPSTREAM((byte) 1), DOWNSTREAM((byte) 0);

        private Direction(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        @Override
        public String toString() {
            switch (value) {
            case 1:
                return "Upstream";
            default:
                return "Downstream";
            }
        }

        private byte value;

        public static Direction valueOf(byte v) {
            switch (v) {
            case 0:
                return Direction.DOWNSTREAM;
            case 1:
                return Direction.UPSTREAM;
            default:
                throw new IllegalArgumentException("not supported value");
            }
        }

    };

    /**
     *
     */
    public enum DSCPTOS {

        ENABLE((byte) 1), OVERRIDE((byte) 0);

        private DSCPTOS(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        @Override
        public String toString() {
            switch (value) {
            case 1:
                return "Enable";
            default:
                return "Override";
            }
        }

        public static DSCPTOS valueOf(byte v) {
            switch (v) {
            case 0:
                return DSCPTOS.OVERRIDE;
            case 1:
                return DSCPTOS.ENABLE;
            default:
                throw new IllegalArgumentException("not supported value");
            }
        }

        private byte value;

    };

    /**
     * <p>
     * provides a way for the Application Manager and the Policy Server to group
     * Gates into different classes with different authorization
     * characteristics. For example, one could use the SessionClassID to
     * represent some prioritization or preemption scheme that would allow
     * either the Policy Server or the CMTS to preempt a pre-authorized Gate in
     * favor of allowing a new Gate with a higher priority to be authorized.
     * </p>
     *
     * @return session class ID;
     */
    ISessionClassID getSessionClassID();

    /**
     * <p>
     * sets the session class ID;
     * </p>
     * <p>
     * SessionClassID is a 1-byte unsigned integer value which identifies the
     * proper admission control policy or parameters to be applied for this
     * Gate. The SessionClassID is a bit field, defined as follows: Bit 0-2:
     * Priority, a number from 0 to 7, where 0 is low priority and 7 is high.
     * Bit 3: Preemption, set to enable preemption of bandwidth allocated to
     * lower priority sessions if necessary (if supported). Bit 4-7:
     * Configurable, default to 0
     * </p>
     */
    void setSessionClassID(ISessionClassID id);

    /**
     *
     * @return direction.
     */
    Direction getDirection();

    /**
     * sets the direction
     *
     * @param direction
     *            Direction
     */
    void setDirection(Direction direction);

    /**
     * Authorized Timer limits the amount of time the authorization must remain
     * valid before it is reserved
     *
     * @return time in ms;
     */
    short getTimerT1();

    /**
     * sets the authorized timer
     *
     * @param authTimer
     *            : authorized timer
     */
    void setTimerT1(short authTimer);

    /**
     * Reserved Timer limits the amount of time the reservation must remain
     * valid before the resources are committed
     *
     * @return time in ms;
     */
    short getTimerT2();

    /**
     * sets the reserved timer.
     *
     * @param timer
     */
    void setTimerT2(short timer);

    /**
     * Committed Timer limits the amount of time a committed service flow may
     * remain idle.
     *
     * @return time in ms;
     */
    short getTimerT3();

    /**
     * sets the committed timer.
     *
     * @param t
     *            timer
     */
    void setTimerT3(short t);

    /**
     * Committed Recovery Timer limits the amount of time that a committed
     * service flow can remain without a subsequent refresh message from the
     * PS/AM once the PS/AM has been notified of inactivity
     *
     * @return time in ms;
     */
    short getTimerT4();

    /**
     * sets the Committed Recovery Timer.
     *
     * @param t
     *            timer
     */
    void setTimerT4(short t);

    /**
     *
     * @param dscpTos
     */
    void setDSCP_TOSOverwrite(DSCPTOS dscpTos);

    /**
     *
     * @return DSCP/TOS
     */
    DSCPTOS getDSCP_TOSOverwrite();

    /**
     *
     * @return
     */
    byte getDSCP_TOSMask();

    /**
     *
     * @param dscp_tos_mask
     */
    void setDSCP_TOSMask(byte dscp_tos_mask);

}

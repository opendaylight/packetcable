/**
 @header@
 */


package org.pcmm.gates;

import org.pcmm.base.IPCMMBaseObject;

/**
 * <p>
 * The AMID consists of two fields: the Application Manager Tag and Application
 * Type. Each Application Manager is pre-provisioned with an Application Manager
 * Tag that is unique within the universe of a single service provider. The
 * Application Manager may also be pre-provisioned with a set of Application
 * Type values that can be used to identify the particular application that a
 * gate is associated with. The Application Manager includes the AMID in all
 * messages that it issues to the Policy Server. The Policy Server transparently
 * passes this information to the CMTS via Gate Control messages. The CMTS MUST
 * return the AMID associated with the Gate to the Policy Server. The Policy
 * Server uses this information to associate Gate messages with a particular
 * Application Manager and Application Type.
 * </p>
 * <p>
 * The Application Manager Tag MUST be a globally unique value assigned to the
 * Application Manager by the service provider. The Application Manager MUST use
 * the assigned Application Manager Tag in all its interactions with Policy
 * Servers. Note that since the Application Manager may be operated by a third
 * party, and a single Application Manager could interact with multiple service
 * provider operators, a single physical Application Manager may be provisioned
 * with multiple Application Manager Tags, and multiple Application Type sets
 * (one for each configured Application Manager Tag).
 * </p>
 *
 *
 */
public interface IAMID extends IPCMMBaseObject {

    static final short LENGTH = 8;
    static final byte SNUM = 2;
    static final byte STYPE = 1;

    void setApplicationType(short type);

    short getApplicationType();

    void setApplicationMgrTag(short type);

    short getApplicationMgrTag();

}

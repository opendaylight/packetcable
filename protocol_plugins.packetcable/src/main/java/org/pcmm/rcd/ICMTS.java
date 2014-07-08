/**
 @header@
 */

package org.pcmm.rcd;

/**
 * <p>
 * In describing the role of the CMTS network element, it is important to
 * consider the relation among DOCSIS, PacketCable 1.x and PacketCable
 * Multimedia functionality. While each of these suites of specifications
 * addresses a specific set of functional requirements, each has also been
 * defined in such a way that corresponding implementations may be constructed
 * in a modular manner; either PacketCable 1.x or PacketCable Multimedia Gate
 * Control may be layered on top of a DOCSIS 1.1 or greater CMTS foundation,
 * with the option of adding additional, complementary functionality as business
 * indicates. Further, it should be emphasized that it is a significant asset of
 * the PacketCable architecture that both telephony and Multimedia variants
 * employ considerable architectural similarity, leading to potential reuse in
 * the underlying Gate management models.
 * </p>
 * <p>
 * The PacketCable Multimedia CMTS is a generalized version of the PacketCable
 * 1.x CMTS that has been defined in order to deliver telephony services in
 * PacketCable 1.x networks. The CMTS is responsible for fulfilling requests for
 * QoS that are received from one or more Policy Servers. It performs this
 * function by installing Gates, which are similar to the Gates defined in [14];
 * Gates allow the subscriber's cable modem to request network resources from
 * the CMTS through the creation of dynamic DOCSIS flows with guaranteed levels
 * of QoS. The CMTS also sends Event Messages detailing actual usage of QoS
 * resources to the Record Keeping Server.
 * </p>
 * <p>
 * The CMTS acts as a server (PS should send OPN message to CMTS to initiate
 * communication), and acts as a client for the rest of the exchange process.
 * </p>
 * 
 * 
 */
public interface ICMTS extends IPCMMServer {

	// generates a GateID and assigns it to the IPCMMGate.

	static final short DEFAULT_R_TYPE = (short) 0x08;
	static final short DEFAULT_M_TYPE = (short) 0;

}

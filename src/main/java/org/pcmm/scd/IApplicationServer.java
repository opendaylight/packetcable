/**
 @header@
 */

package org.pcmm.scd;

/**
 * <p>
 * The Application Server is a network entity that interfaces with the
 * Application Manager that requests PacketCable Multimedia services on behalf
 * of clients. The AS may reside on the MSO's network or it may reside outside
 * of this domain and interact with the MSO network via a particular trust
 * relationship. Similarly, the AS may be under direct control of the operator
 * or it may be controlled by a third-party. Any given AS may communicate with
 * one or more Application Managers.
 * </p>
 * <p>
 * The AS will communicate with a client via a signaling protocol that is
 * outside the scope of this specification. Using this unspecified protocol, the
 * Domain policies. For client requests that pass these checks, the AS
 * determines the particular QoS parameters necessary to deliver the service to
 * the client, based upon its knowledge of the requested service. It then sends
 * a request for these resources to the appropriate Application Manager, which
 * may deny the request based upon additional Service Control Domain policies or
 * may pass the request on to the Policy Server.
 * </p>
 * 
 * 
 */
public interface IApplicationServer {

	/**
	 * sets the Application Server's id
	 * 
	 * @param id
	 *            : the id of the AS
	 */
	void setASId(String id);

	/**
	 * gets the AS id
	 * 
	 * @return AS id
	 */
	String getASId();

}

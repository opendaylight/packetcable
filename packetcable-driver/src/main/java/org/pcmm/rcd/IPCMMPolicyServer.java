/**
 @header@
 */

package org.pcmm.rcd;

import java.net.InetAddress;

import org.pcmm.objects.MMVersionInfo;
import org.pcmm.state.IStateful;

/**
 * <i>PKT-SP-MM-I05-091029 PacketCableTM Specification</i>
 * <p>
 * As discussed in RFC 2753 [11], the policy management framework underlying
 * PacketCable Multimedia is based on the work of the IETF's Resource Allocation
 * Protocol (RAP) working group. Since the Policy Server is situated between the
 * Application Manager and the CMTS, it simultaneously plays a dual role as a
 * "proxy" for AM-initiated session requests and as a "sentry" for defining and
 * enforcing Resource Control Domain policy. As described in [11] and in keeping
 * with the PacketCable 1.x DQoS model, the Policy Server serves as Policy
 * Decision Point (PDP) in relation to the CMTS in that the Policy Server
 * implements MSO-defined authorization and resource-management procedures.
 * Conversely, the Policy Server assumes the role of Policy Enforcement Point
 * (PEP) in relation to the Application Manager as it proxies Gate Control
 * messages to and from the CMTS element. To revisit the interaction scenario,
 * the Application Manager issues policy requests to the Policy Server. The
 * Policy Server acting as a "sentry" for these requests, and applies a set of
 * policy rules that have been pre-provisioned by the MSO. Upon passing the
 * checks, the Policy Server then acts as a "proxy" with respect to the
 * Application Manager and the CMTS, forwarding the policy request and returning
 * any associated response. Each policy request transaction must be processed
 * individually. Policy decisions may be based on a number of factors, such as:
 * <ul>
 * <li>Parameters associated with the request and the status of available
 * resources</li>
 * <li>Identity of the particular client and associated profile information</li>
 * <li>Application parameters</li>
 * <li>Security considerations</li>
 * <li>Time-of-day</li>
 * </ul>
 * The primary functions of the Policy Server include:
 * <ul>
 * <li>A policy decision request mechanism, invoked by Application Managers</li>
 * <li>A policy decision request 'policing' mechanism, enforcing installed
 * Policy Rules</li>
 * <li>A policy decision delivery mechanism, used to install policy decisions on
 * the CMTS</li>
 * <li>A mechanism to allow for the proxying of QoS management messages to the
 * CMTS on behalf of the Application Manager</li>
 * <li>An event recording interface to a Record Keeping Server that is used to
 * log policy requests, which may in turn be correlated with network resource
 * usage records</li>·
 * </ul>
 * Since the Policy Server functions as a proxy between the AM and CMTS elements
 * (with complementary client and server interfaces) some MSOs may elect to
 * deploy multiple layers of Policy Servers and to delegate certain policy
 * decisions among these servers in order to satisfy requirements associated
 * with scalability and fault-tolerance.
 * </p>
 * <p>
 * <i>Stateful & Stateless Policy Servers</i> There are two basic classes of
 * Policy Servers – Stateful and Stateless. A Stateless Policy Server is a
 * slight misnomer since it does maintain enough state to map Application
 * Manager requests to the proper CMTS and maintain COPS ￼session state, while a
 * pure Stateless Policy Server maintains no state on any of the media sessions.
 * Stateful Policy Servers come in several varieties – some participate in
 * admission control and thus monitor the QoS attributes of active media
 * sessions, some leave QoS and admission control to the CMTS but monitor
 * time-based or volume-based service requests from the Application Manager, and
 * some Policy Servers are somewhere between these extremes. The reason there is
 * a variety of Policy Server types is that there is a variety of environments
 * that operators are trying to support. For example, some operators may wish to
 * support PacketCable Multimedia over the same CMTSs that they use for
 * PacketCable telephony, and they may want a single CMS/Policy Server that has
 * a more global view of the network resources being used. On the other hand,
 * some operators may wish to run a PacketCable Multimedia- only environment, or
 * they may utilize simpler CMTS-driven mechanisms for partitioning PacketCable
 * Multimedia and telephony resources. These simpler configurations have more
 * modest requirements on the amount of state that a Policy Server maintains.
 * Policy Server state requirements can also be driven by the level of trust
 * between the Policy Server and Application Manager; a Stateful Policy Server
 * can more readily police Application Manager session control behavior than can
 * a Stateless Policy Server. So a Stateful Policy Server may be more
 * appropriate for operators supporting third party Application Managers. Other
 * operators may rely on economics to enforce their trust relationships with
 * Application Managers, or they may control the Application Managers
 * themselves. In such cases a Stateless Policy Server may be more appropriate.
 * Since it is impossible to categorize all the various components of media
 * session and network QoS state that a Policy Server is maintaining, the
 * protocol is designed to be independent of this complexity. A Stateful Policy
 * Server gleans PacketCable Multimedia media session information from the
 * Application Manager requests it proxies; any other information it requires is
 * gathered via mechanisms that are outside the scope of this specification. The
 * CMTS and the Application Manager make no distinction as to the type of Policy
 * Server to which they are connected, and the protocol is designed in such a
 * manner that the type of Policy Server is transparent to the end point. The
 * type of Policy Server is only of importance to the operator. Since some types
 * of Policy Servers attempt to assist with admission control and may have a
 * larger view of the network and its resources, additional state
 * synchronization issues may arise in design in a network which contains more
 * than one of these types of Policy Servers. It is the responsibility of the
 * operator to ensure that the efforts of these Policy Servers are not
 * undermined by a network that includes other autonomous Policy Servers.
 * </p>
 * <p>
 * <i>Modification of Requests and Responses by Policy Servers</i> Although
 * nominally a part of the Resource Control Domain, the Policy Server can be an
 * intermediary between the Service and the Resource Control Domains, in
 * addition to its normal role of implementing MSO-defined authorization and
 * resource management procedures. In either of these capacities it may modify
 * the incoming request before forwarding it to the CMTS. In acting as an
 * intermediary between the SCD and RCD, the Policy Server may translate fields
 * from formats or scales used in the SCD into formats or scales used in the
 * RCD. For example, the Policy Server may modify the "priority" of a request
 * coming from an Application Manager (especially important to do for an AM
 * outside of the MSO network) so that this priority field uses a consistent
 * scale throughout the operator's RCD. In its capacity as an intermediary, the
 * Policy Server may use bidirectional translation – in other words, it should
 * translate requests from the AM to the CMTS and "untranslate" the responses
 * from the CMTS to the AM. This capability can be supported by stateful policy
 * servers by remembering the original request, and it can be supported by
 * stateless Policy Servers if the translation function is invertible.
 * Modification of certain objects, specifically the Classifier and Traffic
 * Profile objects, may cause operational problems in the originating AM. As
 * such, these objects MUST NOT be modified by the policy server. Aside from
 * these exceptions, all other objects may be policed and modified at the PS's
 * discretion based on provisioned policy rules.
 * </p>
 * 
 */
public interface IPCMMPolicyServer extends IPCMMServer, IStateful {

	/**
	 * establishes COPS connection with the CMTS
	 * 
	 * @param host
	 *            : remote host name or ip address
	 * @return connected socket.
	 */
	IPSCMTSClient requestCMTSConnection(String host);

	/**
	 * establishes COPS connection with the CMTS
	 * 
	 * @param host
	 *            : remote ip address‚
	 * @return connected socket.
	 */
	IPSCMTSClient requestCMTSConnection(InetAddress host);

	/**
	 * <p>
	 * In the PacketCable model, the CMTS (PEP) is the one that listens on the
	 * assigned port 3918, and it is the Policy Server that MUST initiate the
	 * TCP connection to the CMTS, thus we implement the IPCMMClient interface.
	 * </p>
	 */
	public static interface IPSCMTSClient extends IPCMMClient {

		/**
		 * 
		 * @return Classifier Id.
		 */
		short getClassifierId();

		/**
		 * 
		 * @return the transaction Id.
		 */
		short getTransactionId();

		/**
		 * Gate id transmitted by the CMTS to the PS.
		 * 
		 * @return the Gate Id.
		 */
		int getGateId();

		/**
		 * initiates a Gate-Set with the CMTS
		 * 
		 * @return
		 */
		boolean gateSet();

		/**
		 * initiates a Gate-Info with the CMTS
		 * 
		 * @return
		 */
		boolean gateInfo();

		/**
		 * initiates a Gate-Delete with the CMTS
		 * 
		 * @return
		 */
		boolean gateDelete();

		/**
		 * sends synch request
		 * 
		 * @return
		 */
		boolean gateSynchronize();

		/**
		 * Sets the value of the multi-media version info.
		 * 
		 * @param MM
		 *            version info
		 */
		void setVersionInfo(MMVersionInfo vInfo);

		/**
		 * 
		 * @return MM version info
		 */
		MMVersionInfo getVersionInfo();
	}
}

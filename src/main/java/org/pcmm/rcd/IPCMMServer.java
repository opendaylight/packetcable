/**
 @header@
 */


package org.pcmm.rcd;

import org.pcmm.concurrent.IWorker;
import org.pcmm.state.IStateful;

/**
 * <p>
 * As discussed in RFC 2753 [11], the policy management framework underlying
 * PacketCable Multimedia is based on the work of the IETF's Resource Allocation
 * Protocol (RAP) working group. Since the Policy Server is situated between the
 * Application Manager and the CMTS, it simultaneously plays a dual role as a
 * "proxy" for AM-initiated session requests and as a "sentry" for defining and
 * enforcing Resource Control Domain policy.
 * </p>
 * <p>
 * As described in [11] and in keeping with the PacketCable 1.x DQoS model, the
 * Policy Server serves as Policy Decision Point (PDP) in relation to the CMTS
 * resource-management procedures. Conversely, the Policy Server assumes the
 * role of Policy Enforcement Point (PEP) in relation to the Application Manager
 * as it proxies Gate Control messages to and from the CMTS element.
 * </p>
 * <p>
 * To revisit the interaction scenario, the Application Manager issues policy
 * requests to the Policy Server. The Policy Server acting as a "sentry" for
 * these requests, and applies a set of policy rules that have been
 * pre-provisioned by the MSO. Upon passing the checks, the Policy Server then
 * acts as a "proxy" with respect to the Application Manager and the CMTS,
 * forwarding the policy request and returning any associated response. Each
 * policy request transaction must be processed individually.
 * </p>
 * <p>
 * Policy decisions may be based on a number of factors, such as:
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
 * usage records</li>
 * </ul>
 * <p>
 * Since the Policy Server functions as a proxy between the AM and CMTS elements
 * (with complementary client and server interfaces) some MSOs may elect to
 * deploy multiple layers of Policy Servers and to delegate certain policy
 * decisions among these servers in order to satisfy requirements associated
 * with scalability and fault-tolerance.
 * </p>
 * </p>
 *
 *
 */
public interface IPCMMServer extends IStateful {

    /**
     *
     */
    void startServer();

    /**
     *
     */
    void stopServer();

    /**
     * When a client connects to the server, a handler is needed to manage the
     * exchange of the messages between this client and the server.
     *
     *
     */
    public static interface IPCMMClientHandler extends IWorker, IPCMMClient {

    }
}

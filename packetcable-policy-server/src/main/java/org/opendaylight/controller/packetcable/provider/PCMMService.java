package org.opendaylight.controller.packetcable.provider;

import com.google.common.collect.Maps;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.Gates;
import org.pcmm.PCMMPdpAgent;
import org.pcmm.PCMMPdpDataProcess;
import org.pcmm.PCMMPdpMsgSender;
import org.pcmm.gates.impl.PCMMGateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSError.ErrorTypes;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetAddress;
import java.util.Map;

/**
 * Class responsible for managing the gates for a single CCAP.
 */
@ThreadSafe
public class PCMMService {
	private Logger logger = LoggerFactory.getLogger(PCMMService.class);

	private final Ccaps ccap;
	private final IpAddress ipAddr;
	private final PortNumber portNum;
	protected final CcapClient ccapClient;
	protected Map<String, PCMMGateReq> gateRequests = Maps.newConcurrentMap();

	private final short clientType;

	public PCMMService(final short clientType, final Ccaps ccap) {
		this.clientType = clientType;
		this.ccap = ccap;
		ipAddr = ccap.getConnection().getIpAddress();
		portNum = ccap.getConnection().getPort();
		ccapClient = new CcapClient(ipAddr, portNum);
		logger.info("Attempting to add CCAP with ID {} @ {}:{}", ccap.getCcapId(), ipAddr.getIpv4Address().getValue(), portNum.getValue());
	}

	public void disconect() {
		ccapClient.disconnect();
	}

	// TODO - try and change the return to something other than a String to be parsed to determine success
	public String addCcap() {
		ccapClient.connect();
		if (ccapClient.isConnected) {
			logger.info("Connected to CCAP with ID - " + ccap.getCcapId());
			return String.format("200 OK - CCAP %s connected @ %s:%d", ccap.getCcapId(),
					ipAddr.getIpv4Address().getValue(), portNum.getValue());
		} else {
			return String.format("404 Not Found - CCAP %s failed to connect @ %s:%d - %s",
					ccap.getCcapId(),
					ipAddr.getIpv4Address().getValue(), portNum.getValue(), ccapClient.errMessage);
		}
	}

	// TODO - Consider creating an object to return that contains a success flag, message, and gate ID or gate object
	// TODO FIXME - the gate appears to be getting set as per restconf but I am not seeing the proper logging occurring
	public String sendGateSet(final String gatePathStr, final InetAddress subId, final Gates qosGate,
							  final ServiceFlowDirection scnDir) {
		logger.info("Sending gate to CCAP with ID - " + ccap.getCcapId());
		// assemble the gate request for this subId
		final PCMMGateReqBuilder gateBuilder = new PCMMGateReqBuilder();
		gateBuilder.build(ccap.getAmId());
		gateBuilder.build(subId);
		// force gateSpec.Direction to align with SCN direction
		final ServiceClassName scn = qosGate.getTrafficProfile().getServiceClassName();
		if (scn != null) {
			gateBuilder.build(qosGate.getGateSpec(), scnDir);
		} else {
			// not an SCN gate
			gateBuilder.build(qosGate.getGateSpec(), null);
		}
		gateBuilder.build(qosGate.getTrafficProfile());

		// pick a classifier type (only one for now)
		if (qosGate.getClassifier() != null) {
			gateBuilder.build(qosGate.getClassifier());
		} else if (qosGate.getExtClassifier() != null) {
			gateBuilder.build(qosGate.getExtClassifier());
		} else if (qosGate.getIpv6Classifier() != null) {
			gateBuilder.build(qosGate.getIpv6Classifier());
		}
		// assemble the final gate request
		final PCMMGateReq gateReq = gateBuilder.getGateReq();

		// and remember it
		gateRequests.put(gatePathStr, gateReq);
		// and send it to the CCAP
		ccapClient.sendGateSet(gateReq);
		// and wait for the COPS response to complete processing gate request
		try {
			// TODO - see PCMMPdpReqStateMan#processReport() gate.notify(). Should determine a better means to
			// TODO - handle this synchronization.
			// TODO - if not changing this, may want to make this timeout configurable
			synchronized(gateReq) {
				logger.info("Waiting 1000ms for gate request to be updated");
				gateReq.wait(1000);
				logger.debug("Gate request error - " + gateReq.getError());
				logger.debug("Gate request ID - " + gateReq.getGateID());
			}
		} catch (Exception e) {
			logger.error("PCMMService: sendGateSet(): gate response timeout exceeded for "
					+ gatePathStr + '/' + gateReq, e);
			return String.format("408 Request Timeout - gate response timeout exceeded for %s/%s",
					ccap.getCcapId(), gatePathStr);
		}
		if (gateReq.getError() != null) {
			logger.error("PCMMService: sendGateSet(): returned error: {}",
					gateReq.getError().toString());
			return String.format("404 Not Found - sendGateSet for %s/%s returned error - %s",
					ccap.getCcapId(), gatePathStr, gateReq.getError().toString());
		} else {
			if (gateReq.getGateID() != null) {
				logger.info(String.format("PCMMService: sendGateSet(): returned GateId %08x: ",
						gateReq.getGateID().getGateID()));
				return String.format("200 OK - sendGateSet for %s/%s returned GateId %08x",
						ccap.getCcapId(), gatePathStr, gateReq.getGateID().getGateID());
			} else {
				logger.info("PCMMService: sendGateSet(): no gateId returned:");
				return String.format("404 Not Found - sendGateSet for %s/%s no gateId returned",
						ccap.getCcapId(), gatePathStr);
			}
		}
	}

	public Boolean sendGateDelete(final String gatePathStr) {
		logger.info("sendGateDelete() - " + ccap);
		// recover the original gate request
		final PCMMGateReq gateReq = gateRequests.remove(gatePathStr);
		if (gateReq != null) {
			ccapClient.sendGateDelete(gateReq);
			// and wait for the response to complete
			try {
				// TODO - see PCMMPdpReqStateMan#processReport() gate.notify(). Should determine a better means to
				// TODO - handle this synchronization.
				synchronized(gateReq) {
					gateReq.wait(1000);
				}
			} catch (InterruptedException e) {
				logger.error("PCMMService: sendGateDelete(): gate response timeout exceeded for {}/{}",
						gatePathStr, gateReq);
			}
			if (gateReq.getError() != null) {
				logger.warn("PCMMService: sendGateDelete(): returned error: {}", gateReq.getError().toString());
				return false;
			} else {
				if (gateReq.getGateID() != null) {
					logger.info(String.format("PCMMService: sendGateDelete(): deleted GateId %08x: ", gateReq.getGateID().getGateID()));
				} else {
					logger.error("PCMMService: sendGateDelete(): deleted but no gateId returned");
				}
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Used to interface with a CCAP (including CMTSs)
	 */
	protected class CcapClient {
		public final PCMMPdpDataProcess pcmmProcess;
		public final PCMMPdpAgent pcmmPdp;

		private final String ipv4;
		private final Integer port;

		// Needs to be initialized in connect() method else would be final
		protected transient PCMMPdpMsgSender pcmmSender;

		private transient Boolean isConnected = false;
		private transient String errMessage = null;

		/**
		 * Constructor
		 * @param ccapIp - the IP of the CCAP to manage
		 * @param portNum - the port number of the CCAP to manage
		 */
		public CcapClient(final IpAddress ccapIp, final PortNumber portNum) {
			ipv4 = ccapIp.getIpv4Address().getValue();
			if (portNum != null)  port = portNum.getValue();
			else port = PCMMPdpAgent.WELL_KNOWN_PDP_PORT;
			// TODO FIXME - if this object is not null, gate processing will not work correctly
			// TODO see - PCMMPdpReqStateMan#processReport() where the report type is success and the process is null
			//            pcmmProcess = new PCMMPdpDataProcess();
			pcmmProcess = null;
			pcmmPdp = new PCMMPdpAgent(ipv4, port, clientType, pcmmProcess);
		}

		/**
		 * Starts the connection to the CCAP
		 */
		public void connect( ) {
			logger.info("Attempting to connect to host: " + ipv4 + " port: " + port);
			try  {
				pcmmPdp.connect();

				// Cannot instantiate until after pcmmPdp.connect() is called as this is where the client handle is created
				pcmmSender = new PCMMPdpMsgSender(clientType, pcmmPdp.getClientHandle(), pcmmPdp.getSocket());

				isConnected = true;
			} catch (Exception e) {
				isConnected = false;
				logger.error("Failed to connect to host: " + ipv4 + " port: " + port, e);
				errMessage = e.getMessage();
			}
		}

		public void disconnect() {
			logger.info("CcapClient: disconnect(): {}:{}", ipv4, port);
			pcmmPdp.disconnect(new COPSError(ErrorTypes.SHUTTING_DOWN, ErrorTypes.NA));
			isConnected = false;
		}

		// TODO - consider returning a new PCMMGateReq object or a future here instead of setting the ID on the old
		// TODO - request by reference which makes the code more convoluted thus making issues more difficult to track down.
		public Boolean sendGateSet(final PCMMGateReq gateReq) {
			logger.info("CcapClient: sendGateSet(): {}:{} => {}", ipv4, port, gateReq);
			try {
				pcmmSender.sendGateSet(gateReq);

				// TODO - determine if this is the correct place to perform this operation as this currently is the
				// TODO - place where the gate ID can be set on the gateReq object
				//                pcmmSender.handleGateReport(pcmmPdp.getSocket());
			} catch (COPSPdpException e) {
				logger.error("CcapClient: sendGateSet(): {}:{} => {} FAILED:", ipv4, port, gateReq, e);
			}
			// and save it back to the gateRequest object for gate delete later
			gateReq.setGateID(pcmmSender.getGateID());

			// TODO - determine why this method is always returning true???
			return true;
		}

		public Boolean sendGateDelete(final PCMMGateReq gateReq) {
			logger.info("CcapClient: sendGateDelete(): {}:{} => {}", ipv4, port, gateReq);
			try {
				pcmmSender.sendGateDelete(gateReq);
			} catch (COPSPdpException e) {
				logger.error("CcapClient: sendGateDelete(): {}:{} => {} FAILED: {}", ipv4, port, gateReq, e.getMessage());
			}
			return true;
		}
	}
}


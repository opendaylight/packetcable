/**
 * @header@
 */
package org.pcmm.rcd.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.pcmm.PCMMConstants;
import org.pcmm.PCMMGlobalConfig;
import org.pcmm.PCMMProperties;
import org.pcmm.gates.IAMID;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.IGateID;
import org.pcmm.gates.IGateSpec;
import org.pcmm.gates.IGateSpec.DSCPTOS;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.IPCMMError;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ISubscriberID;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.AMID;
import org.pcmm.gates.impl.BestEffortService;
import org.pcmm.gates.impl.Classifier;
import org.pcmm.gates.impl.ExtendedClassifier;
import org.pcmm.gates.impl.GateID;
import org.pcmm.gates.impl.GateSpec;
import org.pcmm.gates.impl.PCMMError;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.SubscriberID;
import org.pcmm.gates.impl.TransactionID;
import org.pcmm.messages.IMessage.MessageProperties;
import org.pcmm.messages.impl.MessageFactory;
import org.pcmm.objects.MMVersionInfo;
import org.pcmm.rcd.IPCMMPolicyServer;
import org.pcmm.utils.PCMMException;
import org.umu.cops.prpdp.COPSPdpConnection;
import org.umu.cops.prpdp.COPSPdpDataProcess;
import org.umu.cops.stack.COPSClientAcceptMsg;
import org.umu.cops.stack.COPSClientCloseMsg;
import org.umu.cops.stack.COPSClientOpenMsg;
import org.umu.cops.stack.COPSClientSI;
import org.umu.cops.stack.COPSData;
import org.umu.cops.stack.COPSDecision;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSException;
import org.umu.cops.stack.COPSHeader;
import org.umu.cops.stack.COPSMsg;
import org.umu.cops.stack.COPSReportMsg;
import org.umu.cops.stack.COPSReqMsg;

/**
 * 
 * PCMM policy server
 * 
 */
public class PCMMPolicyServer extends AbstractPCMMServer implements
		IPCMMPolicyServer {
	/**
	 * since PCMMPolicyServer can connect to multiple CMTS (PEP) we need to
	 * manage each connection in a separate thread.
	 */

	public PCMMPolicyServer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pcmm.rcd.IPCMMPolicyServer#requestCMTSConnection(java.lang.String)
	 */
	public IPSCMTSClient requestCMTSConnection(String host) {
		try {
			InetAddress address = InetAddress.getByName(host);
			return requestCMTSConnection(address);
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pcmm.rcd.IPCMMPolicyServer#requestCMTSConnection(java.net.InetAddress
	 * )
	 */
	public IPSCMTSClient requestCMTSConnection(InetAddress host) {
		IPSCMTSClient client = new PSCMTSClient();
		try {
			if (client.tryConnect(host, PCMMProperties.get(PCMMConstants.PCMM_PORT, Integer.class))) {
				boolean endNegotiation = false;
				while (!endNegotiation) {
					logger.debug("waiting for OPN message from CMTS");
					COPSMsg opnMessage = client.readMessage();
					// Client-Close
					if (opnMessage.getHeader().isAClientClose()) {
						COPSError error = ((COPSClientCloseMsg) opnMessage).getError();
						logger.debug("CMTS requetsed Client-Close");
						throw new PCMMException(new PCMMError(error.getErrCode(), error.getErrSubCode()));
					} else // Client-Open
					if (opnMessage.getHeader().isAClientOpen()) {
						logger.debug("OPN message received from CMTS");
						COPSClientOpenMsg opn = (COPSClientOpenMsg) opnMessage;
						if (opn.getClientSI() == null)
							throw new COPSException("CMTS shoud have sent MM version info in Client-Open message");
						else {
							// set the version info
							MMVersionInfo vInfo = new MMVersionInfo(opn.getClientSI().getData().getData());
							client.setVersionInfo(vInfo);
							logger.debug("CMTS sent MMVersion info : major:" + vInfo.getMajorVersionNB() + "  minor:" + vInfo.getMinorVersionNB()); //
							if (client.getVersionInfo().getMajorVersionNB() == client.getVersionInfo().getMinorVersionNB()) {
								// send a CC since CMTS has exhausted all
								// protocol selection attempts
								throw new COPSException("CMTS exhausted all protocol selection attempts");
							}
						}
						// send CAT response
						Properties prop = new Properties();
						logger.debug("send CAT to the CMTS ");
						COPSMsg catMsg = MessageFactory.getInstance().create(COPSHeader.COPS_OP_CAT, prop);
						client.sendRequest(catMsg);
						// wait for REQ msg
						COPSMsg reqMsg = client.readMessage();
						// Client-Close
						if (reqMsg.getHeader().isAClientClose()) {
							COPSError error = ((COPSClientCloseMsg) opnMessage).getError();
							logger.debug("CMTS requetsed Client-Close");
							throw new PCMMException(new PCMMError(error.getErrCode(), error.getErrSubCode()));
						} else // Request
						if (reqMsg.getHeader().isARequest()) {
							logger.debug("Received REQ message form CMTS");
							// end connection attempts
							COPSReqMsg req = (COPSReqMsg) reqMsg;
							// set the client handle to be used later by the
							// gate-set
							client.setClientHandle(req.getClientHandle().getId().str());
							COPSPdpDataProcess processor = null;
							COPSPdpConnection copsPdpConnection = new COPSPdpConnection(opn.getPepId(), ((AbstractPCMMClient) client).getSocket(), processor);
							copsPdpConnection.setKaTimer(((COPSClientAcceptMsg) catMsg).getKATimer().getTimerVal());
							pool.schedule(pool.adapt(copsPdpConnection));
							endNegotiation = true;
						} else
							throw new COPSException("Can't understand request");
					} else {
						throw new COPSException("Can't understand request");
					}
				}
			}
			// else raise exception.
		} catch (Exception e) {
			logger.error(e.getMessage());
			// no need to keep connection.
			client.disconnect();
			return null;
		}
		return client;
	}

	@Override
	protected IPCMMClientHandler getPCMMClientHandler(Socket socket) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @see {@link IPSCMTSClient}
	 */
	/* public */static class PSCMTSClient extends AbstractPCMMClient implements
			IPSCMTSClient {
		/**
		 * Transaction id is
		 */
		private short transactionID;
		private short classifierID;
		private int gateID;

		public PSCMTSClient() {
			super();
			logger.info("Client " + getClass() + hashCode() + " crated and started");
		}

		public PSCMTSClient(Socket socket) {
			setSocket(socket);
		}

		public boolean gateSet() {
			logger.debug("Sending Gate-Set message");
			if (!isConnected())
				throw new IllegalArgumentException("Not connected");
			// XXX check if other values should be provided
			//
			ITrafficProfile trafficProfile = buildTrafficProfile();
			// PCMMGlobalConfig.DefaultBestEffortTrafficRate);
			ITransactionID trID = new TransactionID();
			// set transaction ID to gate set
			trID.setGateCommandType(ITransactionID.GateSet);
			transactionID = (short) (transactionID == 0 ? (short) (Math.random() * hashCode()) : transactionID);
			trID.setTransactionIdentifier(transactionID);
			// AMID
			IAMID amid = getAMID();
			// GATE SPEC
			IGateSpec gateSpec = getGateSpec();
			ISubscriberID subscriberID = new SubscriberID();
			// Classifier if MM version <4, Extended Classifier else
			IClassifier eclassifier = getClassifier(subscriberID);

			IPCMMGate gate = new PCMMGateReq();
			gate.setTransactionID(trID);
			gate.setAMID(amid);
			gate.setSubscriberID(subscriberID);
			gate.setGateSpec(gateSpec);
			gate.setTrafficProfile(trafficProfile);
			gate.setClassifier(eclassifier);
			byte[] data = gate.getData();

			// configure message properties
			Properties prop = new Properties();
			prop.put(MessageProperties.CLIENT_HANDLE, getClientHandle());
			prop.put(MessageProperties.DECISION_CMD_CODE, COPSDecision.DEC_INSTALL);
			prop.put(MessageProperties.DECISION_FLAG, (short) COPSDecision.DEC_NULL);
			prop.put(MessageProperties.GATE_CONTROL, new COPSData(data, 0, data.length));
			COPSMsg decisionMsg = MessageFactory.getInstance().create(COPSHeader.COPS_OP_DEC, prop);
			// ** Send the GateSet Decision
			// **
			sendRequest(decisionMsg);
			// TODO check on this ?
			// waits for the gate-set-ack or error
			COPSMsg responseMsg = readMessage();
			if (responseMsg.getHeader().isAReport()) {
				logger.info("processing received report from CMTS");
				COPSReportMsg reportMsg = (COPSReportMsg) responseMsg;
				if (reportMsg.getClientSI().size() == 0) {
					logger.debug("CMTS responded with an empty SI");
					return false;
				}
				COPSClientSI clientSI = (COPSClientSI) reportMsg.getClientSI().elementAt(0);
				IPCMMGate responseGate = new PCMMGateReq(clientSI.getData().getData());
				IPCMMError error = ((PCMMGateReq) responseGate).getError();
				if (error != null) {
					logger.error(error.toString());
					return false;
				}
				logger.info("the CMTS has sent TransactionID :"+responseGate.getTransactionID());
				if (responseGate.getTransactionID() != null && responseGate.getTransactionID().getGateCommandType() == ITransactionID.GateSetAck) {
					logger.info("the CMTS has sent a Gate-Set-Ack response");
					// here CMTS responded that he acknowledged the Gate-Set
					// TODO do further check of Gate-Set-Ack GateID etc...
					gateID = responseGate.getGateID().getGateID();
					return true;
				} else {
					return false;
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.pcmm.rcd.IPCMMPolicyServer#gateDelete()
		 */
		@Override
		public boolean gateDelete() {
			if (!isConnected()) {
				logger.error("Not connected");
				return false;
			}
			ITransactionID trID = new TransactionID();
			// set transaction ID to gate set
			trID.setGateCommandType(ITransactionID.GateDelete);
			trID.setTransactionIdentifier(transactionID);
			// AMID
			IAMID amid = getAMID();
			// GATE SPEC
			ISubscriberID subscriberID = new SubscriberID();
			try {
				subscriberID.setSourceIPAddress(InetAddress.getLocalHost());
			} catch (UnknownHostException e1) {
				logger.error(e1.getMessage());
			}

			IGateID gateIdObj = new GateID();
			gateIdObj.setGateID(gateID);

			IPCMMGate gate = new PCMMGateReq();
			gate.setTransactionID(trID);
			gate.setAMID(amid);
			gate.setSubscriberID(subscriberID);
			gate.setGateID(gateIdObj);

			// configure message properties
			Properties prop = new Properties();
			prop.put(MessageProperties.CLIENT_HANDLE, getClientHandle());
			prop.put(MessageProperties.DECISION_CMD_CODE, COPSDecision.DEC_INSTALL);
			prop.put(MessageProperties.DECISION_FLAG, (short) COPSDecision.DEC_NULL);
			byte[] data = gate.getData();
			prop.put(MessageProperties.GATE_CONTROL, new COPSData(data, 0, data.length));
			COPSMsg decisionMsg = MessageFactory.getInstance().create(COPSHeader.COPS_OP_DEC, prop);
			// ** Send the GateSet Decision
			// **
			try {
				decisionMsg.writeData(getSocket());
			} catch (IOException e) {
				logger.error("Failed to send the decision, reason: " + e.getMessage());
				return false;
			}
			// waits for the gate-delete-ack or error
			COPSMsg responseMsg = readMessage();
			if (responseMsg.getHeader().isAReport()) {
				logger.info("processing received report from CMTS");
				COPSReportMsg reportMsg = (COPSReportMsg) responseMsg;
				if (reportMsg.getClientSI().size() == 0) {
					return false;
				}
				COPSClientSI clientSI = (COPSClientSI) reportMsg.getClientSI().elementAt(0);
				IPCMMGate responseGate = new PCMMGateReq(clientSI.getData().getData());
				IPCMMError error = ((PCMMGateReq) responseGate).getError();
				if (error != null) {
					logger.error(error.toString());
					return false;
				}
				// here CMTS responded that he acknowledged the Gate-delete
				// message
				ITransactionID responseTransactionID = responseGate.getTransactionID();
				if (responseTransactionID != null && responseTransactionID.getGateCommandType() == ITransactionID.GateDeleteAck) {
					// TODO check : Is this test needed ??
					if (responseGate.getGateID().getGateID() == gateID && responseTransactionID.getTransactionIdentifier() == transactionID) {
						logger.info("the CMTS has sent a Gate-Delete-Ack response");
						return true;
					}
				}

			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.pcmm.rcd.IPCMMPolicyServer#gateInfo()
		 */
		@Override
		public boolean gateInfo() {
			if (!isConnected()) {
				logger.error("Not connected");
				return false;
			}
			ITransactionID trID = new TransactionID();
			// set transaction ID to gate set
			trID.setGateCommandType(ITransactionID.GateInfo);
			trID.setTransactionIdentifier(transactionID);
			// AMID
			IAMID amid = getAMID();
			// GATE SPEC
			ISubscriberID subscriberID = new SubscriberID();
			try {
				subscriberID.setSourceIPAddress(InetAddress.getLocalHost());
			} catch (UnknownHostException e1) {
				logger.error(e1.getMessage());
			}
			IGateID gateIdObj = new GateID();
			gateIdObj.setGateID(gateID);

			IPCMMGate gate = new PCMMGateReq();
			gate.setTransactionID(trID);
			gate.setAMID(amid);
			gate.setSubscriberID(subscriberID);
			gate.setGateID(gateIdObj);

			// configure message properties
			Properties prop = new Properties();
			prop.put(MessageProperties.CLIENT_HANDLE, getClientHandle());
			prop.put(MessageProperties.DECISION_CMD_CODE, COPSDecision.DEC_INSTALL);
			prop.put(MessageProperties.DECISION_FLAG, (short) COPSDecision.DEC_NULL);
			byte[] data = gate.getData();
			prop.put(MessageProperties.GATE_CONTROL, new COPSData(data, 0, data.length));
			COPSMsg decisionMsg = MessageFactory.getInstance().create(COPSHeader.COPS_OP_DEC, prop);
			// ** Send the GateSet Decision
			// **
			try {
				decisionMsg.writeData(getSocket());
			} catch (IOException e) {
				logger.error("Failed to send the decision, reason: " + e.getMessage());
				return false;
			}
			// waits for the gate-Info-ack or error
			COPSMsg responseMsg = readMessage();
			if (responseMsg.getHeader().isAReport()) {
				logger.info("processing received report from CMTS");
				COPSReportMsg reportMsg = (COPSReportMsg) responseMsg;
				if (reportMsg.getClientSI().size() == 0) {
					return false;
				}
				COPSClientSI clientSI = (COPSClientSI) reportMsg.getClientSI().elementAt(0);
				IPCMMGate responseGate = new PCMMGateReq(clientSI.getData().getData());
				IPCMMError error = ((PCMMGateReq) responseGate).getError();
				ITransactionID responseTransactionID = responseGate.getTransactionID();
				if (error != null) {
					logger.debug(responseTransactionID != null ? responseTransactionID.toString() : "returned Transaction ID is null");
					logger.error(error.toString());
					return false;
				}
				// here CMTS responded that he acknowledged the Gate-Info
				// message
				/*
				 * <Gate-Info-Ack> = <ClientSI Header> <TransactionID> <AMID>
				 * <SubscriberID> <GateID> [<Event Generation Info>] <Gate-Spec>
				 * <classifier> <classifier...>] <Traffic Profile> <Gate Time
				 * Info> <Gate Usage Info> [<Volume-Based Usage Limit>] [<PSID>]
				 * [<Msg-Receipt-Key>] [<UserID>] [<Time-Based Usage Limit>]
				 * [<Opaque Data>] <GateState> [<SharedResourceID>]
				 */
				if (responseTransactionID != null && responseTransactionID.getGateCommandType() == ITransactionID.GateInfoAck) {
					// TODO need to implement missing data wrapper
					logger.info("TransactionID : " + responseTransactionID.toString());
					logger.info("AMID :" + String.valueOf(responseGate.getAMID()));
					logger.info("SubscriberID :" + String.valueOf(responseGate.getSubscriberID()));
					logger.info("Traffic Profile :" + String.valueOf(responseGate.getTrafficProfile()));
					logger.info("Gate Time Info :");
					logger.info("Gate Usage Info :");
					logger.info("GateState :");
					return true;
				}

			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.pcmm.rcd.IPCMMPolicyServer#synchronize()
		 */
		@Override
		public boolean gateSynchronize() {
			if (!isConnected()) {
				logger.error("Not connected");
				return false;
			}
			ITransactionID trID = new TransactionID();
			// set transaction ID to gate set
			trID.setGateCommandType(ITransactionID.SynchRequest);
			trID.setTransactionIdentifier(transactionID);
			// AMID
			IAMID amid = getAMID();
			// GATE SPEC
			ISubscriberID subscriberID = new SubscriberID();
			try {
				subscriberID.setSourceIPAddress(InetAddress.getLocalHost());
			} catch (UnknownHostException e1) {
				logger.error(e1.getMessage());
			}
			IGateID gateIdObj = new GateID();
			gateIdObj.setGateID(gateID);

			IPCMMGate gate = new PCMMGateReq();
			gate.setTransactionID(trID);
			gate.setAMID(amid);
			gate.setSubscriberID(subscriberID);
			gate.setGateID(gateIdObj);

			// configure message properties
			Properties prop = new Properties();
			prop.put(MessageProperties.CLIENT_HANDLE, getClientHandle());
			prop.put(MessageProperties.DECISION_CMD_CODE, COPSDecision.DEC_INSTALL);
			prop.put(MessageProperties.DECISION_FLAG, (short) COPSDecision.DEC_NULL);
			byte[] data = gate.getData();
			prop.put(MessageProperties.GATE_CONTROL, new COPSData(data, 0, data.length));
			COPSMsg decisionMsg = MessageFactory.getInstance().create(COPSHeader.COPS_OP_DEC, prop);
			// ** Send the GateSet Decision
			// **
			try {
				decisionMsg.writeData(getSocket());
			} catch (IOException e) {
				logger.error("Failed to send the decision, reason: " + e.getMessage());
				return false;
			}
			// waits for the gate-Info-ack or error
			COPSMsg responseMsg = readMessage();
			if (responseMsg.getHeader().isAReport()) {
				logger.info("processing received report from CMTS");
				COPSReportMsg reportMsg = (COPSReportMsg) responseMsg;
				if (reportMsg.getClientSI().size() == 0) {
					return false;
				}
				COPSClientSI clientSI = (COPSClientSI) reportMsg.getClientSI().elementAt(0);
				IPCMMGate responseGate = new PCMMGateReq(clientSI.getData().getData());
				IPCMMError error = ((PCMMGateReq) responseGate).getError();
				ITransactionID responseTransactionID = responseGate.getTransactionID();
				if (error != null) {
					logger.debug(responseTransactionID != null ? responseTransactionID.toString() : "returned Transaction ID is null");
					logger.error(error.toString());
					return false;
				}
				// here CMTS responded that he acknowledged the Gate-Info
				// message
				/*
				 * <Gate-Info-Ack> = <ClientSI Header> <TransactionID> <AMID>
				 * <SubscriberID> <GateID> [<Event Generation Info>] <Gate-Spec>
				 * <classifier> <classifier...>] <Traffic Profile> <Gate Time
				 * Info> <Gate Usage Info> [<Volume-Based Usage Limit>] [<PSID>]
				 * [<Msg-Receipt-Key>] [<UserID>] [<Time-Based Usage Limit>]
				 * [<Opaque Data>] <GateState> [<SharedResourceID>]
				 */
				if (responseTransactionID != null && responseTransactionID.getGateCommandType() == ITransactionID.SynchReport) {
					// TODO need to implement missing data wrapper
					logger.info("TransactionID : " + responseTransactionID.toString());
					logger.info("AMID :" + String.valueOf(responseGate.getAMID()));
					logger.info("SubscriberID :" + String.valueOf(responseGate.getSubscriberID()));
					logger.info("Traffic Profile :" + String.valueOf(responseGate.getTrafficProfile()));
					logger.info("Gate Time Info :");
					logger.info("Gate Usage Info :");
					logger.info("GateState :");
					return true;
				}

			}
			return false;
		}

		private IAMID getAMID() {
			IAMID amid = new AMID();
			amid.setApplicationType((short) 1);
			amid.setApplicationMgrTag((short) 1);
			return amid;
		}

		private IClassifier getClassifier(ISubscriberID subscriberID) {
			IClassifier classifier = null;
			// if the version major is less than 4 we need to use Classifier
			if (getVersionInfo().getMajorVersionNB() >= 4) {
				classifier = new ExtendedClassifier();
				// eclassifier.setProtocol(IClassifier.Protocol.NONE);
				classifier.setProtocol(IClassifier.Protocol.TCP);
				try {
					InetAddress subIP = InetAddress.getByName(PCMMGlobalConfig.SubscriberID);
					InetAddress srcIP = InetAddress.getByName(PCMMGlobalConfig.srcIP);
					InetAddress dstIP = InetAddress.getByName(PCMMGlobalConfig.dstIP);
					InetAddress mask = InetAddress.getByName(PCMMProperties.get(PCMMConstants.DEFAULT_MASK, String.class));
					subscriberID.setSourceIPAddress(subIP);
					classifier.setSourceIPAddress(srcIP);
					classifier.setDestinationIPAddress(dstIP);
					((IExtendedClassifier) classifier).setIPDestinationMask(mask);
					((IExtendedClassifier) classifier).setIPSourceMask(mask);
				} catch (UnknownHostException unae) {
					System.out.println("Error getByName" + unae.getMessage());
				}
				((IExtendedClassifier) classifier).setSourcePortStart(PCMMGlobalConfig.srcPort);
				((IExtendedClassifier) classifier).setSourcePortEnd(PCMMGlobalConfig.srcPort);
				((IExtendedClassifier) classifier).setDestinationPortStart(PCMMGlobalConfig.dstPort);
				((IExtendedClassifier) classifier).setDestinationPortEnd(PCMMGlobalConfig.dstPort);
				((IExtendedClassifier) classifier).setActivationState((byte) 0x01);
				/*
				 * check if we have a stored value of classifierID else we just
				 * create one eclassifier.setClassifierID((short) 0x01);
				 */
				((IExtendedClassifier) classifier).setClassifierID((short) (classifierID == 0 ? Math.random() * hashCode() : classifierID));
				// XXX - testie
				// eclassifier.setClassifierID((short) 1);
				((IExtendedClassifier) classifier).setAction((byte) 0x00);
				// XXX - temp default until Gate Modify is hacked in
				// eclassifier.setPriority(PCMMGlobalConfig.EClassifierPriority);
				classifier.setPriority((byte) 65);

			} else {
				classifier = new Classifier();
				classifier.setProtocol(IClassifier.Protocol.TCP);
				try {
					InetAddress subIP = InetAddress.getByName(PCMMGlobalConfig.SubscriberID);
					InetAddress srcIP = InetAddress.getByName(PCMMGlobalConfig.srcIP);
					InetAddress dstIP = InetAddress.getByName(PCMMGlobalConfig.dstIP);
					subscriberID.setSourceIPAddress(subIP);
					classifier.setSourceIPAddress(srcIP);
					classifier.setDestinationIPAddress(dstIP);
				} catch (UnknownHostException unae) {
					System.out.println("Error getByName" + unae.getMessage());
				}
				classifier.setSourcePort(PCMMGlobalConfig.srcPort);
				classifier.setDestinationPort(PCMMGlobalConfig.dstPort);
			}
			return classifier;
		}

		/**
		 * 
		 * @return GateSpec object
		 */
		private IGateSpec getGateSpec() {
			IGateSpec gateSpec = new GateSpec();
			gateSpec.setDirection(Direction.UPSTREAM);
			gateSpec.setDSCP_TOSOverwrite(DSCPTOS.OVERRIDE);
			gateSpec.setTimerT1(PCMMGlobalConfig.GateT1);
			gateSpec.setTimerT2(PCMMGlobalConfig.GateT2);
			gateSpec.setTimerT3(PCMMGlobalConfig.GateT3);
			gateSpec.setTimerT4(PCMMGlobalConfig.GateT4);
			return gateSpec;
		}

		/**
		 * creates a traffic profile with 3 envelops (Authorized, Reserved and
		 * Committed).
		 * 
		 * @return Traffic profile
		 */
		private ITrafficProfile buildTrafficProfile() {
			ITrafficProfile trafficProfile = new BestEffortService(BestEffortService.DEFAULT_ENVELOP);
			((BestEffortService) trafficProfile).getAuthorizedEnvelop().setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
			((BestEffortService) trafficProfile).getAuthorizedEnvelop().setMaximumTrafficBurst(BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
			((BestEffortService) trafficProfile).getAuthorizedEnvelop().setRequestTransmissionPolicy(PCMMGlobalConfig.BETransmissionPolicy);
			((BestEffortService) trafficProfile).getAuthorizedEnvelop().setMaximumSustainedTrafficRate(PCMMGlobalConfig.DefaultLowBestEffortTrafficRate);
			// PCMMGlobalConfig.DefaultBestEffortTrafficRate);

			((BestEffortService) trafficProfile).getReservedEnvelop().setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
			((BestEffortService) trafficProfile).getReservedEnvelop().setMaximumTrafficBurst(BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
			((BestEffortService) trafficProfile).getReservedEnvelop().setRequestTransmissionPolicy(PCMMGlobalConfig.BETransmissionPolicy);
			((BestEffortService) trafficProfile).getReservedEnvelop().setMaximumSustainedTrafficRate(PCMMGlobalConfig.DefaultLowBestEffortTrafficRate);
			// PCMMGlobalConfig.DefaultBestEffortTrafficRate);

			((BestEffortService) trafficProfile).getCommittedEnvelop().setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
			((BestEffortService) trafficProfile).getCommittedEnvelop().setMaximumTrafficBurst(BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
			((BestEffortService) trafficProfile).getCommittedEnvelop().setRequestTransmissionPolicy(PCMMGlobalConfig.BETransmissionPolicy);
			((BestEffortService) trafficProfile).getCommittedEnvelop().setMaximumSustainedTrafficRate(PCMMGlobalConfig.DefaultLowBestEffortTrafficRate);
			return trafficProfile;
		}

		@Override
		public short getClassifierId() {
			return classifierID;
		}

		@Override
		public short getTransactionId() {
			return transactionID;
		}

		@Override
		public int getGateId() {
			return gateID;
		}
	}

}

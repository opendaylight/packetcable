/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.packetcable.provider;

import com.google.common.collect.Maps;
import java.net.InetAddress;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccaps.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;
import org.pcmm.PCMMPdpAgent;
import org.pcmm.PCMMPdpDataProcess;
import org.pcmm.PCMMPdpMsgSender;
import org.pcmm.gates.IGateState;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.gates.impl.TransactionID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.umu.cops.prpdp.COPSPdpException;
import org.umu.cops.stack.COPSError;
import org.umu.cops.stack.COPSError.ErrorTypes;
import java.nio.ByteBuffer;


/**
 * Class responsible for managing the gates for a single CCAP.
 */
@ThreadSafe
public class PCMMService {
    private final Logger logger = LoggerFactory.getLogger(PCMMService.class);

    private final Ccap ccap;
    private final IpAddress ipAddr;
    private final PortNumber portNum;
    protected final CcapClient ccapClient;
    protected Map<String, PCMMGateReq> gateRequests = Maps.newConcurrentMap();

    private final short clientType;

    public PCMMService(final short clientType, final Ccap ccap) {
        this.clientType = clientType;
        this.ccap = ccap;
        ipAddr = ccap.getConnection().getIpAddress();
        portNum = ccap.getConnection().getPort();

        ccapClient = new CcapClient(ipAddr, portNum);
        logger.info("Attempting to add CCAP with ID {} @ {}:{}", ccap.getCcapId(), ipAddr.getIpv4Address().getValue(),
                portNum.getValue());
    }

    public void disconect() {
        ccapClient.disconnect();
    }

    // TODO - try and change the return to something other than a String to be parsed to determine success
    public String addCcap() {
        ccapClient.connect();
        if (ccapClient.isConnected) {
            logger.info("Connected to CCAP with ID - " + ccap.getCcapId());
            return String
                    .format("200 OK - CCAP %s connected @ %s:%d", ccap.getCcapId(), ipAddr.getIpv4Address().getValue(),
                            portNum.getValue());
        } else {
            return String.format("404 Not Found - CCAP %s failed to connect @ %s:%d - %s", ccap.getCcapId(),
                    ipAddr.getIpv4Address().getValue(), portNum.getValue(), ccapClient.errMessage);
        }
    }

    public class GateSendStatus {
        private boolean didSucceed = false;
        private String message = "";
        private String copsGateId = "";
        private String copsGateState = "";
        private String copsGateStateReason = "";
        private String copsGateTimeInfo = "";
        private String copsGateUsageInfo = "";

        public boolean didSucceed() {
            return didSucceed;
        }

        void setDidSucceed(final boolean didSucceed) {
            this.didSucceed = didSucceed;
        }

        public String getMessage() {
            return message;
        }

        void setMessage(final String message) {
            this.message = message;
        }

        public String getCopsGateId() {
            return copsGateId;
        }

        void setCopsGateId(final String copsGateId) {
            this.copsGateId = copsGateId;
        }

        public String getCopsGateState() {
            return copsGateState;
        }
        void setCopsGateState(final String copsGateState) {
            this.copsGateState = copsGateState;
        }

        public String getCopsGateStateReason() {
            return copsGateStateReason;
        }
        void setCopsGateStateReason(final String copsGateStateReason) {
            this.copsGateStateReason = copsGateStateReason;
        }

        public String getCopsGateTimeInfo() {
            return copsGateTimeInfo;
        }
        void setCopsGateTimeInfo(final String copsGateTimeInfo) {
            this.copsGateTimeInfo = copsGateTimeInfo;
        }

        public String getCopsGateUsageInfo() {
            return copsGateUsageInfo;
        }
        void setCopsGateUsageInfo(final String copsGateUsageInfo) {
            this.copsGateUsageInfo = copsGateUsageInfo;
        }
    }

    private static final int BITS_PER_BYTE = 8;
    private static long getUnsignedInt(int x) {
        ByteBuffer buf = ByteBuffer.allocate(Long.SIZE / BITS_PER_BYTE);
        buf.putInt(Integer.SIZE / BITS_PER_BYTE, x);
        return buf.getLong(0);
    }

    public GateSendStatus sendGateSet(final String gatePathStr, final InetAddress subId, final Gate qosGate) {

        GateSendStatus status = new GateSendStatus();

        logger.info("Sending gate to CCAP with ID - " + ccap.getCcapId());

        // assemble the gate request for this subId
        final PCMMGateReqBuilder gateBuilder = new PCMMGateReqBuilder();
        gateBuilder.setAmId(ccap.getAmId());
        gateBuilder.setSubscriberId(subId);
        gateBuilder.setGateSpec(qosGate.getGateSpec());
        gateBuilder.setTrafficProfile(qosGate.getTrafficProfile());
        gateBuilder.setClassifiers(qosGate.getClassifiers().getClassifierContainer());

        if (qosGate.getCopsGateId() != null) {
            long lgate = Long.parseLong(qosGate.getCopsGateId());
            gateBuilder.setGateId((int)lgate);
        }
        
        logger.debug("PCMMService: sendGateSet(): formatting gate");
        
        // assemble the final gate request
        final PCMMGateReq gateReq = gateBuilder.build();

        if (gateRequests.get(gatePathStr) == null) {
            // and remember it
            gateRequests.put(gatePathStr, gateReq);
            // and send it to the CCAP
            ccapClient.sendGateSet(gateReq);
            // and wait for the COPS response to complete processing gate request
            try {
                // TODO - see PCMMPdpReqStateMan#processReport() gate.notify(). Should determine a better means to
                // TODO - handle this synchronization.
                // TODO - if not changing this, may want to make this timeout configurable
                synchronized (gateReq) {
                    logger.info("Waiting 5000ms for gate request to be updated");
                    gateReq.wait(5000);
                    logger.debug("Gate request error - " + gateReq.getError());
                    logger.debug("Gate request ID - " + gateReq.getGateID());
                }
            } catch (Exception e) {
                logger.error(
                        "PCMMService: sendGateSet(): gate response timeout exceeded for " + gatePathStr + '/' + gateReq,
                        e);
                status.setDidSucceed(false);
                status.setMessage(String.format("408 Request Timeout - gate response timeout exceeded for %s/%s", ccap.getCcapId(),
                        gatePathStr));
                return status;
            }


            if (gateReq.getError() != null) {
                gateRequests.remove(gatePathStr);
                status.setDidSucceed(false);
                status.setMessage(
                        String.format("404 Not Found - sendGateSet for %s/%s returned error - %s", ccap.getCcapId(),
                                gatePathStr, gateReq.getError().toString()));

                logger.error("PCMMService: sendGateSet(): returned error: {}", gateReq.getError().toString());
            } else {
                if (gateReq.getGateID() != null) {
                    status.setDidSucceed(true);
                ByteBuffer buf = ByteBuffer.allocate(Long.SIZE / 8);
                buf.putInt(Integer.SIZE / 8, gateReq.getGateID().getGateID());
                status.setCopsGateId(String.format("%d", getUnsignedInt(gateReq.getGateID().getGateID())));
                status.setMessage(String.format("200 OK - sendGateSet for %s/%s returned GateId %d",
                                                ccap.getCcapId(), gatePathStr, getUnsignedInt(gateReq.getGateID().getGateID())) );
                logger.info(String.format("PCMMService: sendGateSet(): returned GateId %d: ",
                                          getUnsignedInt(gateReq.getGateID().getGateID())));
                } else {
                    status.setDidSucceed(false);
                    status.setMessage(
                            String.format("404 Not Found - sendGateSet for %s/%s no gateId returned", ccap.getCcapId(),
                                    gatePathStr));

                    logger.info("PCMMService: sendGateSet(): no gateId returned:");
                }
            }
        } else {
            logger.info("PCMMService: sendGateSet(): no gateId returned:");
            status.setMessage(String.format("404 Not Found - sendGateSet for %s/%s already exists", ccap.getCcapId(), gatePathStr));
        }

        return status;
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
                synchronized (gateReq) {
                    gateReq.wait(1000);
                }
            } catch (InterruptedException e) {
                logger.error("PCMMService: sendGateDelete(): gate response timeout exceeded for {}/{}", gatePathStr,
                        gateReq);
            }
            if (gateReq.getError() != null) {
                logger.warn("PCMMService: sendGateDelete(): returned error: {}", gateReq.getError().toString());
                return false;
            } else {
                if (gateReq.getGateID() != null) {
                    logger.info(String.format("PCMMService: sendGateDelete(): deleted GateId %d: ",
                                              getUnsignedInt(gateReq.getGateID().getGateID())));
                } else {
                    logger.error("PCMMService: sendGateDelete(): deleted but no gateId returned");
                }
                return true;
            }
        } else {
            logger.warn("Attempt to delete non-existent gate with path - " + gatePathStr);
            return false;
        	}
        }

    	public Boolean getPcmmPdpSocket() {
    		try {
    			return ccapClient.pcmmPdp.getSocket().isClosed();
    		} catch (Exception e) {
    			logger.error("getPcmmPdpSocket: {} FAILED: {}", ccapClient, e.getMessage());
    			return true;
    		}
    	}

    	public Boolean getPcmmCcapClientIsConnected() {
    		try {
    			return ccapClient.isConnected;
    		} catch (Exception e) {
    			logger.error("getPcmmCcapClientIsConnected: {} FAILED: {}", ccapClient, e.getMessage());
    			return false;
    		}
    	}

    	public String getPcmmCcapClientConnectErrMsg() {
    		try {
    			return ccapClient.errMessage;
    		} catch (Exception e) {
    			logger.error("getPcmmCcapClientIsConnected: {} FAILED: {}", ccapClient, e.getMessage());
    			return e.getMessage();
    		}
    	}

        //new gate-info method
    	public GateSendStatus sendGateInfo(final String gatePathStr) {

    		logger.info("sendGateInfo() - " + ccap);

    		GateSendStatus status = new GateSendStatus();

            // recover the original gate request
            final PCMMGateReq gateReq = gateRequests.get(gatePathStr);

            // is the ccap socket open?
            final Boolean socketIsClosed = getPcmmPdpSocket();

            if ((gateReq != null) && (!socketIsClosed)) {
                gateReq.setTransactionID(new TransactionID(gateReq.getTransactionID().getTransactionIdentifier(),
                        ITransactionID.GateCommandType.GATE_INFO));

                ccapClient.sendGateInfo(gateReq);
                // and wait for the response to complete
                try {
                    // TODO - see PCMMPdpReqStateMan#processReport() gate.notify(). Should determine a better means to
                    // TODO - handle this synchronization.
                    synchronized (gateReq) {
                        logger.info("Waiting 5000ms for gate request to be updated");
                        gateReq.wait(5000);
                        logger.debug("Gate request error - " + gateReq.getError());
                        logger.debug("Gate request ID - " + gateReq.getGateID());
                    }
                } catch (InterruptedException e) {
                    status.setDidSucceed(false);
                    status.setMessage(String.format("Gate-Info Request Timeout for %s", ccap.getCcapId()));
                    return status;
                }
                if (gateReq.getError() != null) {
                    status.setDidSucceed(false);
                    status.setMessage(
                            String.format("%s reports '%s'", ccap.getCcapId(), gateReq.getError().toString()));
                    logger.error("PCMMService: sendGateInfo(): returned error: {}", gateReq.getError().toString());
                } else {
                    if (gateReq.getGateID() != null) {
                        status.setDidSucceed(true);
                        status.setCopsGateId(String.format("%d", getUnsignedInt(gateReq.getGateID().getGateID())));

                        final IGateState gateState = gateReq.getGateState();
                        status.setCopsGateState(gateState.getGateState().toString());
                        status.setCopsGateStateReason(gateState.getGateStateReason().toString());
                        status.setCopsGateTimeInfo(String.format("%d", gateReq.getGateTimeInfo().getGateTimeInfo()));
                        status.setCopsGateUsageInfo(String.format("%d", gateReq.getGateUsageInfo().getGateUsageInfo()));
                        logger.info(String.format("PCMMService: sendGateInfo(): returned GateId %d: ",
                                getUnsignedInt(gateReq.getGateID().getGateID())));
                    } else {
                        status.setDidSucceed(false);
                        status.setMessage(
                                String.format("404 Not Found - sendGateInfo for %s/%s no gateId returned", ccap.getCcapId(),
                                        gatePathStr));

                        logger.info("PCMMService: sendGateInfo(): no gateId returned:");
                    }
                    return status;
                }
            } else {
            	status.setDidSucceed(false);
                if (socketIsClosed) {
                	status.setMessage(String.format("%s: CCAP Cops Socket is closed",ccap.getCcapId()));
                }
                else {
                	status.setMessage( String.format("Attempt to get info of non-existent gate with path - " + gatePathStr));
                }
            	return status;
            }
			return status;

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
         *
         * @param ccapIp
         *         - the IP of the CCAP to manage
         * @param portNum
         *         - the port number of the CCAP to manage
         */
        public CcapClient(final IpAddress ccapIp, final PortNumber portNum) {
            ipv4 = ccapIp.getIpv4Address().getValue();
            if (portNum != null) {
                port = portNum.getValue();
            } else {
                port = PCMMPdpAgent.WELL_KNOWN_PDP_PORT;
            }
            // TODO FIXME - if this object is not null, gate processing will not work correctly
            // TODO see - PCMMPdpReqStateMan#processReport() where the report type is success and the process is null
            //            pcmmProcess = new PCMMPdpDataProcess();
            pcmmProcess = null;
            pcmmPdp = new PCMMPdpAgent(ipv4, port, clientType, pcmmProcess);
        }

        /**
         * Starts the connection to the CCAP
         */
        public void connect() {
            logger.info("Attempting to connect to host: " + ipv4 + " port: " + port);
            errMessage = null;
            try {
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
            // and save it back to the gateRequest object for gate delete later
            gateReq.setGateID(pcmmSender.getGateID());
            return true;
            } catch (COPSPdpException e) {
                logger.error("CcapClient: sendGateSet(): {}:{} => {} FAILED: {}", ipv4, port, gateReq,
                             e.getMessage());
                return false;
        }
            catch (Exception e) {
                logger.error("CcapClient: sendGateSet(): {}:{} => {} FAILED: {}", ipv4, port, gateReq,
                             e.getMessage());
                return false;
            }
        }

        public Boolean sendGateDelete(final PCMMGateReq gateReq) {
            logger.info("CcapClient: sendGateDelete(): {}:{} => {}", ipv4, port, gateReq);
            try {
                pcmmSender.sendGateDelete(gateReq);
                return true;
            } catch (COPSPdpException e) {
                logger.error("CcapClient: sendGateDelete(): {}:{} => {} FAILED: {}", ipv4, port,
                             gateReq, e.getMessage());
                return false;
            }
            catch (Exception e) {
                logger.error("CcapClient: sendGateDelete(): {}:{} => {} FAILED: {}", ipv4, port,
                             gateReq, e.getMessage());
                return false;
        }
       }

        public Boolean sendGateInfo(final PCMMGateReq gateReq) {
            logger.info("CcapClient: sendGateInfo(): {}:{} => {}", ipv4, port);
            try {
                pcmmSender.sendGateInfo(gateReq);
                // and save it back to the gateRequest object for operational sal persistance
                gateReq.setGateID(pcmmSender.getGateID());
                return true;
            } catch (COPSPdpException e) {
                logger.error("CcapClient: sendGateInfo(): {}:{} => {} FAILED: {}", ipv4, port,
                             gateReq, e.getMessage());
                return false;
            }
            catch (Exception e) {
                logger.error("CcapClient: sendGateInfo(): {}:{} => {} FAILED: {}", ipv4, port,
                             gateReq, e.getMessage());
                return false;
        }
    }
}
}


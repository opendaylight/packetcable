/**
 @header@
 */
package org.pcmm.rcd.impl;

import org.pcmm.gates.IPCMMGate;
import org.pcmm.gates.ITransactionID;
import org.pcmm.gates.impl.PCMMGateReq;
import org.pcmm.messages.impl.MessageFactory;
import org.pcmm.rcd.ICMTS;
import org.umu.cops.COPSStateMan;
import org.umu.cops.prpep.COPSPepConnection;
import org.umu.cops.prpep.COPSPepDataProcess;
import org.umu.cops.prpep.COPSPepException;
import org.umu.cops.prpep.COPSPepReqStateMan;
import org.umu.cops.stack.*;
import org.umu.cops.stack.COPSHeader.OPCode;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * This class starts a mock CMTS that can be used for testing.
 */
public class CMTS extends AbstractPCMMServer implements ICMTS {

	/**
	 * Constructor for having the server port automatically assigned
	 * Call getPort() after startServer() is called to determine the port number of the server
	 */
	public CMTS() {
		this(0);
	}

	/**
	 * Constructor for starting the server to a pre-defined port number
	 * @param port - the port number on which to start the server.
	 */
	public CMTS(final int port) {
		super(port);
	}

	@Override
	protected IPCMMClientHandler getPCMMClientHandler(final Socket socket) {

		return new AbstractPCMMClientHandler(socket) {

			private COPSHandle handle;

			public void run() {
				try {
					// send OPN message
					// set the major version info and minor version info to
					// default (5,0)
					logger.info("Send OPN message to the PS");
					sendRequest(MessageFactory.getInstance().create(OPCode.OPN, new Properties()));
					// wait for CAT
					COPSMsg recvMsg = readMessage();

					if (recvMsg.getHeader().getOpCode().equals(OPCode.CC)) {
						COPSClientCloseMsg cMsg = (COPSClientCloseMsg) recvMsg;
						logger.info("PS requested Client-Close" + cMsg.getError().getDescription());
						// send a CC message and close the socket
						disconnect();
						return;
					}
					if (recvMsg.getHeader().getOpCode().equals(OPCode.CAT)) {
						logger.info("received Client-Accept from PS");
						COPSClientAcceptMsg cMsg = (COPSClientAcceptMsg) recvMsg;
						// Support
						if (cMsg.getIntegrity() != null) {
							throw new COPSPepException("Unsupported object (Integrity)");
						}

						// Mandatory KATimer
						COPSKATimer kt = cMsg.getKATimer();
						if (kt == null)
							throw new COPSPepException("Mandatory COPS object missing (KA Timer)");
						short kaTimeVal = kt.getTimerVal();

						// ACTimer
						COPSAcctTimer at = cMsg.getAcctTimer();
						short acctTimer = 0;
						if (at != null)
							acctTimer = at.getTimerVal();

						logger.info("Send a REQ message to the PS");
						{
							Properties prop = new Properties();
							COPSMsg reqMsg = MessageFactory.getInstance().create(OPCode.REQ, prop);
							handle = ((COPSReqMsg) reqMsg).getClientHandle();
							sendRequest(reqMsg);
						}
						// Create the connection manager
						final PCMMCmtsConnection conn = new PCMMCmtsConnection(CLIENT_TYPE, socket);
						// pcmm specific handler
						// conn.addReqStateMgr(handle, new
						// PCMMPSReqStateMan(CLIENT_TYPE, handle));
						conn.addRequestState(handle, new CmtsDataProcessor());
						conn.setKaTimer(kaTimeVal);
						conn.setAcctTimer(acctTimer);
						logger.info(getClass().getName() + " Thread(conn).start");
						new Thread(conn).start();
					} else {
						// messages of other types are not expected
						throw new COPSPepException("Message not expected. Closing connection for " + socket.toString());
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}

			@Override
			public void task(Callable<?> c) {
				// TODO Auto-generated method stub

			}

			@Override
			public void shouldWait(int t) {
				// TODO Auto-generated method stub

			}

			@Override
			public void done() {
				// TODO Auto-generated method stub

			}

		};
	}

	class PCMMCmtsConnection extends COPSPepConnection {

		public PCMMCmtsConnection(final short clientType, final Socket sock) {
			super(clientType, sock);
		}

		public COPSPepReqStateMan addRequestState(final COPSHandle clientHandle, final COPSPepDataProcess process)
				throws COPSException {
			return super.addRequestState(clientHandle, process);
		}
	}

	class PCMMPSReqStateMan extends COPSPepReqStateMan {

		public PCMMPSReqStateMan(final short clientType, final COPSHandle clientHandle) {
			super(clientType, clientHandle, new CmtsDataProcessor());
		}

		@Override
		protected void processDecision(final COPSDecisionMsg dMsg, final Socket socket) throws COPSPepException {
            final Map<String, String> removeDecs = new HashMap<>();
			final Map<String, String> installDecs = new HashMap<>();
			final Map<String, String> errorDecs = new HashMap<>();

			for (final Set<COPSDecision> copsDecisions : dMsg.getDecisions().values()) {
				final COPSDecision cmddecision = copsDecisions.iterator().next();

				// cmddecision --> we must check whether it is an error!
                String prid = "";
                switch (cmddecision.getCommand()) {
                    case INSTALL:
                        for (final COPSDecision decision : copsDecisions) {
							final COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                            switch (obj.getSNum()) {
                                // TODO when there is install request only the PR_PRID
                                // is git but the ClientSI object containing the PR_EPD
                                // is null??? this is why the tests fail and so I set
                                // the assertion to NOT true....
                                case COPSPrObjBase.PR_PRID:
                                    prid = obj.getData().str();
                                    break;
                                case COPSPrObjBase.PR_EPD:
                                    installDecs.put(prid, obj.getData().str());
                                    break;
                                default:
                                    break;
                            }
                        }
                    case REMOVE:
                        for (final COPSDecision decision : copsDecisions) {
							final COPSPrObjBase obj = new COPSPrObjBase(decision.getData().getData());
                            switch (obj.getSNum()) {
                                // TODO when there is install request only the PR_PRID
                                // is git but the ClientSI object containing the PR_EPD
                                // is null??? this is why the tests fail and so I set
                                // the assertion to NOT true....
                                case COPSPrObjBase.PR_PRID:
                                    prid = obj.getData().str();
                                    break;
                                case COPSPrObjBase.PR_EPD:
                                    removeDecs.put(prid, obj.getData().str());
                                    break;
                                default:
                                    break;
                            }
                        }
                }
            }

			if (_process != null) {
				// ** Apply decisions to the configuration
				_process.setDecisions(this, removeDecs, installDecs, errorDecs);
				_status = Status.ST_DECS;
				if (_process.isFailReport(this)) {
					// COPSDebug.out(getClass().getName(),"Sending FAIL Report\n");
					_sender.sendFailReport(_process.getReportData(this));
				} else {
					// COPSDebug.out(getClass().getName(),"Sending SUCCESS Report\n");
					_sender.sendSuccessReport(_process.getReportData(this));
				}
				_status = Status.ST_REPORT;
			}
		}
	}

	class CmtsDataProcessor implements COPSPepDataProcess {

		private Map<String, String> removeDecs;
		private Map<String, String> installDecs;
		private Map<String, String> errorDecs;
		private COPSPepReqStateMan stateManager;

		public CmtsDataProcessor() {
			setRemoveDecs(new HashMap<String, String>());
			setInstallDecs(new HashMap<String, String>());
			setErrorDecs(new HashMap<String, String>());
		}

		@Override
		public void setDecisions(final COPSPepReqStateMan man, final Map<String, String> removeDecs,
                                 final Map<String, String> installDecs, final Map<String, String> errorDecs) {
			setRemoveDecs(removeDecs);
			setInstallDecs(installDecs);
			setErrorDecs(errorDecs);
			setStateManager(man);
		}

		@Override
		public boolean isFailReport(final COPSPepReqStateMan man) {
			return (errorDecs != null && errorDecs.size() > 0);
		}

		@Override
		public Map<String, String> getReportData(final COPSPepReqStateMan man) {
			if (isFailReport(man)) {
				return errorDecs;
			} else {
				final Map<String, String> siDataHashTable = new HashMap<>();
				if (installDecs.size() > 0) {
					String data = "";
					for (String k : installDecs.keySet()) {
						data = installDecs.get(k);
						break;
					}
					final ITransactionID transactionID = new PCMMGateReq(new COPSData(data).getData()).getTransactionID();
					final IPCMMGate responseGate = new PCMMGateReq();
					responseGate.setTransactionID(transactionID);

                    // TODO FIXME - Why is the key always null??? What value should be used here???
                    final String key = null;
					siDataHashTable.put(key, new String(responseGate.getData()));
				}
				return siDataHashTable;
			}
		}

		@Override
		public Map<String, String> getClientData(COPSPepReqStateMan man) {
			// TODO Auto-generated method stub
			return new HashMap<>();
		}

		@Override
		public Map<String, String> getAcctData(COPSPepReqStateMan man) {
			// TODO Auto-generated method stub
			return new HashMap<>();
		}

		@Override
		public void notifyClosedConnection(final COPSStateMan man, final COPSError error) {
			// TODO Auto-generated method stub
		}

		@Override
		public void notifyNoKAliveReceived(final COPSStateMan man) {
			// TODO Auto-generated method stub
		}

		@Override
		public void closeRequestState(final COPSStateMan man) {
			// TODO Auto-generated method stub
		}

		@Override
		public void newRequestState(final COPSPepReqStateMan man) {
			// TODO Auto-generated method stub
		}

		public Map<String, String> getRemoveDecs() {
			return new HashMap<>(removeDecs);
		}

		public void setRemoveDecs(final Map<String, String> removeDecs) {
			this.removeDecs = new HashMap<>(removeDecs);
		}

		public Map<String, String> getInstallDecs() {
			return new HashMap<>(installDecs);
		}

		public void setInstallDecs(final Map<String, String> installDecs) {
			this.installDecs = new HashMap<>(installDecs);
		}

		public Map<String, String> getErrorDecs() {
			return errorDecs;
		}

		public void setErrorDecs(final Map<String, String> errorDecs) {
			this.errorDecs = new HashMap<>(errorDecs);
		}

		public COPSPepReqStateMan getStateManager() {
			return stateManager;
		}

		public void setStateManager(COPSPepReqStateMan stateManager) {
			this.stateManager = stateManager;
		}

	}
}

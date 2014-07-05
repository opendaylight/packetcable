/*
 @header@
 */

package org.opendaylight.controller.protocol_plugin.packetcable.internal;

import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IPluginInFlowProgrammerService;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.umu.cops.prpdp.COPSPdpException;
import org.pcmm.gates.IPCMMGate;
import org.pcmm.PCMMDef;
import org.pcmm.PCMMGlobalConfig;
import org.pcmm.PCMMPdpMsgSender;
import org.pcmm.PCMMPdpDataProcess;
import org.pcmm.PCMMPdpAgent;



/**
 * Represents the packetcable plugin component in charge of programming the flows
 * the flow programming and relay them to functional modules above SAL.
 */
public class FlowProgrammerService implements IPluginInFlowProgrammerService
{
    protected PCMMPdpDataProcess process;
    protected PCMMPdpAgent pcmm_pdp;
    protected PCMMPdpMsgSender pcmm_sender;


    protected static final Logger logger = LoggerFactory
                                           .getLogger(FlowProgrammerService.class);
    void init() {
        logger.info("FlowProgrammerService: init");
        pcmm_pdp = new PCMMPdpAgent(PCMMDef.C_PCMM, process);
    }

    /**
     * Function called by the dependency manager when at least one dependency
     * become unsatisfied or when the component is shutting down because for
     * example bundle is being stopped.
     *
     */
    void destroy() {
        logger.info("FlowProgrammerService: destroy");
    }

    /**
     * Function called by dependency manager after "init ()" is called and after
     * the services provided by the class are registered in the service registry
     *
     */
    void start() {
        logger.info("FlowProgrammerService: start");
        try  {
            logger.info("Open connection to CMTS");
            pcmm_pdp.connect( PCMMGlobalConfig.DefaultCMTS, 3918 );
            pcmm_sender = new PCMMPdpMsgSender (PCMMDef.C_PCMM, pcmm_pdp.getClientHandle(), pcmm_pdp.getSocket());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Function called by the dependency manager before the services exported by
     * the component are unregistered, this will be followed by a "destroy ()"
     * calls
     *
     */
    void stop() {
        logger.info("FlowProgrammerService: stop");
    }


    /**
     * Synchronously add a flow to the network node
     *
     * @param node
     * @param flow
     */
    public Status addFlow(Node node, Flow flow){
        logger.info("FlowProgrammerService: addFlow");
        FlowConverter fc = new FlowConverter(flow);
        fc.dump();
        IPCMMGate gate = fc.getServiceFlow();
        fc.dumpAction();
        try {
            logger.info("sendGateSet to CMTS");
            pcmm_sender.sendGateSet(gate);
        } catch (COPSPdpException e) {
            logger.error("Failed to sendGateSet, reason: " + e.getMessage());
            return new Status(StatusCode.INTERNALERROR, "Failed to sendGateSet to CMTS");
        }
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Synchronously modify existing flow on the switch
     *
     * @param node
     * @param flow
     */
    public Status modifyFlow(Node node, Flow oldFlow, Flow newFlow){
        logger.info("FlowProgrammerService: modifyFlow");
        return new Status(StatusCode.SUCCESS);
    }
    /**
     * Synchronously remove the flow from the network node
     *
     * @param node
     * @param flow
     */
    public Status removeFlow(Node node, Flow flow){
        logger.info("FlowProgrammerService: removeFlow");
        logger.info("FlowProgrammerService: removeFlow GateID1 " + PCMMGlobalConfig.getGateID1() );
        logger.info("FlowProgrammerService: removeFlow GateID2 " + PCMMGlobalConfig.getGateID2() );
        logger.info("FlowProgrammerService: removeFlow FlowID " + flow.getId() );
        if ( PCMMGlobalConfig.getGateID1()  != 0 ) {
            logger.info("Remove Flow " + PCMMGlobalConfig.getGateID1() );
            try {
                pcmm_sender.sendGateDelete( PCMMGlobalConfig.getGateID1() );
            } catch (COPSPdpException e) {
                logger.error("Failed to sendGateDelete, reason: " + e.getMessage());
                return new Status(StatusCode.INTERNALERROR, "Failed to sendGateDelete to CMTS");
            }
        }
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Asynchronously add a flow to the network node
     *
     * @param node
     * @param flow
     * @param rid
     */
    public Status addFlowAsync(Node node, Flow flow, long rid){
        logger.info("FlowProgrammerService: addFlowAsync");
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Asynchronously modify existing flow on the switch
     *
     * @param node
     * @param flow
     * @param rid
     */
    public Status modifyFlowAsync(Node node, Flow oldFlow, Flow newFlow, long rid){
        logger.info("FlowProgrammerService: modifyFlowAsync");
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Asynchronously remove the flow from the network node
     *
     * @param node
     * @param flow
     * @param rid
     */
    public Status removeFlowAsync(Node node, Flow flow, long rid){
        logger.info("FlowProgrammerService: removeFlowAsync");
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Remove all flows present on the network node
     *
     * @param node
     */
    public Status removeAllFlows(Node node){
        logger.info("FlowProgrammerService: removeAllFlows");
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Send Barrier message synchronously. The caller will be blocked until the
     * Barrier reply arrives.
     *
     * @param node
     */
    public Status syncSendBarrierMessage(Node node){
        logger.info("FlowProgrammerService: syncSendBarrierMessage");
        return new Status(StatusCode.SUCCESS);
    }

    /**
     * Send Barrier message asynchronously. The caller is not blocked.
     *
     * @param node
     */
    public Status asyncSendBarrierMessage(Node node){
        logger.info("FlowProgrammerService: asyncSendBarrierMessage");
        return new Status(StatusCode.SUCCESS);
    }
  }

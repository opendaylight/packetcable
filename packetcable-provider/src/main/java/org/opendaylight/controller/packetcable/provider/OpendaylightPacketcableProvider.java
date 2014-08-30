package org.opendaylight.controller.packetcable.provider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.FlowspecCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.SubscriberIdRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.TcpMatchRangesRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcUpdateFlowUpdated;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.flowspec._case.Flowspec;


@SuppressWarnings("unused")
public class OpendaylightPacketcableProvider implements
		DataChangeListener, SalFlowService, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(OpendaylightPacketcableProvider.class);
	private NotificationProviderService notificationProvider;
	private DataBroker dataProvider;

	private final ExecutorService executor;

	// The following holds the Future for the current make toast task.
	// This is used to cancel the current toast.
	private final AtomicReference<Future<?>> currentConnectionsTasks = new AtomicReference<>();


	public OpendaylightPacketcableProvider() {
		executor = Executors.newCachedThreadPool();

	}

	public void setNotificationProvider(final NotificationProviderService salService) {
		this.notificationProvider = salService;
	}

	public void setDataProvider(final DataBroker salDataProvider) {
		this.dataProvider = salDataProvider;
	}

	/**
	 * Implemented from the AutoCloseable interface.
	 */
	@Override
	public void close() throws ExecutionException, InterruptedException {
		executor.shutdown();
//		if (dataProvider != null) {
//			for (Iterator<InstanceIdentifier<CmtsInstance>> iter = cmtsInstances.iterator(); iter.hasNext();) {
//				WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
//				tx.delete(LogicalDatastoreType.OPERATIONAL, iter.next());
//				Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
//					@Override
//					public void onSuccess(final Void result) {
//						logger.debug("Delete commit result: " + result);
//					}
//
//					@Override
//					public void onFailure(final Throwable t) {
//						logger.error("Delete operation failed", t);
//					}
//				});
//			}
//		}
	}

	// private CmtsInstance buildCmtsConnection(final String host) {
	// InetAddress address = InetAddress.getByName(host);
	// IpAddress ipAddress =
	// IpAddressBuilder.getDefaultInstance(address.getHostAddress());
	// PcmmConfigurationBuilder pcmmConfigurationBuilder = new
	// PcmmConfigurationBuilder().setIpAddress(ipAddress);
	// org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140120.cmts.instance.ConfigurationPointsBuilder
	// configurationPointsBuilder = new
	// org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140120.cmts.instance.ConfigurationPointsBuilder().build();
	// return new CmtsAddedBuilder().setConfigurationPoints().build();
	// }

	/**
	 * Implemented from the DataChangeListener interface.
	 */
	@Override
	public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
		DataObject dataObject = change.getUpdatedSubtree();
		logger.debug("OpendaylightPacketcableProvider.onDataChanged() :" + dataObject);
	}

    @Override
    public Future<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
        // Examples of how to unpack the matches
        UdpMatchRangesRpcAddFlow updRange = input.getMatch().getAugmentation(UdpMatchRangesRpcAddFlow.class);
        updRange.getUpdMatchRanges().getUdpDestinationPortStart();
        TcpMatchRangesRpcAddFlow tcpRange = input.getMatch().getAugmentation(TcpMatchRangesRpcAddFlow.class);
        tcpRange.getTcpMatchRanges().getTcpDestinationPortBegin();
        SubscriberIdRpcAddFlow subId = input.getMatch().getAugmentation(SubscriberIdRpcAddFlow.class);
        subId.getSubscriberId().getIpv6Address();
        // Examples of how to unpack the actions
        for( Instruction i : input.getInstructions().getInstruction()) {
            if(i.getInstruction() instanceof ApplyActionsCase) {
                ApplyActionsCase aac = (ApplyActionsCase)i.getInstruction();
                for (Action a : aac.getApplyActions().getAction() ) {
                    if(a.getAction() instanceof FlowspecCase) {
                        Flowspec flowSpec = ((FlowspecCase)a.getAction()).getFlowspec();
                        flowSpec.getFAuthorizedEnvelope();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Future<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
        UdpMatchRangesRpcRemoveFlow updRange = input.getMatch().getAugmentation(UdpMatchRangesRpcRemoveFlow.class);
        return null;
    }

    @Override
    public Future<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
        OriginalFlow foo = input.getOriginalFlow();
        UdpMatchRangesRpcUpdateFlowOriginal bar = foo.getMatch().getAugmentation(UdpMatchRangesRpcUpdateFlowOriginal.class);
        UpdatedFlow updated = input.getUpdatedFlow();
        UdpMatchRangesRpcUpdateFlowUpdated updatedRange = updated.getMatch().getAugmentation(UdpMatchRangesRpcUpdateFlowUpdated.class);

        return null;
    }
}

package org.opendaylight.controller.packetcable.provider;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.packetcable.provider.processors.PCMMDataProcessor;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.TrafficProfileBestEffortAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.TrafficProfileDocsisServiceClassNameAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.TrafficProfileFlowspecAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.BestEffortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.DocsisServiceClassNameCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.FlowspecCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeContextRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140909.CmtsCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140909.nodes.node.CmtsNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140909.UdpMatchRangesRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140909.UdpMatchRangesRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140909.UdpMatchRangesRpcUpdateFlowUpdated;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration.CompositeObjectRegistrationBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.ITrafficProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

@SuppressWarnings("unused")
public class OpendaylightPacketcableProvider implements DataChangeListener,
		SalFlowService, OpenDaylightPacketCableProviderService,
		BindingAwareProvider, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(OpendaylightPacketcableProvider.class);
	private NotificationProviderService notificationProvider;
	private DataBroker dataProvider;

	private final ExecutorService executor;

	// The following holds the Future for the current make toast task.
	// This is used to cancel the current toast.
	private final AtomicReference<Future<?>> currentConnectionsTasks = new AtomicReference<>();
	private ProviderContext providerContext;
	private NotificationProviderService notificationService;
	private DataBroker dataBroker;
	private ListenerRegistration<DataChangeListener> listenerRegistration;
	private List<InstanceIdentifier<?>> cmtsInstances;
	private PCMMDataProcessor pcmmDataProcessor;

	public OpendaylightPacketcableProvider() {
		executor = Executors.newCachedThreadPool();
		cmtsInstances = Lists.newArrayList();
		pcmmDataProcessor = new PCMMDataProcessor();
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
		if (dataProvider != null) {
			for (Iterator<InstanceIdentifier<?>> iter = cmtsInstances.iterator(); iter.hasNext();) {
				WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
				tx.delete(LogicalDatastoreType.OPERATIONAL, iter.next());
				Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
					@Override
					public void onSuccess(final Void result) {
						logger.debug("Delete commit result: " + result);
					}

					@Override
					public void onFailure(final Throwable t) {
						logger.error("Delete operation failed", t);
					}
				});
			}
		}
	}

	/**
	 * Implemented from the DataChangeListener interface.
	 */
	@Override
	public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
		DataObject dataObject = change.getUpdatedSubtree();
		logger.debug("OpendaylightPacketcableProvider.onDataChanged() :" + dataObject);
	}

	public void notifyConsumerOnCmtsAdd(CmtsNode input, TransactionId transactionId) {
		CmtsAdded cmtsRemoved = new CmtsAddedBuilder().setAddress(input.getAddress()).setPort(input.getPort()).setTransactionId(transactionId).build();
		notificationProvider.publish(cmtsRemoved);
	}

	public void notifyConsumerOnCmtsRemove(CmtsNode input, TransactionId transactionId) {
		CmtsRemoved cmtsRemoved = new CmtsRemovedBuilder().setAddress(input.getAddress()).setPort(input.getPort()).setTransactionId(transactionId).build();
		notificationProvider.publish(cmtsRemoved);
	}

	public void notifyConsumerOnCmtsUpdate(CmtsNode input, TransactionId transactionId) {
		CmtsUpdated cmtsRemoved = new CmtsUpdatedBuilder().setAddress(input.getAddress()).setPort(input.getPort()).setTransactionId(transactionId).build();
		notificationProvider.publish(cmtsRemoved);
	}

	@Override
	public Future<RpcResult<AddFlowOutput>> addFlow(AddFlowInput input) {
		Match match = input.getMatch();
		CmtsNode cmts = getCmtsNode(input);
		if (cmts != null)
			cmtsInstances.add(input.getNode().getValue());
		IClassifier classifier = buildClassifier(match);
		ITrafficProfile trafficProfie = null;
		for (Instruction i : input.getInstructions().getInstruction()) {
			if (i.getInstruction() instanceof ApplyActionsCase) {
				ApplyActionsCase aac = (ApplyActionsCase) i.getInstruction();
				for (Action a : aac.getApplyActions().getAction()) {
					if (a.getAction() instanceof FlowspecCase) {
						// not implemented
						// trafficProfie = buildTrafficProfile(((FlowspecCase)
						// a.getAction()).getFlowspec());
					} else if (a.getAction() instanceof BestEffortCase) {
						trafficProfie = buildTrafficProfile(((BestEffortCase) a.getAction()).getBestEffort());
						break;
					} else if (a.getAction() instanceof DocsisServiceClassNameCase) {
						trafficProfie = buildTrafficProfile(((DocsisServiceClassNameCase) a.getAction()).getDocsisServiceClassName());
						break;
					}
				}
			}
		}
		TransactionId transactionId = null;
		notifyConsumerOnCmtsAdd(cmts, transactionId);
		return Futures.immediateFuture(RpcResultBuilder.success(new AddFlowOutputBuilder().setTransactionId(transactionId).build()).build());
	}

	@Override
	public ITrafficProfile buildTrafficProfile(TrafficProfileDocsisServiceClassNameAttributes docsis) {
		return pcmmDataProcessor.process(docsis);
	}

	@Override
	public ITrafficProfile buildTrafficProfile(TrafficProfileBestEffortAttributes bestEffort) {
		return pcmmDataProcessor.process(bestEffort);
	}

	@Override
	public ITrafficProfile buildTrafficProfile(TrafficProfileFlowspecAttributes flowSpec) {
		return pcmmDataProcessor.process(flowSpec);
	}

	@Override
	public IClassifier buildClassifier(Match match) {
		return pcmmDataProcessor.process(match);
	}

	@Override
	public Future<RpcResult<RemoveFlowOutput>> removeFlow(RemoveFlowInput input) {
		UdpMatchRangesRpcRemoveFlow updRange = input.getMatch().getAugmentation(UdpMatchRangesRpcRemoveFlow.class);
		notifyConsumerOnCmtsRemove(getCmtsNode(input), null);
		return null;
	}

	@Override
	public Future<RpcResult<UpdateFlowOutput>> updateFlow(UpdateFlowInput input) {
		OriginalFlow foo = input.getOriginalFlow();
		UdpMatchRangesRpcUpdateFlowOriginal bar = foo.getMatch().getAugmentation(UdpMatchRangesRpcUpdateFlowOriginal.class);
		UpdatedFlow updated = input.getUpdatedFlow();
		UdpMatchRangesRpcUpdateFlowUpdated updatedRange = updated.getMatch().getAugmentation(UdpMatchRangesRpcUpdateFlowUpdated.class);
		notifyConsumerOnCmtsUpdate(getCmtsNode(input), null);
		return null;
	}

	@SuppressWarnings("unchecked")
	protected CmtsNode getCmtsNode(NodeContextRef input) {
		NodeRef nodeRef = input.getNode();
		InstanceIdentifier<Node> instanceIdentifier = (InstanceIdentifier<Node>) nodeRef.getValue();
		ReadOnlyTransaction rtransaction = dataBroker.newReadOnlyTransaction();
		CheckedFuture<Optional<Node>, ReadFailedException> value = rtransaction.read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
		rtransaction.close();
		Optional<Node> opt = null;
		try {
			opt = value.get();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
		Node node = opt.get();
		CmtsCapableNode cmts = node.getAugmentation(CmtsCapableNode.class);
		CmtsNode cmtsNode = cmts.getCmtsNode();
		return cmtsNode;
	}

	@Override
	public Collection<? extends RpcService> getImplementations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends ProviderFunctionality> getFunctionality() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSessionInitiated(ProviderContext session) {
		providerContext = session;
		notificationService = session.getSALService(NotificationProviderService.class);
		dataBroker = session.getSALService(DataBroker.class);
		InstanceIdentifier<CmtsNode> listenTo = InstanceIdentifier.create(Nodes.class).child(Node.class).augmentation(CmtsCapableNode.class).child(CmtsNode.class);
		listenerRegistration = dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, listenTo, this, DataChangeScope.BASE);
	}

	@Override
	public void onSessionInitialized(ConsumerContext session) {
		// Noop

	}

	public void onSessionAdded(/* Whatever you need per CmtsConnection */) {
		CompositeObjectRegistrationBuilder<OpendaylightPacketcableProvider> builder = CompositeObjectRegistration.<OpendaylightPacketcableProvider> builderFor(this);
		/*
		 * You will need a routedRpc registration per Cmts... I'm not doing the
		 * accounting of storing them here, but you will need to so you can
		 * close them when your provider is closed
		 */
		RoutedRpcRegistration<SalFlowService> registration = providerContext.addRoutedRpcImplementation(SalFlowService.class, this);
		/*
		 * You will need to get your identifier somewhere... this is your
		 * nodeId. I would recommend adoption a convention like
		 * "cmts:<ipaddress>" for CmtsCapableNodes
		 * registration.registerPath(NodeContext.class, getIdentifier());
		 */
	}

}

package org.opendaylight.controller.packetcable.provider;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.packetcable.provider.processors.PCMMDataProcessor;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileBestEffortAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileDocsisServiceClassNameAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.TrafficProfileFlowspecAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.BestEffortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.DocsisServiceClassNameCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140808.add.flow.input.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.FlowspecCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140120.CmtsCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140120.nodes.node.CmtsNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.SubscriberIdRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.TcpMatchRangesAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.TcpMatchRangesRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.UdpMatchRangesRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.tcp.match.ranges.attributes.TcpMatchRanges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.match.types.rev140120.udp.match.ranges.attributes.UpdMatchRanges;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration.CompositeObjectRegistrationBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.pcmm.gates.IClassifier;
import org.pcmm.gates.IExtendedClassifier;
import org.pcmm.gates.ITrafficProfile;
import org.pcmm.gates.impl.ExtendedClassifier;
import org.pcmm.rcd.IPCMMPolicyServer;
import org.pcmm.rcd.IPCMMPolicyServer.IPSCMTSClient;
import org.pcmm.rcd.impl.PCMMPolicyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private PCMMDataProcessor pcmmDataProcessor;
	private IPCMMPolicyServer policyServer;

	public OpendaylightPacketcableProvider() {
		executor = Executors.newCachedThreadPool();
		pcmmDataProcessor = new PCMMDataProcessor();
		policyServer=new PCMMPolicyServer();
		policyServer.startServer();
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
		// if (dataProvider != null) {
		// for (Iterator<InstanceIdentifier<CmtsInstance>> iter =
		// cmtsInstances.iterator(); iter.hasNext();) {
		// WriteTransaction tx = dataProvider.newWriteOnlyTransaction();
		// tx.delete(LogicalDatastoreType.OPERATIONAL, iter.next());
		// Futures.addCallback(tx.submit(), new FutureCallback<Void>() {
		// @Override
		// public void onSuccess(final Void result) {
		// logger.debug("Delete commit result: " + result);
		// }
		//
		// @Override
		// public void onFailure(final Throwable t) {
		// logger.error("Delete operation failed", t);
		// }
		// });
		// }
		// }
	}

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
		Match match = input.getMatch();
		//XXX this wrong fix it  
		CmtsNode cmts = (CmtsNode) input.getNode();
		///end wrong
		IClassifier classifier = buildClassifier(match);
		ITrafficProfile trafficProfie = null;
		for (Instruction i : input.getInstructions().getInstruction()) {
			if (i.getInstruction() instanceof ApplyActionsCase) {
				ApplyActionsCase aac = (ApplyActionsCase) i.getInstruction();
				for (Action a : aac.getApplyActions().getAction()) {
					if (a.getAction() instanceof FlowspecCase) {
						// not implemented
						// trafficProfie = buildTrafficProfile(((FlowspecCase) a.getAction()).getFlowspec());
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
		TransactionId transactionId=null;
		try {
			IPSCMTSClient requestCMTSConnection = policyServer.requestCMTSConnection(InetAddress.getByName(cmts.getAddress().getIpv4Address().getValue()));
			transactionId=new TransactionId(new BigInteger(String.valueOf(requestCMTSConnection.getTransactionId())));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if(transactionId==null)
		{
			return Futures.immediateFuture(RpcResultBuilder.<AddFlowOutput>failed().build());
		}
		return Futures.immediateFuture(
				RpcResultBuilder.success(
						new AddFlowOutputBuilder().setTransactionId(transactionId).build()
						).build()
						);
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

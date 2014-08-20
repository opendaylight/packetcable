package org.opendaylight.controller.packetcable.provider;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.opendaylight.controller.config.yang.config.packetcable_provider.impl.PacketcableProviderRuntimeMXBean;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140120.CmtsInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsAddInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsAddOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsAddOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsRemoveInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsRemoveOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsRemoveOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsUpdateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsUpdateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsUpdateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.CmtsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.PacketcableServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsBestEffortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsBestEffortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsDownstreamServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsDownstreamServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsFlowspecInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsFlowspecOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsNonRealTimePollingServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsNonRealTimePollingServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsRealTimePollingServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsRealTimePollingServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsUnsolicitedGrantServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsUnsolicitedGrantServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsUnsolicitedGrantServiceWithActivityDetectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileGetDefaultsUnsolicitedGrantServiceWithActivityDetectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsBestEffortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsBestEffortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsDownstreamServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsDownstreamServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsFlowspecInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsFlowspecOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsNonRealTimePollingServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsNonRealTimePollingServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsRealTimePollingServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsRealTimePollingServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsUnsolicitedGrantServiceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsUnsolicitedGrantServiceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsUnsolicitedGrantServiceWithActivityDetectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packetcable.service.rev140120.TrafficProfileUpdateDefaultsUnsolicitedGrantServiceWithActivityDetectionOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

@SuppressWarnings("unused")
public class OpendaylightPacketcableProvider implements
		PacketcableServiceService, PacketcableProviderRuntimeMXBean,
		DataChangeListener, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(OpendaylightPacketcableProvider.class);
	private NotificationProviderService notificationProvider;
	private DataBroker dataProvider;

	private final ExecutorService executor;

	// The following holds the Future for the current make toast task.
	// This is used to cancel the current toast.
	private final AtomicReference<Future<?>> currentConnectionsTasks = new AtomicReference<>();

	private List<InstanceIdentifier<CmtsInstance>> cmtsInstances;

	public OpendaylightPacketcableProvider() {
		executor = Executors.newCachedThreadPool();
		cmtsInstances = Lists.newArrayList();

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
			for (Iterator<InstanceIdentifier<CmtsInstance>> iter = cmtsInstances.iterator(); iter.hasNext();) {
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
	public Boolean getConnectionState() {
		return null;
	}

	@Override
	public void closeCmtsConnection() {
		for (Iterator<InstanceIdentifier<CmtsInstance>> iter = cmtsInstances.iterator(); iter.hasNext();) {

			// notificationProvider.publish(paramNotification)
			// iter.next().getId()
		}
	}

	@Override
	public Future<RpcResult<CmtsAddOutput>> cmtsAdd(CmtsAddInput input) {
		//TODO how to get this transaction id ???
		TransactionId transactionId = null;
		if (transactionId != null) {
			CmtsAdded cmtsAdded = new CmtsAddedBuilder().setCmtsRef(input.getCmtsRef()).setId(input.getId()).setConfigurationPoints(input.getConfigurationPoints()).setTransactionUri(input.getTransactionUri()).setTransactionId(transactionId).setNode(input.getNode()).setManagedCableModemSubscribers(input.getManagedCableModemSubscribers()).build();
			notificationProvider.publish(cmtsAdded);
			CmtsAddOutput output = new CmtsAddOutputBuilder().setTransactionId(transactionId).build();
			return Futures.immediateFuture(RpcResultBuilder.success(output).build());
		} else {
			return Futures.immediateFuture(RpcResultBuilder.<CmtsAddOutput> failed().build());
		}
	}

	@Override
	public Future<RpcResult<CmtsRemoveOutput>> cmtsRemove(CmtsRemoveInput input) {
		TransactionId transactionId = null;
		if (transactionId != null) {
			CmtsRemoved cmtsRemoved = new CmtsRemovedBuilder().setCmtsRef(input.getCmtsRef()).setId(input.getId()).setConfigurationPoints(input.getConfigurationPoints()).setTransactionUri(input.getTransactionUri()).setTransactionId(transactionId).setNode(input.getNode()).setManagedCableModemSubscribers(input.getManagedCableModemSubscribers()).build();
			notificationProvider.publish(cmtsRemoved);
			CmtsRemoveOutput output = new CmtsRemoveOutputBuilder().setTransactionId(transactionId).build();
			return Futures.immediateFuture(RpcResultBuilder.success(output).build());
		} else {
			return Futures.immediateFuture(RpcResultBuilder.<CmtsRemoveOutput> failed().build());
		}
	}

	@Override
	public Future<RpcResult<CmtsUpdateOutput>> cmtsUpdate(CmtsUpdateInput input) {
		TransactionId transactionId = null;
		if (transactionId != null) {
			CmtsUpdated cmtsUpdated = new CmtsUpdatedBuilder().setCmtsRef(input.getCmtsRef()).setId(input.getOriginalCmts().getId()).setConfigurationPoints(input.getOriginalCmts().getConfigurationPoints()).setTransactionUri(input.getTransactionUri()).setTransactionId(transactionId).setNode(input.getNode()).setManagedCableModemSubscribers(input.getOriginalCmts().getManagedCableModemSubscribers()).build();
			notificationProvider.publish(cmtsUpdated);
			CmtsUpdateOutput output = new CmtsUpdateOutputBuilder().setTransactionId(transactionId).build();
			return Futures.immediateFuture(RpcResultBuilder.success(output).build());
		} else {
			return Futures.immediateFuture(RpcResultBuilder.<CmtsUpdateOutput> failed().build());
		}
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsBestEffortOutput>> trafficProfileGetDefaultsBestEffort(TrafficProfileGetDefaultsBestEffortInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsDownstreamServiceOutput>> trafficProfileGetDefaultsDownstreamService(TrafficProfileGetDefaultsDownstreamServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsFlowspecOutput>> trafficProfileGetDefaultsFlowspec(TrafficProfileGetDefaultsFlowspecInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsNonRealTimePollingServiceOutput>> trafficProfileGetDefaultsNonRealTimePollingService(TrafficProfileGetDefaultsNonRealTimePollingServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsRealTimePollingServiceOutput>> trafficProfileGetDefaultsRealTimePollingService(TrafficProfileGetDefaultsRealTimePollingServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsUnsolicitedGrantServiceOutput>> trafficProfileGetDefaultsUnsolicitedGrantService(TrafficProfileGetDefaultsUnsolicitedGrantServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileGetDefaultsUnsolicitedGrantServiceWithActivityDetectionOutput>> trafficProfileGetDefaultsUnsolicitedGrantServiceWithActivityDetection(TrafficProfileGetDefaultsUnsolicitedGrantServiceWithActivityDetectionInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsBestEffortOutput>> trafficProfileUpdateDefaultsBestEffort(TrafficProfileUpdateDefaultsBestEffortInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsDownstreamServiceOutput>> trafficProfileUpdateDefaultsDownstreamService(TrafficProfileUpdateDefaultsDownstreamServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsFlowspecOutput>> trafficProfileUpdateDefaultsFlowspec(TrafficProfileUpdateDefaultsFlowspecInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsNonRealTimePollingServiceOutput>> trafficProfileUpdateDefaultsNonRealTimePollingService(TrafficProfileUpdateDefaultsNonRealTimePollingServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsRealTimePollingServiceOutput>> trafficProfileUpdateDefaultsRealTimePollingService(TrafficProfileUpdateDefaultsRealTimePollingServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsUnsolicitedGrantServiceOutput>> trafficProfileUpdateDefaultsUnsolicitedGrantService(TrafficProfileUpdateDefaultsUnsolicitedGrantServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<TrafficProfileUpdateDefaultsUnsolicitedGrantServiceWithActivityDetectionOutput>> trafficProfileUpdateDefaultsUnsolicitedGrantServiceWithActivityDetection(TrafficProfileUpdateDefaultsUnsolicitedGrantServiceWithActivityDetectionInput input) {
		// TODO Auto-generated method stub
		return null;
	}

}

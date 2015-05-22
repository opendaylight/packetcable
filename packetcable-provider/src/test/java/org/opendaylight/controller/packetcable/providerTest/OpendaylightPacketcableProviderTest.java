package org.opendaylight.controller.packetcable.providerTest;


import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.packetcable.provider.OpendaylightPacketcableProvider;
import org.opendaylight.controller.packetcable.provider.processors.PCMMDataProcessor;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.TrafficProfileBestEffortAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.TrafficProfileDocsisServiceClassNameAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.traffic.profile.rev140908.TrafficProfileFlowspecAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.broker.rev140909.CmtsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.cmts.rev140909.nodes.node.CmtsNode;



@RunWith(MockitoJUnitRunner.class)
public class OpendaylightPacketcableProviderTest {

	@Mock private NotificationProviderService notificationProvider;
	@Mock private DataBroker dataBroker;
	//@Spy private NotificationProviderService spyNotificationProvider;
	@Mock private PCMMDataProcessor pcmmDataProcessor;
	@Mock private CmtsAdded cmtsAdd;
	@Mock private CmtsRemoved cmtsRemove;
	@Mock private CmtsUpdated cmtsUpdate;
	@Mock private ExecutorService executorMock;
	@Mock private TrafficProfileDocsisServiceClassNameAttributes docsis;
	@Mock private TrafficProfileBestEffortAttributes bestEffort;
	@Mock private TrafficProfileFlowspecAttributes flowSpec;
	@Mock private CmtsNode cmtsNode;
	@Mock private TransactionId transactionId;

	@Mock private OpendaylightPacketcableProvider packetCableProv;// = mock(OpendaylightPacketcableProvider.class);

	@Before
	public void setUp() throws Exception {
		packetCableProv.setNotificationProvider(notificationProvider);
		packetCableProv.setDataProvider(dataBroker);
		assertNotNull(packetCableProv);
	}

	@Test
	public final void testNotifyConsumerOnCmtsAdd() {
		packetCableProv.notifyConsumerOnCmtsAdd(cmtsNode, transactionId);
		verify(packetCableProv, times(1)).notifyConsumerOnCmtsAdd(any(CmtsNode.class), any(TransactionId.class));
	}

	@Test
	public final void testNotifyConsumerOnCmtsRemove() {
		packetCableProv.notifyConsumerOnCmtsRemove(cmtsNode, transactionId);
		verify(packetCableProv, times(1)).notifyConsumerOnCmtsRemove(any(CmtsNode.class), any(TransactionId.class));
	}

	@Test
	public final void testNotifyConsumerOnCmtsUpdate() {
		packetCableProv.notifyConsumerOnCmtsUpdate(cmtsNode, transactionId);
		verify(packetCableProv, times(1)).notifyConsumerOnCmtsUpdate(any(CmtsNode.class), any(TransactionId.class));
	}

	@Test
	public final void testBuildTrafficProfileTrafficProfileDocsisServiceClassNameAttributes() {
		packetCableProv.buildTrafficProfile(docsis);
		verify(packetCableProv, times(1)).buildTrafficProfile(docsis);
	}

	@Test
	public final void testBuildTrafficProfileTrafficProfileBestEffortAttributes() {
		packetCableProv.buildTrafficProfile(bestEffort);
		verify(packetCableProv, times(1)).buildTrafficProfile(bestEffort);
	}

	@Test
	public final void testBuildTrafficProfileTrafficProfileFlowspecAttributes() {
		packetCableProv.buildTrafficProfile(flowSpec);
		verify(packetCableProv, times(1)).buildTrafficProfile(flowSpec);
	}

	@Test
	public final void testClose() throws ExecutionException, InterruptedException {
		packetCableProv.close();
		verify(packetCableProv, times(1)).close();
	}

}

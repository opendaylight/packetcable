/*
 * Copyright (c) 2015 CableLabs and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.packetcable.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.packetcable.provider.validation.DataValidator;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.controller.packetcable.provider.validation.impl.CcapsValidatorProviderFactory;
import org.opendaylight.controller.packetcable.provider.validation.impl.QosValidatorProviderFactory;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.serviceclass.name.profile.ServiceClassNameProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.serviceclass.name.profile.ServiceClassNameProfileBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.AppContext;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapContext;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapPollConnectionInput;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapPollConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapPollConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapSetConnectionInput;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapSetConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.CcapSetConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.PacketcableService;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.Qos;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.QosPollGatesInput;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.QosPollGatesOutput;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.QosPollGatesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.attributes.ConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccaps.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccaps.CcapBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gate.spec.GateSpec;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gate.spec.GateSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.Apps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.App;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.AppBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.AppKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.Subscribers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.SubscribersBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.Subscriber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.SubscriberBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.SubscriberKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.subscriber.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.subscriber.GatesBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.GateBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.GateKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.TrafficProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.TrafficProfileBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.traffic.profile.TrafficProfileChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.FlowSpecChoiceBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.ServiceClassNameChoiceBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.FlowSpecChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.qos.traffic.profile.traffic.profile.traffic.profile.choice.ServiceClassNameChoice;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.flow.spec.profile.FlowSpecProfile;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.pcmm.serviceclass.name.profile.ServiceClassNameProfile;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.pcmm.gates.IGateSpec.Direction;
import org.pcmm.gates.impl.DOCSISFlowSpecTrafficProfile;
import org.pcmm.gates.impl.DOCSISServiceClassNameTrafficProfile;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called by ODL framework to start this bundle.
 * <p>
 * This class is responsible for processing messages received from ODL's restconf interface.
 * TODO - Remove some of these state maps and move some of this into the PCMMService
 * TODO Don't implement PacketcableService, move that into an inner class
 */
@ThreadSafe
public class PacketcableProvider implements BindingAwareProvider, AutoCloseable, PacketcableService {

    private static final Logger logger = LoggerFactory.getLogger(PacketcableProvider.class);

    // keys to the /restconf/config/packetcable:ccaps and /restconf/config/packetcable:qos config datastore
    private static final InstanceIdentifier<Ccaps> ccapsIID = InstanceIdentifier.builder(Ccaps.class).build();
    private static final InstanceIdentifier<Qos> qosIID = InstanceIdentifier.builder(Qos.class).build();

    // TODO - Revisit these maps and remove the ones no longer necessary
    private final Map<String, Ccap> ccapMap = new ConcurrentHashMap<>();
    private final Map<String, Gate> gateMap = new ConcurrentHashMap<>();
    private final Map<String, String> gateCcapMap = new ConcurrentHashMap<>();
    private final Map<Subnet, Ccap> subscriberSubnetsMap = new ConcurrentHashMap<>();
    private final Map<ServiceClassName, List<Ccap>> downstreamScnMap = new ConcurrentHashMap<>();
    private final Map<ServiceClassName, List<Ccap>> upstreamScnMap = new ConcurrentHashMap<>();

    private final Executor executor = Executors.newSingleThreadExecutor();

    /**
     * Holds a PCMMService object for each CCAP being managed.
     */
    private final Map<String, PCMMService> pcmmServiceMap = new ConcurrentHashMap<>();

    /**
     * The ODL object used to broker messages throughout the framework
     */
    private DataBroker dataBroker;
    private MdsalUtils mdsalUtils;

    //Routed RPC Registration
    private RoutedRpcRegistration<PacketcableService> rpcRegistration;

    // Data change listeners/registrations
    private final CcapsDataTreeChangeListener ccapsDataTreeChangeListener = new CcapsDataTreeChangeListener();
    private final QosDataTreeChangeListener qosDataTreeChangeListener = new QosDataTreeChangeListener();

    private ListenerRegistration<DataTreeChangeListener> ccapsDataTreeChangeListenerRegistration;
    private ListenerRegistration<DataTreeChangeListener> qosDataTreeChangeListenerRegistration;

    /**
     * Constructor
     */
    public PacketcableProvider() {
        logger.info("Starting provider");
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        logger.info("Packetcable Session Initiated");
        logger.info("logging levels: error={}, warn={}, info={}, debug={}, trace={}", logger.isErrorEnabled(), logger.isWarnEnabled(), logger.isInfoEnabled(), logger.isDebugEnabled(), logger.isTraceEnabled());

        dataBroker = session.getSALService(DataBroker.class);

        mdsalUtils = new MdsalUtils(dataBroker);
        final DataTreeIdentifier<Ccap> ccapsDataTreeIid =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, ccapsIID.child(Ccap.class));

        final DataTreeIdentifier<Gate> appDataTreeIid =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                        qosIID.child(Apps.class).child(App.class).child(Subscribers.class).child(Subscriber.class).child(Gates.class).child(Gate.class));

        ccapsDataTreeChangeListenerRegistration =
                dataBroker.registerDataTreeChangeListener(ccapsDataTreeIid, new CcapsDataTreeChangeListener());

        qosDataTreeChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(appDataTreeIid, new QosDataTreeChangeListener());

        rpcRegistration = session.addRoutedRpcImplementation(PacketcableService.class, this);
        logger.info("onSessionInitiated().rpcRgistration: {}", rpcRegistration);

    }

    /**
     * Implemented from the AutoCloseable interface.
     */
    @Override
    public void close() throws ExecutionException, InterruptedException {
        if (ccapsDataTreeChangeListenerRegistration != null) {
            ccapsDataTreeChangeListenerRegistration.close();
        }

        if (qosDataTreeChangeListenerRegistration != null) {
            qosDataTreeChangeListenerRegistration.close();
        }
    }

    private void updateCcapMaps(final Ccap ccap) {
        // add ccap to the subscriberSubnets map
        for (final IpPrefix ipPrefix : ccap.getSubscriberSubnets()) {
            try {
                subscriberSubnetsMap.put(Subnet.createInstance(getIpPrefixStr(ipPrefix)), ccap);
            } catch (UnknownHostException e) {
                logger.error("updateSubscriberSubnets: {}:{} FAILED: {}", ipPrefix, ccap, e.getMessage());
            }
        }
        // ccap to upstream SCN map
        for (final ServiceClassName scn : ccap.getUpstreamScns()) {
            if (upstreamScnMap.containsKey(scn)) {
                upstreamScnMap.get(scn).add(ccap);
            } else {
                final List<Ccap> ccapList = new ArrayList<>();
                ccapList.add(ccap);
                upstreamScnMap.put(scn, ccapList);
            }
        }
        // ccap to downstream SCN map
        for (final ServiceClassName scn : ccap.getDownstreamScns()) {
            if (downstreamScnMap.containsKey(scn)) {
                downstreamScnMap.get(scn).add(ccap);
            } else {
                final List<Ccap> ccapList = new ArrayList<>();
                ccapList.add(ccap);
                downstreamScnMap.put(scn, ccapList);
            }
        }
    }

    private String getIpPrefixStr(final IpPrefix ipPrefix) {
        final Ipv4Prefix ipv4 = ipPrefix.getIpv4Prefix();
        if (ipv4 != null) {
            return ipv4.getValue();
        } else {
            return ipPrefix.getIpv6Prefix().getValue();
        }
    }

    public InetAddress getInetAddress(final String subId) {
        try {
            return InetAddress.getByName(subId);
        } catch (UnknownHostException e) {
            logger.error("getInetAddress: {} FAILED: {}", subId, e.getMessage());
            return null;
        }
    }

    private Ccap findCcapForSubscriberId(final InetAddress inetAddr) {
        // TODO replace this with a loading cache, https://github.com/google/guava/wiki/CachesExplained
        Ccap matchedCcap = null;
        int longestPrefixLen = -1;
        for (final Map.Entry<Subnet, Ccap> entry : subscriberSubnetsMap.entrySet()) {
            final Subnet subnet = entry.getKey();
            if (subnet.isInNet(inetAddr)) {
                int prefixLen = subnet.getPrefixLen();
                if (prefixLen > longestPrefixLen) {
                    matchedCcap = entry.getValue();
                    longestPrefixLen = prefixLen;
                }
            }
        }
        return matchedCcap;
    }

    private ServiceFlowDirection findScnOnCcap(final ServiceClassName scn, final Ccap ccap) {
        checkNotNull(scn);
        checkNotNull(ccap);

        if (upstreamScnMap.containsKey(scn)) {
            final List<Ccap> ccapList = upstreamScnMap.get(scn);
            if (ccapList.contains(ccap)) {
                return ServiceFlowDirection.Us;
            }
        } else if (downstreamScnMap.containsKey(scn)) {
            final List<Ccap> ccapList = downstreamScnMap.get(scn);
            if (ccapList.contains(ccap)) {
                return ServiceFlowDirection.Ds;
            }
        }
        return null;
    }

    private void removeCcapFromAllMaps(final Ccap ccap) {
        // remove the ccap from all maps
        // subscriberSubnets map
        for (final Map.Entry<Subnet, Ccap> entry : subscriberSubnetsMap.entrySet()) {
            if (entry.getValue() == ccap) {
                subscriberSubnetsMap.remove(entry.getKey());
            }
        }
        // ccap to upstream SCN map
        for (final Map.Entry<ServiceClassName, List<Ccap>> entry : upstreamScnMap.entrySet()) {
            final List<Ccap> ccapList = entry.getValue();
            ccapList.remove(ccap);
            if (ccapList.isEmpty()) {
                upstreamScnMap.remove(entry.getKey());
            }
        }
        // ccap to downstream SCN map
        for (final Map.Entry<ServiceClassName, List<Ccap>> entry : downstreamScnMap.entrySet()) {
            final List<Ccap> ccapList = entry.getValue();
            ccapList.remove(ccap);
            if (ccapList.isEmpty()) {
                downstreamScnMap.remove(entry.getKey());
            }
        }

        final PCMMService service = pcmmServiceMap.remove(ccap.getCcapId());
        if (service != null) {
            service.disconect();
        }
    }

    // ValidationException does not need to be thrown again
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private <T extends DataObject> void saveErrors(@Nonnull DataTreeModification<T> change, ValidationException exception) {

        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        InstanceIdentifier<T> iid = change.getRootPath().getRootIdentifier();
        //final ValidationException exception = exceptionMap.get(change);
        final T badData = change.getRootNode().getDataAfter();

        if (badData instanceof Ccap) {
            final Ccap ccap = (Ccap) badData;

            final Ccap opperationalCcap =
                    new CcapBuilder().setCcapId(ccap.getCcapId()).setError(exception.getErrorMessages()).build();

            @SuppressWarnings("unchecked") final InstanceIdentifier<Ccap> ccapIID = (InstanceIdentifier<Ccap>) change;
            writeTransaction.put(LogicalDatastoreType.OPERATIONAL, ccapIID, opperationalCcap);
        } else if (badData instanceof Gate) {
            final Gate gate = (Gate) badData;

            final Gate operationalGate =
                    new GateBuilder().setGateId(gate.getGateId()).setError(exception.getErrorMessages()).build();

            final Gates operationalGates =
                    new GatesBuilder().setGate(Collections.singletonList(operationalGate)).build();

            final SubscriberKey subscriberKey = InstanceIdentifier.keyOf(iid.firstIdentifierOf(Subscriber.class));
            final Subscriber operationalSubscriber =
                    new SubscriberBuilder().setSubscriberId(subscriberKey.getSubscriberId())
                            .setGates(operationalGates)
                            .build();

            final Subscribers operationalSubscribers =
                    new SubscribersBuilder().setSubscriber(Collections.singletonList(operationalSubscriber))
                            .build();

            final InstanceIdentifier<App> appIID = iid.firstIdentifierOf(App.class);
            final AppKey appKey = InstanceIdentifier.keyOf(appIID);
            final App operationalApp =
                    new AppBuilder().setAppId(appKey.getAppId()).setSubscribers(operationalSubscribers).build();


            writeTransaction.put(LogicalDatastoreType.OPERATIONAL, appIID, operationalApp);
        } else {
            // If you get here a developer forgot to add a type above
            logger.error("Unexpected type requested for error saving: {}", badData);
            throw new IllegalStateException("Unsupported type for error saving");
        }

        CheckedFuture<Void, TransactionCommitFailedException> future = writeTransaction.submit();

        try {
            future.checkedGet();
        } catch (TransactionCommitFailedException e) {
            logger.error("Failed to write errors to operational datastore", e);
        }
    }

    /**
     * Removes Ccaps if all Ccap instances are removed
     */
    private class CcapsCleaner extends AbstractCleaner<Ccaps> {

        public CcapsCleaner(final InstanceIdentifier<?> removedIID) {
            super(removedIID, Ccaps.class, LogicalDatastoreType.OPERATIONAL);
        }

        @Override
        protected boolean shouldClean(final Ccaps ccaps) {
            return ccaps.getCcap().isEmpty();
        }
    }


    /**
     * Removes Subscriber if all Gate instances are removed
     */
    private class SubscriberCleaner extends AbstractCleaner<Subscriber> {

        public SubscriberCleaner(InstanceIdentifier<Gate> removedGateIID) {
            super(removedGateIID, Subscriber.class, LogicalDatastoreType.OPERATIONAL);
        }

        @Override
        protected boolean shouldClean(final Subscriber subscriber) {
            return subscriber.getGates().getGate().isEmpty();
        }

        @Override
        protected void postRemove(InstanceIdentifier<Subscriber> subscriberIID) {
            executor.execute(new AppCleaner(subscriberIID));
        }
    }


    /**
     * Removes App if all Subscribers are removed.
     */
    private class AppCleaner extends AbstractCleaner<App> {

        public AppCleaner(InstanceIdentifier<Subscriber> removedSubscriberIID) {
            super(removedSubscriberIID, App.class, LogicalDatastoreType.OPERATIONAL);
        }

        @Override
        boolean shouldClean(final App app) {
            return app.getSubscribers().getSubscriber().isEmpty();
        }

        @Override
        void postRemove(final InstanceIdentifier<App> appIID) {
            //unregister app rpc path
            logger.info("Un-Registering App Routed RPC Path...");
            rpcRegistration.unregisterPath(AppContext.class, appIID);
            executor.execute(new AppsCleaner(appIID));
        }
    }


    /**
     * Removes Apps if all App instances are removed.
     */
    private class AppsCleaner extends AbstractCleaner<Apps> {

        public AppsCleaner(InstanceIdentifier<App> removedAppIID) {
            super(removedAppIID, Apps.class, LogicalDatastoreType.OPERATIONAL);
        }

        @Override
        protected boolean shouldClean(final Apps apps) {
            return apps.getApp().isEmpty();
        }
    }


    /**
     * Helper class to do the heavy lifting in removing object. Lets subclasses decide with
     * {@link #shouldClean(DataObject)}. <br>
     * <p>
     * Subclasses can react after an instance is removed by overriding {@link #postRemove(InstanceIdentifier)}
     *
     * @param <T>
     *         The type that will be removed
     */
    private abstract class AbstractCleaner<T extends DataObject> implements Runnable {
        final InstanceIdentifier<?> removedIID;
        final Class<T> tClass;
        final LogicalDatastoreType datastoreType;

        public AbstractCleaner(InstanceIdentifier<?> removedIID, Class<T> tClass, LogicalDatastoreType datastoreType) {
            this.removedIID = checkNotNull(removedIID);
            this.tClass = checkNotNull(tClass);
            this.datastoreType = checkNotNull(datastoreType);
        }

        @Override
        public void run() {
            InstanceIdentifier<T> tIID = removedIID.firstIdentifierOf(tClass);
            if (tIID != null) {
                Optional<T> optional = mdsalUtils.read(datastoreType, tIID);
                if (optional.isPresent()) {

                    if (shouldClean(optional.get())) {
                        if (mdsalUtils.delete(datastoreType, tIID)) {
                            postRemove(tIID);
                        } else {
                            removeFailed(tIID);
                        }
                    }

                }
            } else {
                logger.error("Expected to find InstanceIdentifier<{}> but was not found: {}", tClass.getSimpleName(),
                        removedIID);
            }
        }

        /**
         * If returns true the object will be removed from the datastore
         *
         * @param object
         *         The object that might be removed.
         * @return true if it should be removed.
         */
        abstract boolean shouldClean(final T object);

        /**
         * Called after an instance is removed.
         *
         * @param tIID
         *         the InstanceIdentifier of the removed object
         */
        void postRemove(InstanceIdentifier<T> tIID) {

        }

        void removeFailed(InstanceIdentifier<T> tIID) {
            logger.error("Failed to remove {}", tIID);
        }
    }


    /**
     * Listener for the packetcable:ccaps tree
     */
    private class CcapsDataTreeChangeListener extends AbstractDataTreeChangeListener<Ccap> {

        private final Set<InstanceIdentifier<Ccap>> updateQueue = Sets.newConcurrentHashSet();

        public CcapsDataTreeChangeListener() {
            super(Ccap.class,new DataValidator(new CcapsValidatorProviderFactory().build()));
        }

        @Override
        protected void handleCreatedData(final DataTreeModification<Ccap> change) {
            final Ccap ccap = change.getRootNode().getDataAfter();
            InstanceIdentifier<Ccap> iid = change.getRootPath().getRootIdentifier();

            // add service
            if (pcmmServiceMap.containsKey(ccap.getCcapId())) {
                logger.error("Already monitoring CCAP - " + ccap);
                return;
            }
            final PCMMService pcmmService = new PCMMService(IPCMMClient.CLIENT_TYPE, ccap);
            // TODO - may want to use the AMID but for the client type but probably not???
/*
                    final PCMMService pcmmService = new PCMMService(
                            thisCcap.getAmId().getAmType().shortValue(), thisCcap);
*/
            ConnectionBuilder connectionBuilder = new ConnectionBuilder();
            String message = pcmmService.addCcap();
            if (message.contains("200 OK")) {
                pcmmServiceMap.put(ccap.getCcapId(), pcmmService);
                ccapMap.put(ccap.getCcapId(), ccap);
                updateCcapMaps(ccap);
                logger.info("Created CCAP: {}/{} : {}", iid, ccap, message);
                logger.info("Created CCAP: {} : {}", iid, message);

                connectionBuilder.setConnected(true).setError(Collections.<String>emptyList());
            } else {
                logger.error("Create CCAP Failed: {} : {}", iid, message);

                connectionBuilder.setConnected(false).setError(Collections.singletonList(message));
            }

            //register rpc
            logger.info("Registering CCAP Routed RPC Path...");
            rpcRegistration.registerPath(CcapContext.class, iid);

            Optional<Ccap> optionalCcap = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, iid);

            final CcapBuilder responseCcapBuilder;
            if (optionalCcap.isPresent()) {
                responseCcapBuilder = new CcapBuilder(optionalCcap.get());
            } else {
                responseCcapBuilder = new CcapBuilder();
                responseCcapBuilder.setCcapId(ccap.getCcapId());
            }

            responseCcapBuilder.setConnection(connectionBuilder.build());

            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, iid, responseCcapBuilder.build());
        }

        @Override
        protected void handleUpdatedData(final DataTreeModification<Ccap> change) {
            //final Ccap ccap = (Ccap) change.getRootNode().getIdentifier();
            InstanceIdentifier<Ccap> iid = change.getRootPath().getRootIdentifier();
            // TODO actually support updates
            // update operation not allowed -- restore the original config object and complain

            // If this notification is coming from our modification ignore it.
            if (updateQueue.contains(iid)) {
                updateQueue.remove(iid);
                return;
            }

            final Ccap originalCcap = change.getRootNode().getDataBefore();
            //final Ccap updatedCcap = entry.getValue();

            //register rpc
            logger.info("Registering CCAP Routed RPC Path...");
            rpcRegistration.registerPath(CcapContext.class, iid);

            // restore the original data
            updateQueue.add(iid);
            mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, iid, originalCcap);
            logger.error("CCAP update not permitted {}", iid);
        }

        @Override
        protected void handleRemovedData(final DataTreeModification<Ccap> change) {

            InstanceIdentifier<Ccap> iid = change.getRootPath().getRootIdentifier();
            final Ccap nukedCcap = change.getRootNode().getDataBefore();
            removeCcapFromAllMaps(nukedCcap);

            //unregister ccap rpc path
            logger.info("Un-Registering CCAP Routed RPC Path...");
            rpcRegistration.unregisterPath(CcapContext.class, iid);

            mdsalUtils.delete(LogicalDatastoreType.OPERATIONAL, iid);

            // clean up ccaps level if it is now empty
            executor.execute(new CcapsCleaner(iid));
        }

        @Override
        protected void handleInvalidData(final DataTreeModification<Ccap> change, ValidationException validationException){
            // bad data write errors to operational datastore
            saveErrors(change, validationException);
        }

    }

    private class QosDataTreeChangeListener extends AbstractDataTreeChangeListener<Gate> {

        private final Set<InstanceIdentifier<Gate>> updateQueue = Sets.newConcurrentHashSet();

        public QosDataTreeChangeListener() {
            super(Gate.class,new DataValidator(new QosValidatorProviderFactory().build()));
        }

        @Override
        protected void handleCreatedData(final DataTreeModification<Gate> change) {

            InstanceIdentifier<Gate> gateIID = change.getRootPath().getRootIdentifier();
            final Gate newGate = change.getRootNode().getDataAfter();

            final String newGatePathStr = makeGatePathString(gateIID);

            // if a new app comes along add RPC registration
            final InstanceIdentifier<App> appIID = gateIID.firstIdentifierOf(App.class);
            // TBD verify if App ID exists first

            //register appID RPC path
            logger.info("Registering App Routed RPC Path...");
            rpcRegistration.registerPath(AppContext.class, appIID);

            final InstanceIdentifier<Subscriber> subscriberIID = gateIID.firstIdentifierOf(Subscriber.class);
            final SubscriberKey subscriberKey = InstanceIdentifier.keyOf(subscriberIID);
            final InetAddress subscriberAddr = getInetAddress(subscriberKey.getSubscriberId());
            if (subscriberAddr == null) {
                final String msg = String.format("subscriberId must be a valid ipaddress: %s",
                        subscriberKey.getSubscriberId());
                logger.error(msg);
                saveGateError(gateIID, newGatePathStr, msg);
                return;
            }

            final Ccap ccap = findCcapForSubscriberId(subscriberAddr);
            if (ccap == null) {
                final String msg = String.format("Unable to find Ccap for subscriber %s: @ %s",
                        subscriberKey.getSubscriberId(), newGatePathStr);
                logger.error(msg);
                saveGateError(gateIID, newGatePathStr, msg);
                return;
            }

            final PCMMService pcmmService = pcmmServiceMap.get(ccap.getCcapId());
            if (pcmmService == null) {
                final String msg =
                        String.format("Unable to locate PCMM Service for CCAP: %s ; with subscriber: %s", ccap,
                                subscriberKey.getSubscriberId());
                logger.error(msg);
                saveGateError(gateIID, newGatePathStr, msg);
                return;
            }

            //
            // set up gate builder with known fields (and some empty ones)
            //
            final GateBuilder gateBuilder = new GateBuilder();
            gateBuilder.setGateId(newGate.getGateId())
                    .setGatePath(newGatePathStr)
                    .setCcapId(ccap.getCcapId())
                    .setClassifiers(newGate.getClassifiers())
                    .setGateSpec(newGate.getGateSpec())
                    .setCopsGateState("")
                    .setCopsGateTimeInfo("")
                    .setCopsGateUsageInfo("");

            ServiceFlowDirection scnDirection = null;
            
            if (newGate.getTrafficProfile().getTrafficProfileChoice() instanceof ServiceClassNameChoice) {    
                final ServiceClassName scn =
                    ((ServiceClassNameChoice)newGate.getTrafficProfile()
                     .getTrafficProfileChoice())
                    .getServiceClassNameProfile()
                    .getServiceClassName();
                scnDirection = findScnOnCcap(scn, ccap);
                if (scnDirection == null) {
                    final String msg =
                        String.format("SCN %s not found on CCAP %s for %s", scn, ccap.getCcapId(), newGatePathStr);
                    logger.error(msg);
                    saveGateError(gateIID, newGatePathStr, msg);
                    return;
                }
                ServiceClassNameProfileBuilder scnBuilder = new ServiceClassNameProfileBuilder();
                scnBuilder.setServiceClassName(scn);
                ServiceClassNameProfile scnProfile = scnBuilder.build();
                ServiceClassNameChoiceBuilder scncBuilder = new ServiceClassNameChoiceBuilder();
                scncBuilder.setServiceClassNameProfile(scnProfile);
                ServiceClassNameChoice scnChoice = scncBuilder.build();
                TrafficProfileBuilder trafficProfileBuilder = new TrafficProfileBuilder();
                trafficProfileBuilder.setTrafficProfileChoice(scnChoice);
                TrafficProfile trafficProfile = trafficProfileBuilder.build();
                gateBuilder.setTrafficProfile(trafficProfile);
            }
            else {
                gateBuilder.setTrafficProfile(newGate.getTrafficProfile());
            }
            
            //
            // since we may be modifying the contents of the original request GateSpec
            // to update flow direction (based on the ccap SCN configuration) we need to
            // rebuild the requested gate spec and replace the existing one in the gate builder
            //
            final GateSpecBuilder gateSpecBuilder = new GateSpecBuilder();
            gateSpecBuilder.setDirection(scnDirection);
            gateSpecBuilder.setDscpTosMask(newGate.getGateSpec().getDscpTosMask());
            gateSpecBuilder.setDscpTosOverwrite(newGate.getGateSpec().getDscpTosOverwrite());
            gateSpecBuilder.setSessionClassId(newGate.getGateSpec().getSessionClassId());
            gateSpecBuilder.setInactivityTimer(newGate.getGateSpec().getInactivityTimer());
            final GateSpec gateSpec = gateSpecBuilder.build();
            gateBuilder.setGateSpec(gateSpec);

            //
            // build the gate to be requested
            //
            gateBuilder.setTimestamp(getNowTimeStamp());

            final Gate requestGate = gateBuilder.build();

            //
            // send gate request to Ccap
            //
            PCMMService.GateSendStatus status =
                    pcmmService.sendGateSet(newGatePathStr, subscriberAddr, requestGate);
            if (status.didSucceed()) {
                gateMap.put(newGatePathStr, requestGate);
                gateCcapMap.put(newGatePathStr, ccap.getCcapId());

                //
                // inquire as to the status, and implementation info of the requested gate
                //
                PCMMService.GateSendStatus infoStatus = pcmmService.sendGateInfo(newGatePathStr);

                if (infoStatus.didSucceed()) {
                    //
                    // update builder with info for operational storage
                    //
                    gateBuilder.setCopsGateState(
                            infoStatus.getCopsGateState() + "/" + infoStatus.getCopsGateStateReason())
                            .setCopsGateTimeInfo(infoStatus.getCopsGateTimeInfo())
                            .setCopsGateUsageInfo(infoStatus.getCopsGateUsageInfo())
                            .setCopsGateId(status.getCopsGateId());
                } else {
                    List<String> errors = new ArrayList<>(2);

                    // Keep GateSetErrors
                    if (gateBuilder.getError() != null) {
                        errors.addAll(gateBuilder.getError());
                    }

                    errors.add(infoStatus.getMessage());
                    gateBuilder.setError(errors);
                }
            }
            else {
                gateBuilder.setError(Collections.singletonList(status.getMessage()));
            }

            Gate operationalGate = gateBuilder.build();
            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIID, operationalGate);
        }

        private void saveGateError(@Nonnull final InstanceIdentifier<Gate> gateIID, @Nonnull final String gatePathStr,
                                   @Nonnull final String error) {
            checkNotNull(gateIID);
            checkNotNull(error);

            final GateBuilder gateBuilder = new GateBuilder();
            gateBuilder.setGateId(InstanceIdentifier.keyOf(gateIID).getGateId())
                    .setGatePath(gatePathStr)
                    .setCopsGateId("")
                    .setCopsGateState("N/A");

            gateBuilder.setError(Collections.singletonList(error));

            Gate operationalGate = gateBuilder.build();

            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIID, operationalGate);
        }

        @Override
        protected void handleUpdatedData(final DataTreeModification<Gate> change) {
            InstanceIdentifier<Gate> gateIID = change.getRootPath().getRootIdentifier();
            //final Gate newGate = (Gate) change.getRootNode().getIdentifier();
            // TODO actually support updates

            // update operation not allowed -- restore the original config object and complain

            // If this notification is coming from our modification ignore it.
            if (updateQueue.contains(gateIID)) {
                updateQueue.remove(gateIID);
                return;
            }

            final Gate originalGate = change.getRootNode().getDataBefore();

            // restores the original data
            updateQueue.add(gateIID);
            mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, gateIID, originalGate);
            logger.error("Update not permitted {}", gateIID);
        }

        @Override
        protected void handleRemovedData(final DataTreeModification<Gate> change) {
            InstanceIdentifier<Gate> removedGateIID = change.getRootPath().getRootIdentifier();
            final Gate newGate = change.getRootNode().getDataBefore();

            mdsalUtils.delete(LogicalDatastoreType.OPERATIONAL, removedGateIID);

            executor.execute(new SubscriberCleaner(removedGateIID));

            final String gatePathStr = makeGatePathString(removedGateIID);

            if (gateMap.containsKey(gatePathStr)) {
                final Gate thisGate = gateMap.remove(gatePathStr);
                final String gateId = thisGate.getGateId();
                final String ccapId = gateCcapMap.remove(gatePathStr);
                final Ccap thisCcap = ccapMap.get(ccapId);
                final PCMMService service = pcmmServiceMap.get(thisCcap.getCcapId());
                if (service != null) {
                    service.sendGateDelete(gatePathStr);
                    logger.info("onDataChanged(): removed QoS gate {} for {}/{}/{}: ", gateId, ccapId, gatePathStr,thisGate);
                } else {
                    logger.warn("Unable to send to locate PCMMService to send gate delete message with CCAP - "
                            + thisCcap);
                }
            }

        }

        @Override
        protected void handleInvalidData(final DataTreeModification<Gate> change, ValidationException validationException){
            // bad data write errors to operational datastore
            saveErrors(change, validationException);
        }

        private String makeGatePathString(InstanceIdentifier<Gate> iid) {
            final InstanceIdentifier<App> appIID = iid.firstIdentifierOf(App.class);
            final AppKey appKey = InstanceIdentifier.keyOf(appIID);

            final InstanceIdentifier<Subscriber> subscriberIID = iid.firstIdentifierOf(Subscriber.class);
            final SubscriberKey subscriberKey = InstanceIdentifier.keyOf(subscriberIID);

            final GateKey gateKey = InstanceIdentifier.keyOf(iid);

            return appKey.getAppId() + "/" + subscriberKey.getSubscriberId() + "/" + gateKey.getGateId();
        }
    }

    @Override
    public Future<RpcResult<CcapSetConnectionOutput>> ccapSetConnection(CcapSetConnectionInput input) {
        // TODO refactor this method into smaller parts

        InstanceIdentifier<Ccap> ccapIid = (InstanceIdentifier<Ccap>) input.getCcapId();
        List<String> outputError = new ArrayList<String>();
        String rpcResponse = null;
        Boolean inputIsConnected = input.getConnection().isConnected();
        Boolean effectiveIsConnected = null;
        String ccapId = input.getCcapId().firstIdentifierOf(Ccap.class).firstKeyOf(Ccap.class).getCcapId();
        PCMMService pcmmService = pcmmServiceMap.get(ccapId);

        if (!inputIsConnected) {
            // set connected false
            if (pcmmService.getPcmmPdpSocket()) {
                outputError.add(ccapId + ": CCAP COPS socket is already closed");
                effectiveIsConnected = false;
            } else {
                //if (!pcmmService.getPcmmCcapClientIsConnected()) {
                outputError.add(ccapId + ": CCAP client is disconnected with error: "
                        + pcmmService.getPcmmCcapClientConnectErrMsg());
                //}
                pcmmService.ccapClient.disconnect();
                effectiveIsConnected = false;
            }
        } else {
            // set connected true
            if (!pcmmService.getPcmmPdpSocket() && pcmmService.getPcmmCcapClientIsConnected()) {
                outputError.add(ccapId + ": CCAP COPS socket is already open");
                outputError.add(ccapId + ": CCAP client is connected");
                effectiveIsConnected = true;
            } else {
                if (pcmmService.getPcmmCcapClientIsConnected()) {
                    pcmmService.ccapClient.disconnect();
                }
                pcmmService.ccapClient.connect();
                if (pcmmService.getPcmmCcapClientIsConnected()) {
                    effectiveIsConnected = true;
                    outputError.add(ccapId + ": CCAP client is connected");
                } else {
                    effectiveIsConnected = false;
                    outputError.add(ccapId + ": CCAP client is disconnected with error: "
                            + pcmmService.getPcmmCcapClientConnectErrMsg());
                }
            }
        }

        DateAndTime connectionDateAndTime = getNowTimeStamp();
        org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.set.connection.output.ccap.ConnectionBuilder
                connectionRpcOutput =
                new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.set.connection.output.ccap.ConnectionBuilder()
                        .setConnected(effectiveIsConnected)
                        .setError(outputError)
                        .setTimestamp(connectionDateAndTime);

        org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.set.connection.output.CcapBuilder ccapRpcOutput =
                new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.set.connection.output.CcapBuilder().setCcapId(
                        ccapId).setConnection(connectionRpcOutput.build());


        ConnectionBuilder connectionOps = new ConnectionBuilder().setConnected(effectiveIsConnected)
                .setError(outputError)
                .setTimestamp(connectionDateAndTime);

        CcapBuilder responseCcapBuilder = new CcapBuilder().setCcapId(ccapId).setConnection(connectionOps.build());


        mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, ccapIid, responseCcapBuilder.build());


        DateAndTime rpcDateAndTime = getNowTimeStamp();
        rpcResponse = ccapId + ": CCAP set complete";
        CcapSetConnectionOutputBuilder outputBuilder =
                new CcapSetConnectionOutputBuilder().setCcap(ccapRpcOutput.build())
                        .setResponse(rpcResponse)
                        .setTimestamp(rpcDateAndTime);

        return Futures.immediateFuture(RpcResultBuilder.success(outputBuilder.build()).build());
    }



    @Override
    public Future<RpcResult<CcapPollConnectionOutput>> ccapPollConnection(CcapPollConnectionInput input) {
        // TODO refactor this method into smaller parts

        InstanceIdentifier<Ccap> ccapIid = (InstanceIdentifier<Ccap>) input.getCcapId();
        List<String> outputError = new ArrayList<String>();

        String ccapId = input.getCcapId().firstIdentifierOf(Ccap.class).firstKeyOf(Ccap.class).getCcapId();
        PCMMService pcmmService = pcmmServiceMap.get(ccapId);
        Boolean effectiveIsConnected = true;
        String response = null;
        org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.poll.connection.output.ccap.ConnectionBuilder
                connectionRpcOutput =
                new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.poll.connection.output.ccap.ConnectionBuilder();

        if (pcmmService != null) {
            if (pcmmService.getPcmmPdpSocket()) {
                outputError.add(ccapId + ": CCAP Cops socket is closed");
                if (!pcmmService.getPcmmCcapClientIsConnected()) {
                    outputError.add(ccapId + ": CCAP client is disconnected with error: "
                            + pcmmService.getPcmmCcapClientConnectErrMsg());
                }
                effectiveIsConnected = false;
            } else {
                //outputError.add(String.format(ccapId+": CCAP Cops socket is open"));
                if (!pcmmService.getPcmmCcapClientIsConnected()) {
                    outputError.add(ccapId + ": CCAP client is disconnected with error: "
                            + pcmmService.getPcmmCcapClientConnectErrMsg());
                    effectiveIsConnected = false;
                } else {
                    outputError.add(ccapId + ": CCAP client is connected");
                }
            }
            DateAndTime connectionDateAndTime = getNowTimeStamp();


            ConnectionBuilder connectionOps = new ConnectionBuilder().setConnected(effectiveIsConnected)
                    .setError(outputError)
                    .setTimestamp(connectionDateAndTime);

            CcapBuilder responseCcapBuilder = new CcapBuilder().setCcapId(ccapId).setConnection(connectionOps.build());

            connectionRpcOutput =
                    new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.poll.connection.output.ccap.ConnectionBuilder()
                            .setConnected(effectiveIsConnected)
                            .setError(outputError)
                            .setTimestamp(connectionDateAndTime);

            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, ccapIid, responseCcapBuilder.build());
            response = ccapId + ": CCAP poll complete";
        } else {
            //pcmmService is null, do not poll
            response = ccapId + ": CCAP connection null; no poll performed";
        }

        DateAndTime rpcDateAndTime = getNowTimeStamp();

        org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.poll.connection.output.CcapBuilder ccapRpcOutput =
                new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.ccap.poll.connection.output.CcapBuilder().setCcapId(
                        ccapId).setConnection(connectionRpcOutput.build());

        CcapPollConnectionOutputBuilder outputBuilder =
                new CcapPollConnectionOutputBuilder().setCcap(ccapRpcOutput.build())
                        .setResponse(response)
                        .setTimestamp(rpcDateAndTime);

        return Futures.immediateFuture(RpcResultBuilder.success(outputBuilder.build()).build());
    }



    private App readAppFromOperationalDatastore(InstanceIdentifier<App> appIid) {
        Optional<App> optionalApp = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, appIid);
        AppBuilder thisAppBuilder = new AppBuilder(optionalApp.get());
        App thisApp = thisAppBuilder.build();
        logger.info("readAppFromConfigDatastore() retrived App: " + thisApp.getAppId());
        return thisApp;
    }

    private Gate readGateFromOperationalDatastore(InstanceIdentifier<Gate> gateIid) {
        Optional<Gate> optionalGate = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, gateIid);
        if (optionalGate.isPresent()) {
            GateBuilder gateBuilder = new GateBuilder(optionalGate.get());
            Gate thisGate = gateBuilder.build();
            return thisGate;
        } else {
            return null;
        }
    }

    private Subscriber readSubscriberFromOperationalDatastore(InstanceIdentifier<Subscriber> subscriberIid) {
        Optional<Subscriber> optionalSubscriber = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, subscriberIid);
        if (optionalSubscriber.isPresent()) {
            SubscriberBuilder subscriberBuilder = new SubscriberBuilder(optionalSubscriber.get());
            Subscriber thisSubscriber = subscriberBuilder.build();
            return thisSubscriber;
        } else {
            return null;
        }
    }



    @Override
    public Future<RpcResult<QosPollGatesOutput>> qosPollGates(QosPollGatesInput input) {
        // TODO refactor this method into smaller parts

        InstanceIdentifier<App> appIid = (InstanceIdentifier<App>) input.getAppId();
        //logger.info("qospollgates appIid : "+appIid.toString());
        App app = readAppFromOperationalDatastore(appIid);
        //logger.info("qospollgates app : "+app.toString());
        AppKey appKey = InstanceIdentifier.keyOf(appIid);
        String inputSubscriberId = input.getSubscriberId();
        String inputGateId = input.getGateId();
        List<String> gateOutputError = Collections.emptyList();
        String subscriberId = null;
        String gateId = null;
        String ccapId = null;
        String gatePathStr = null;
        String opsCopsGateId = null;
        Gate opsGate = null;

        String rpcResponse = null;

        org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.qos.poll.gates.output.GateBuilder gateOutputBuilder =
                new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.qos.poll.gates.output.GateBuilder();

        GateBuilder gateBuilder = new GateBuilder();

        if (inputSubscriberId != null) {
            if (inputGateId != null) {
                //Subscriber Id and Gate Id provided, only one gate to be poolled

                //generate the gateiid
                InstanceIdentifier<Gate> gateIid = appIid.builder()
                        .child(Subscribers.class)
                        .child(Subscriber.class, new SubscriberKey(inputSubscriberId))
                        .child(Gates.class)
                        .child(Gate.class, new GateKey(inputGateId))
                        .build();


                opsGate = readGateFromOperationalDatastore(gateIid);

                //does the gate exists in the Operational DS?
                if (opsGate == null) {
                    gatePathStr = appKey.getAppId() + "/" + inputSubscriberId + "/" + inputGateId;
                    rpcResponse = gatePathStr + ": gate does not exist in the system; gate poll not performed";
                } else {
                    opsCopsGateId = opsGate.getCopsGateId();
                    gatePathStr = opsGate.getGatePath();

                    if ((!Objects.equals(opsCopsGateId, "")) && (!Objects.equals(opsCopsGateId, null))) {
                        ccapId = findCcapForSubscriberId(getInetAddress(inputSubscriberId)).getCcapId();
                        PCMMService pcmmService = pcmmServiceMap.get(ccapId);
                        //is the CCAP socket open?
                        if (!pcmmService.getPcmmPdpSocket() && pcmmService.getPcmmCcapClientIsConnected()) {
                            PCMMService.GateSendStatus status = pcmmService.sendGateInfo(gatePathStr);
                            DateAndTime gateDateAndTime = getNowTimeStamp();
                            //logger.info("qospollgates Gate Status : GateID/"+status.getCopsGateId());
                            //logger.info("qospollgates Gate Status : Message/"+status.getMessage());
                            //logger.info("qospollgates Gate Status : DidSucceed/"+status.didSucceed());
                            gateOutputError = Collections.singletonList(status.getMessage());

                            gateOutputBuilder.setGatePath(gatePathStr)
                                    .setCcapId(ccapId)
                                    .setCopsGateState(status.getCopsGateState() + "/" + status.getCopsGateStateReason())
                                    .setCopsGateTimeInfo(status.getCopsGateTimeInfo())
                                    .setCopsGateUsageInfo(status.getCopsGateUsageInfo())
                                    .setCopsGateId(status.getCopsGateId())
                                    .setError(gateOutputError)
                                    .setTimestamp(gateDateAndTime);

                            gateBuilder.setGateId(inputGateId)
                                    .setGatePath(gatePathStr)
                                    .setCcapId(ccapId)
                                    .setCopsGateState(status.getCopsGateState() + "/" + status.getCopsGateStateReason())
                                    .setCopsGateTimeInfo(status.getCopsGateTimeInfo())
                                    .setCopsGateUsageInfo(status.getCopsGateUsageInfo())
                                    .setCopsGateId(status.getCopsGateId())
                                    .setError(gateOutputError)
                                    .setTimestamp(gateDateAndTime);

                            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIid, gateBuilder.build());
                            rpcResponse = gatePathStr + ": gate poll complete";
                        } else {
                            rpcResponse =
                                    ccapId + ": CCAP socket is down or client disconnected; gate poll not performed";
                        }
                    } else {
                        rpcResponse = gatePathStr + ": gate not active; gate poll not performed";
                    }
                }
            } else {
                //inputGateId is null; pool all gates for the subscriber if the sub exists

                //generate active subIid
                InstanceIdentifier<Subscriber> subIid = appIid.builder()
                        .child(Subscribers.class)
                        .child(Subscriber.class, new SubscriberKey(inputSubscriberId))
                        .build();
                //does the subscriber provided exists in the Operational Datastore?
                Subscriber sub = readSubscriberFromOperationalDatastore(subIid);
                if (sub != null) {
                    //If Subscriber exsits poll all gates for the subscriber
                    subscriberId = sub.getSubscriberId();
                    List<Gate> gateList = sub.getGates().getGate();
                    for (Gate gate : gateList) {
                        //generate active gateIid
                        gateId = gate.getGateId();
                        InstanceIdentifier<Gate> gateIid =
                                subIid.builder().child(Gates.class).child(Gate.class, new GateKey(gateId)).build();

                        opsGate = readGateFromOperationalDatastore(gateIid);
                        opsCopsGateId = opsGate.getCopsGateId();
                        //generate active gatePathStr
                        gatePathStr = appKey.getAppId() + "/" + subscriberId + "/" + gateId;

                        if ((!Objects.equals(opsCopsGateId, "")) && (!Objects.equals(opsCopsGateId, null))) {
                            ccapId = findCcapForSubscriberId(getInetAddress(subscriberId)).getCcapId();
                            PCMMService pcmmService = pcmmServiceMap.get(ccapId);
                            //is the CCAP socket open?
                            if (!pcmmService.getPcmmPdpSocket() && pcmmService.getPcmmCcapClientIsConnected()) {
                                PCMMService.GateSendStatus status = pcmmService.sendGateInfo(gatePathStr);
                                DateAndTime gateDateAndTime = getNowTimeStamp();

                                gateBuilder.setGateId(gateId)
                                        .setGatePath(gatePathStr)
                                        .setCcapId(ccapId)
                                        .setCopsGateState(
                                                status.getCopsGateState() + "/" + status.getCopsGateStateReason())
                                        .setCopsGateTimeInfo(status.getCopsGateTimeInfo())
                                        .setCopsGateUsageInfo(status.getCopsGateUsageInfo())
                                        .setCopsGateId(status.getCopsGateId())
                                        .setError(gateOutputError)
                                        .setTimestamp(gateDateAndTime);

                                mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIid, gateBuilder.build());
                            } else {
                                logger.info(
                                        "qospollgates: {}: CCAP Cops socket is down or client disconnected; gate poll not performed",
                                        ccapId);
                            }
                        } else {
                            //TODO define what happens if a gate is not active.. is nothing ok?
                            logger.info("qospollgates: {}: gate not active; gate poll not performed", gatePathStr);
                        }
                    } //for
                    rpcResponse = inputSubscriberId + "/: subscriber subtree poll in progress";
                } else {
                    rpcResponse =
                            inputSubscriberId + "/: subscriber is not defined in the system, gate poll not performed";
                }
            }
        } //inputSubId if
        else {
            // inputSubId is null
            if (inputGateId != null) {
                gatePathStr = appKey.getAppId() + "/" + inputSubscriberId + "/" + inputGateId;
                rpcResponse = gatePathStr + ": Subscriber ID not provided; gate poll not performed";
            } else {
                //poll all gates for the appId
                PollAllGatesForApp pollAllGatesForApp = new PollAllGatesForApp(appIid,app);
                Thread t = new Thread(pollAllGatesForApp);
                t.start();
                rpcResponse = appKey.getAppId() + "/: gate subtree poll in progress";
            }
        }

        DateAndTime rpcDateAndTime = getNowTimeStamp();

        QosPollGatesOutputBuilder outputBuilder = new QosPollGatesOutputBuilder().setTimestamp(rpcDateAndTime)
                .setResponse(rpcResponse)
                .setGate(gateOutputBuilder.build());
        return Futures.immediateFuture(RpcResultBuilder.success(outputBuilder.build()).build());
    }
    private class PollAllGatesForApp implements Runnable {

        private InstanceIdentifier <App> appIid;
        private App app;

        private PollAllGatesForApp (InstanceIdentifier <App> appIid, App app) {
            this.app = app;
            this.appIid = appIid;
        }

        @Override
        public void run() {

            org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.qos.poll.gates.output.GateBuilder gateOutputBuilder =
                    new org.opendaylight.yang.gen.v1.urn.packetcable.rev170125.qos.poll.gates.output.GateBuilder();

            GateBuilder gateBuilder = new GateBuilder();

            //generate appKey
            AppKey appKey = InstanceIdentifier.keyOf(appIid);

            Subscribers subs = app.getSubscribers();
            logger.info("qospollgates subscribers: " + subs.toString());

            List<Subscriber> subList = subs.getSubscriber();
            logger.info("qospollgates subList: " + subList.toString());

            for (Subscriber sub : subList) {
                //generate active subIid
                String subscriberId = sub.getSubscriberId();
                InstanceIdentifier<Subscriber> subIid = appIid.builder()
                        .child(Subscribers.class)
                        .child(Subscriber.class, new SubscriberKey(subscriberId))
                        .build();

                List<Gate> gateList = sub.getGates().getGate();

                for (Gate gate : gateList) {
                    //logger.info("qospollgates active gate: "+gate);

                    //generate active gateIid
                    String gateId = gate.getGateId();
                    InstanceIdentifier<Gate> gateIid =
                            subIid.builder().child(Gates.class).child(Gate.class, new GateKey(gateId)).build();


                    Gate opsGate = readGateFromOperationalDatastore(gateIid);
                    String opsCopsGateId = opsGate.getCopsGateId();
                    //generate active gatePathStr
                    String gatePathStr = appKey.getAppId() + "/" + subscriberId + "/" + gateId;

                    if ((!Objects.equals(opsCopsGateId, "")) && (!Objects.equals(opsCopsGateId, null))) {
                        String ccapId = findCcapForSubscriberId(getInetAddress(subscriberId)).getCcapId();
                        PCMMService pcmmService = pcmmServiceMap.get(ccapId);
                        //is the CCAP socket open?
                        if (!pcmmService.getPcmmPdpSocket() && pcmmService.getPcmmCcapClientIsConnected()) {
                            PCMMService.GateSendStatus status = pcmmService.sendGateInfo(gatePathStr);
                            DateAndTime gateDateAndTime = getNowTimeStamp();
                            List<String> gateOutputError = Collections.singletonList(status.getMessage());


                            gateBuilder.setGateId(gateId)
                                    .setGatePath(gatePathStr)
                                    .setCcapId(ccapId)
                                    .setCopsGateState(
                                            status.getCopsGateState() + "/" + status.getCopsGateStateReason())
                                    .setCopsGateTimeInfo(status.getCopsGateTimeInfo())
                                    .setCopsGateUsageInfo(status.getCopsGateUsageInfo())
                                    .setCopsGateId(status.getCopsGateId())
                                    .setError(gateOutputError)
                                    .setTimestamp(gateDateAndTime);

                            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIid, gateBuilder.build());
                        } else {
                            logger.info(
                                    "qospollgates: {}: CCAP socket is down or client disconnected; gate poll not performed",
                                    ccapId);
                        }
                    } else {
                        //TODO define what happens if a gate is not active.. is nothing ok
                        logger.info("qospollgates: {}: gate not active; gate poll not performed", gatePathStr);
                    }
                }
            }
        }
    }


    private DateAndTime getNowTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return new DateAndTime(dateFormat.format(new Date()));
    }
}

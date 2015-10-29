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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.packetcable.provider.validation.DataValidator;
import org.opendaylight.controller.packetcable.provider.validation.ValidationException;
import org.opendaylight.controller.packetcable.provider.validation.Validator;
import org.opendaylight.controller.packetcable.provider.validation.impl.CcapsValidatorProviderFactory;
import org.opendaylight.controller.packetcable.provider.validation.impl.QosValidatorProviderFactory;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.Qos;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.ccap.attributes.ConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.ccaps.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.ccaps.CcapBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.Apps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.App;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.AppBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.AppKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.Subscribers;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.SubscribersBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.Subscriber;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.SubscriberBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.SubscriberKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.subscriber.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.subscriber.GatesBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.Gate;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.GateBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev151026.pcmm.qos.gates.apps.app.subscribers.subscriber.gates.GateKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called by ODL framework to start this bundle.
 * <p>
 * This class is responsible for processing messages received from ODL's restconf interface.
 * TODO - Remove some of these state maps and move some of this into the PCMMService
 */
@ThreadSafe
public class PacketcableProvider implements BindingAwareProvider, AutoCloseable {

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

    // Data change listeners/registrations
    private final CcapsDataChangeListener ccapsDataChangeListener = new CcapsDataChangeListener();
    private final QosDataChangeListener qosDataChangeListener = new QosDataChangeListener();

    private ListenerRegistration<DataChangeListener> ccapsDataChangeListenerRegistration;
    private ListenerRegistration<DataChangeListener> qosDataChangeListenerRegistration;

    /**
     * Constructor
     */
    public PacketcableProvider() {
        logger.info("Starting provider");
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        logger.info("Packetcable Session Initiated");
        logger.info("logging levels: error={}, warn={}, info={}, debug={}, trace={}", logger.isErrorEnabled(),
                logger.isWarnEnabled(), logger.isInfoEnabled(), logger.isDebugEnabled(), logger.isTraceEnabled());

        dataBroker = session.getSALService(DataBroker.class);

        mdsalUtils = new MdsalUtils(dataBroker);

        ccapsDataChangeListenerRegistration = dataBroker
                .registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, ccapsIID.child(Ccap.class),
                        ccapsDataChangeListener, DataBroker.DataChangeScope.SUBTREE);

        qosDataChangeListenerRegistration = dataBroker
                .registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, PacketcableProvider.qosIID.child(Apps.class).child(App.class),
                        qosDataChangeListener, DataBroker.DataChangeScope.SUBTREE);

        // Add empty top level elements
//        for (LogicalDatastoreType datastoreType : LogicalDatastoreType.values()) {
//            WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
//            writeTransaction.put(datastoreType, ccapsIID, new CcapsBuilder().build());
//            CheckedFuture<Void, TransactionCommitFailedException> future = writeTransaction.submit();
//            try {
//                future.checkedGet();
//            } catch (TransactionCommitFailedException e) {
//                logger.error("Failed to initialise top level ccaps in datastore {}", datastoreType, e);
//            }
//            writeTransaction = dataBroker.newWriteOnlyTransaction();
//            writeTransaction.put(datastoreType, qosIID, new QosBuilder().build());
//            future = writeTransaction.submit();
//            try {
//                future.checkedGet();
//            } catch (TransactionCommitFailedException e) {
//                logger.error("Failed to initialise top level qos in datastore {}", datastoreType, e);
//            }
//        }


    }

    /**
     * Implemented from the AutoCloseable interface.
     */
    @Override
    public void close() throws ExecutionException, InterruptedException {
        if (ccapsDataChangeListenerRegistration != null) {
            ccapsDataChangeListenerRegistration.close();
        }

        if (qosDataChangeListenerRegistration != null) {
            qosDataChangeListenerRegistration.close();
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
    private <T extends DataObject> void saveErrors(@Nonnull Map<InstanceIdentifier<T>, ValidationException> errorMap,
            @Nonnull Map<InstanceIdentifier<T>, T> dataMap) {

        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();


        for (InstanceIdentifier<T> iid : errorMap.keySet()) {

            final ValidationException exception = errorMap.get(iid);
            final T badData = dataMap.get(iid);

            if (!badData.getImplementedInterface().isAssignableFrom(iid.getTargetType())) {
                // InstanceIdentifier<T> does not have the same type as the DataObject
                logger.error("Bad InstanceIdentifier to DataObject mapping, {} : {}", iid, badData);
                continue;
            }

            if (badData instanceof Ccap) {
                final Ccap ccap = (Ccap) badData;

                final Ccap opperationalCcap =
                        new CcapBuilder().setCcapId(ccap.getCcapId()).setError(exception.getErrorMessages()).build();


                // type match between iid and badData is done at start of loop
                @SuppressWarnings("unchecked")
                final InstanceIdentifier<Ccap> ccapIID = (InstanceIdentifier<Ccap>) iid;
                writeTransaction.put(LogicalDatastoreType.OPERATIONAL, ccapIID, opperationalCcap);
            }
            else if (badData instanceof Gate) {
                final Gate gate = (Gate) badData;

                final Gate operationalGate =
                        new GateBuilder()
                        .setGateId(gate.getGateId())
                        .setError(exception.getErrorMessages())
                        .build();

                final Gates operationalGates = new GatesBuilder()
                        .setGate(Collections.singletonList(operationalGate))
                        .build();

                final SubscriberKey subscriberKey = InstanceIdentifier.keyOf(iid.firstIdentifierOf(Subscriber.class));
                final Subscriber operationalSubscriber = new SubscriberBuilder()
                        .setSubscriberId(subscriberKey.getSubscriberId())
                        .setGates(operationalGates)
                        .build();

                final Subscribers operationalSubscribers = new SubscribersBuilder()
                        .setSubscriber(Collections.singletonList(operationalSubscriber))
                        .build();

                final InstanceIdentifier<App> appIID = iid.firstIdentifierOf(App.class);
                final AppKey appKey = InstanceIdentifier.keyOf(appIID);
                final App operationalApp = new AppBuilder()
                        .setAppId(appKey.getAppId())
                        .setSubscribers(operationalSubscribers)
                        .build();


                writeTransaction.put(LogicalDatastoreType.OPERATIONAL, appIID, operationalApp);
            }
            else {
                // If you get here a developer forgot to add a type above
                logger.error("Unexpected type requested for error saving: {}", badData);
                throw new IllegalStateException("Unsupported type for error saving");
            }

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
            executor.execute(new AppsCleaner(appIID));
        }
    }


    /**
     * Removes Apps if all App instances are removed.
     */
    private class AppsCleaner extends  AbstractCleaner<Apps> {

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
     *  {@link #shouldClean(DataObject)}. <br>
     *
     * Subclasses can react after an instance is removed by overriding {@link #postRemove(InstanceIdentifier)}
     * @param <T> The type that will be removed
     */
    private abstract class AbstractCleaner <T extends DataObject> implements Runnable {
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
                        }
                        else {
                            removeFailed(tIID);
                        }
                    }

                }
            }
            else {
                logger.error("Expected to find InstanceIdentifier<{}> but was not found: {}",
                        tClass.getSimpleName(), removedIID);
            }
        }

        /**
         * If returns true the object will be removed from the datastore
         * @param object The object that might be removed.
         * @return true if it should be removed.
         */
        abstract boolean shouldClean(final T object);

        /**
         * Called after an instance is removed.
         * @param tIID the InstanceIdentifier of the removed object
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
    private class CcapsDataChangeListener extends AbstractDataChangeListener<Ccap> {

        private final DataValidator ccapsDataValidator = new DataValidator(new CcapsValidatorProviderFactory().build());

        private final Set<InstanceIdentifier<Ccap>> updateQueue = Sets.newConcurrentHashSet();

        public CcapsDataChangeListener() {
            super(Ccap.class);
        }

        @Override
        protected void handleCreatedData(final Map<InstanceIdentifier<Ccap>, Ccap> createdCcaps) {
            if (createdCcaps.isEmpty()) {
                return;
            }

            final Map<InstanceIdentifier<Ccap>, ValidationException> errorMap =
                    ccapsDataValidator.validateOneType(createdCcaps, Validator.Extent.NODE_AND_SUBTREE);

            // validate all new objects an update operational datastore
            if (!errorMap.isEmpty()) {
                // bad data write errors to operational datastore
                saveErrors(errorMap, createdCcaps);
            }

            if (createdCcaps.size() > errorMap.size()) {
                final Map<InstanceIdentifier<Ccap>, Ccap> goodData =
                        Maps.newHashMapWithExpectedSize(createdCcaps.size() - errorMap.size());
                for (InstanceIdentifier<Ccap> iid : createdCcaps.keySet()) {
                    if (!errorMap.containsKey(iid)) {
                        goodData.put(iid, createdCcaps.get(iid));
                    }
                }
                addNewCcaps(goodData);
            }
        }

        private void addNewCcaps(final Map<InstanceIdentifier<Ccap>, Ccap> goodData) {
            for (InstanceIdentifier<Ccap> iid : goodData.keySet()) {
                final Ccap ccap = goodData.get(iid);

                // add service
                if (pcmmServiceMap.containsKey(ccap.getCcapId())) {
                    logger.error("Already monitoring CCAP - " + ccap);
                    continue;
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

        }

        @Override
        protected void handleUpdatedData(final Map<InstanceIdentifier<Ccap>, Ccap> updatedCcaps,
                final Map<InstanceIdentifier<Ccap>, Ccap> originalCcaps) {

            // TODO actually support updates

            // update operation not allowed -- restore the original config object and complain
            for (final Map.Entry<InstanceIdentifier<Ccap>, Ccap> entry : updatedCcaps.entrySet()) {
                if (!originalCcaps.containsKey(entry.getKey())) {
                    logger.error("No original data found for supposedly updated data: {}", entry.getValue());
                    continue;
                }

                // If this notification is coming from our modification ignore it.
                if (updateQueue.contains(entry.getKey())) {
                    updateQueue.remove(entry.getKey());
                    continue;
                }

                final Ccap originalCcap = originalCcaps.get(entry.getKey());
                //final Ccap updatedCcap = entry.getValue();

                // restore the original data
                updateQueue.add(entry.getKey());
                mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, entry.getKey(), originalCcap);
                logger.error("CCAP update not permitted {}", entry.getKey());
            }
        }

        @Override
        protected void handleRemovedData(final Set<InstanceIdentifier<Ccap>> removedCcapPaths,
                final Map<InstanceIdentifier<Ccap>, Ccap> originalCcaps) {

            for (InstanceIdentifier<Ccap> iid : removedCcapPaths) {
                final Ccap nukedCcap = originalCcaps.get(iid);
                removeCcapFromAllMaps(nukedCcap);

                mdsalUtils.delete(LogicalDatastoreType.OPERATIONAL, iid);

                // clean up ccaps level if it is now empty
                executor.execute(new CcapsCleaner(iid));
            }

        }
    }


    private class QosDataChangeListener extends AbstractDataChangeListener<Gate> {

        private final DataValidator qosDataValidator = new DataValidator(new QosValidatorProviderFactory().build());
        private final Set<InstanceIdentifier<Gate>> updateQueue = Sets.newConcurrentHashSet();

        public QosDataChangeListener() {
            super(Gate.class);
        }

        @Override
        protected void handleCreatedData(final Map<InstanceIdentifier<Gate>, Gate> createdData) {

            final Map<InstanceIdentifier<Gate>, ValidationException> errorMap =
                    qosDataValidator.validateOneType(createdData, Validator.Extent.NODE_AND_SUBTREE);

            // validate all new objects an update operational datastore
            if (!errorMap.isEmpty()) {
                // bad data write errors to operational datastore
                saveErrors(errorMap, createdData);
            }

            if (createdData.size() > errorMap.size()) {
                final Map<InstanceIdentifier<Gate>, Gate> goodData =
                        Maps.newHashMapWithExpectedSize(createdData.size() - errorMap.size());
                for (InstanceIdentifier<Gate> iid : createdData.keySet()) {
                    if (!errorMap.containsKey(iid)) {
                        goodData.put(iid, createdData.get(iid));
                    }
                }
                addNewGates(goodData);
            }

        }

        private void addNewGates(final Map<InstanceIdentifier<Gate>, Gate> createdGates) {

            for (InstanceIdentifier<Gate> gateIID : createdGates.keySet()) {
                final Gate newGate = createdGates.get(gateIID);

                final String newGatePathStr = makeGatePathString(gateIID);

                final InstanceIdentifier<Subscriber> subscriberIID = gateIID.firstIdentifierOf(Subscriber.class);
                final SubscriberKey subscriberKey = InstanceIdentifier.keyOf(subscriberIID);
                final InetAddress subscriberAddr = getInetAddress(subscriberKey.getSubscriberId());
                if (subscriberAddr == null) {
                    final String msg = String.format("subscriberId must be a valid ipaddress: %s",
                            subscriberKey.getSubscriberId());
                    logger.error(msg);
                    saveGateError(gateIID, newGatePathStr, msg);
                    continue;
                }

                final Ccap ccap = findCcapForSubscriberId(subscriberAddr);
                if (ccap == null) {
                    final String msg = String.format("Unable to find Ccap for subscriber %s: @ %s",
                            subscriberKey.getSubscriberId(), newGatePathStr);
                    logger.error(msg);
                    saveGateError(gateIID, newGatePathStr, msg);
                    continue;
                }

                final ServiceClassName scn = newGate.getTrafficProfile().getServiceClassName();
                final ServiceFlowDirection scnDirection = findScnOnCcap(scn, ccap);
                if (scnDirection == null) {
                    final String msg = String.format("SCN %s not found on CCAP %s for %s",
                            scn, ccap.getCcapId(), newGatePathStr);
                    logger.error(msg);
                    saveGateError(gateIID, newGatePathStr, msg);
                    continue;
                }

                final PCMMService pcmmService = pcmmServiceMap.get(ccap.getCcapId());
                if (pcmmService == null) {
                    final String msg = String.format("Unable to locate PCMM Service for CCAP: %s ; with subscriber: %s",
                            ccap, subscriberKey.getSubscriberId());
                    logger.error(msg);
                    saveGateError(gateIID, newGatePathStr, msg);
                    continue;
                }

                PCMMService.GateSetStatus status = pcmmService.sendGateSet(newGatePathStr, subscriberAddr, newGate, scnDirection);
                gateMap.put(newGatePathStr, newGate);
                gateCcapMap.put(newGatePathStr, ccap.getCcapId());

                final GateBuilder gateBuilder = new GateBuilder();
                gateBuilder.setGateId(newGate.getGateId())
                        .setGatePath(newGatePathStr)
                        .setCcapId(ccap.getCcapId())
                        .setCopsGateId(status.getCopsGateId())
                        .setCopsState(status.didSucceed() ? "success" : "failure");
                if (!status.didSucceed()) {
                    gateBuilder.setError(Collections.singletonList(status.getMessage()));
                }

                Gate operationalGate = gateBuilder.build();

                mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIID, operationalGate);

            }

        }

        private void saveGateError(@Nonnull final InstanceIdentifier<Gate> gateIID, @Nonnull final String gatePathStr,
                @Nonnull final String error) {
            checkNotNull(gateIID);
            checkNotNull(error);

            final GateBuilder gateBuilder = new GateBuilder();
            gateBuilder.setGateId(InstanceIdentifier.keyOf(gateIID).getGateId())
                    .setGatePath(gatePathStr)
                    .setCopsGateId("")
                    .setCopsState("N/A");

                gateBuilder.setError(Collections.singletonList(error));

            Gate operationalGate = gateBuilder.build();

            mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, gateIID, operationalGate);
        }

        @Override
        protected void handleUpdatedData(final Map<InstanceIdentifier<Gate>, Gate> updatedData,
                final Map<InstanceIdentifier<Gate>, Gate> originalData) {
            // TODO actually support updates

            // update operation not allowed -- restore the original config object and complain
            for (final Map.Entry<InstanceIdentifier<Gate>, Gate> entry : updatedData.entrySet()) {
                if (!originalData.containsKey(entry.getKey())) {
                    logger.error("No original data found for supposedly updated data: {}", entry.getValue());
                    continue;
                }

                // If this notification is coming from our modification ignore it.
                if (updateQueue.contains(entry.getKey())) {
                    updateQueue.remove(entry.getKey());
                    continue;
                }

                final Gate originalGate = originalData.get(entry.getKey());

                // restores the original data
                updateQueue.add(entry.getKey());
                mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, entry.getKey(), originalGate);
                logger.error("Update not permitted {}", entry.getKey());

            }
        }



        @Override
        protected void handleRemovedData(final Set<InstanceIdentifier<Gate>> removedPaths,
                final Map<InstanceIdentifier<Gate>, Gate> originalData) {

            for (final InstanceIdentifier<Gate> removedGateIID : removedPaths) {

                mdsalUtils.delete(LogicalDatastoreType.OPERATIONAL, removedGateIID);
                //TODO check if this was the last gate for this app/subscriber and if so delete them

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
                            logger.info("onDataChanged(): removed QoS gate {} for {}/{}/{}: ", gateId, ccapId, gatePathStr,
                                    thisGate);
                        } else {
                            logger.warn(
                                    "Unable to send to locate PCMMService to send gate delete message with CCAP - " + thisCcap);
                        }
                    }


            }

        }

        private String makeGatePathString(InstanceIdentifier<Gate> iid) {
            final InstanceIdentifier<App> appIID = iid.firstIdentifierOf(App.class);
            final AppKey appKey = InstanceIdentifier.keyOf(appIID);

            final InstanceIdentifier<Subscriber> subscriberIID = iid.firstIdentifierOf(Subscriber.class);
            final SubscriberKey subscriberKey = InstanceIdentifier.keyOf(subscriberIID);

            final GateKey gateKey = InstanceIdentifier.keyOf(iid);

            return appKey.getAppId()
                    + "/" + subscriberKey.getSubscriberId()
                    + "/" + gateKey.getGateId();
        }
    }

}

package org.opendaylight.controller.packetcable.provider;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.Ccap;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.Qos;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceClassName;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ServiceFlowDirection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.CcapsKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.attributes.Connection;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.Apps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.AppsKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.Subs;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.SubsKey;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.GatesKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.pcmm.rcd.IPCMMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Called by ODL framework to start this bundle.
 *
 * This class is responsible for processing messages received from ODL's restconf interface.
 * TODO - Remove some of these state maps and move some of this into the PCMMService
 */
@ThreadSafe
public class PacketcableProvider implements BindingAwareProvider, DataChangeListener, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(PacketcableProvider.class);

    // keys to the /restconf/config/packetcable:ccap and /restconf/config/packetcable:qos config datastore
    public static final InstanceIdentifier<Ccap> ccapIID = InstanceIdentifier.builder(Ccap.class).build();
    public static final InstanceIdentifier<Qos> qosIID = InstanceIdentifier.builder(Qos.class).build();

    /**
     * The ODL object used to broker messages throughout the framework
     */
    private DataBroker dataBroker;

    private MdsalUtils mdsalUtils;

    private ListenerRegistration<DataChangeListener> ccapDataChangeListenerRegistration;
    private ListenerRegistration<DataChangeListener> qosDataChangeListenerRegistration;

    // TODO - Revisit these maps and remove the ones no longer necessary
    private final Map<String, Ccaps> ccapMap = new ConcurrentHashMap<>();
    private final Map<String, Gates> gateMap = new ConcurrentHashMap<>();
    private final Map<String, String> gateCcapMap = new ConcurrentHashMap<>();
    private final Map<Subnet, Ccaps> subscriberSubnetsMap = new ConcurrentHashMap<>();
    private final Map<ServiceClassName, List<Ccaps>> downstreamScnMap = new ConcurrentHashMap<>();
    private final Map<ServiceClassName, List<Ccaps>> upstreamScnMap = new ConcurrentHashMap<>();

    /**
     * Holds a PCMMService object for each CCAP being managed.
     */
    private final Map<String, PCMMService> pcmmServiceMap = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    public PacketcableProvider() {
        logger.info("Starting provider");
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        logger.info("Packetcable Session Initiated");

        dataBroker =  session.getSALService(DataBroker.class);

        mdsalUtils = new MdsalUtils(dataBroker);

        ccapDataChangeListenerRegistration =
                dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                        PacketcableProvider.ccapIID, this, DataBroker.DataChangeScope.SUBTREE );

        qosDataChangeListenerRegistration =
                dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                        PacketcableProvider.qosIID, this, DataBroker.DataChangeScope.SUBTREE );
    }
    /**
     * Implemented from the AutoCloseable interface.
     */
    @Override
    public void close() throws ExecutionException, InterruptedException {
        if (ccapDataChangeListenerRegistration != null) {
            ccapDataChangeListenerRegistration.close();
        }

        if (qosDataChangeListenerRegistration != null) {
            qosDataChangeListenerRegistration.close();
        }
    }

    public InetAddress getInetAddress(final String subId){
        try {
            return InetAddress.getByName(subId);
        } catch (UnknownHostException e) {
            logger.error("getInetAddress: {} FAILED: {}", subId, e.getMessage());
            return null;
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

    private void updateCcapMaps(final Ccaps ccap) {
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
                final List<Ccaps> ccapList = new ArrayList<>();
                ccapList.add(ccap);
                upstreamScnMap.put(scn, ccapList);
            }
        }
        // ccap to downstream SCN map
        for (final ServiceClassName scn : ccap.getDownstreamScns()) {
            if (downstreamScnMap.containsKey(scn)) {
                downstreamScnMap.get(scn).add(ccap);
            } else {
                final List<Ccaps> ccapList = new ArrayList<>();
                ccapList.add(ccap);
                downstreamScnMap.put(scn, ccapList);
            }
        }
    }

    private void removeCcapFromAllMaps(final Ccaps ccap) {
        // remove the ccap from all maps
        // subscriberSubnets map
        for (final Map.Entry<Subnet, Ccaps> entry : subscriberSubnetsMap.entrySet()) {
            if (entry.getValue() == ccap) {
                subscriberSubnetsMap.remove(entry.getKey());
            }
        }
        // ccap to upstream SCN map
        for (final Map.Entry<ServiceClassName, List<Ccaps>> entry : upstreamScnMap.entrySet()) {
            final List<Ccaps> ccapList = entry.getValue();
            ccapList.remove(ccap);
            if (ccapList.isEmpty()) {
                upstreamScnMap.remove(entry.getKey());
            }
        }
        // ccap to downstream SCN map
        for (final Map.Entry<ServiceClassName, List<Ccaps>> entry : downstreamScnMap.entrySet()) {
            final List<Ccaps> ccapList = entry.getValue();
            ccapList.remove(ccap);
            if (ccapList.isEmpty()) {
                downstreamScnMap.remove(entry.getKey());
            }
        }

        final PCMMService service = pcmmServiceMap.remove(ccap.getCcapId());
        if (service != null) service.disconect();
    }

    private Ccaps findCcapForSubscriberId(final InetAddress inetAddr) {
        Ccaps matchedCcap = null;
        int longestPrefixLen = -1;
        for (final Map.Entry<Subnet, Ccaps> entry : subscriberSubnetsMap.entrySet()) {
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

    private ServiceFlowDirection findScnOnCcap(final ServiceClassName scn, final Ccaps ccap) {
        if (upstreamScnMap.containsKey(scn)) {
            final List<Ccaps> ccapList = upstreamScnMap.get(scn);
            if (ccapList.contains(ccap)) {
                return ServiceFlowDirection.Us;
            }
        } else if (downstreamScnMap.containsKey(scn)) {
            final List<Ccaps> ccapList = downstreamScnMap.get(scn);
            if (ccapList.contains(ccap)) {
                return ServiceFlowDirection.Ds;
            }
        }
        return null;
    }

    /**
     * Implemented from the DataChangeListener interface.
     */

    private class InstanceData {
        // CCAP Identity
        public final Map<InstanceIdentifier<Ccaps>, Ccaps> ccapIidMap = new HashMap<>();
        // Gate Identity
        public String subId;
        public final Map<String, String> gatePathMap = new HashMap<>();
        public String gatePath;
        public final Map<InstanceIdentifier<Gates>, Gates> gateIidMap = new HashMap<>();
        // remove path for either CCAP or Gates
        public final Set<String> removePathList = new HashSet<>();

        public final Set<InstanceIdentifier<?>> reqCcapIds = new HashSet<>();

        public InstanceData(final Map<InstanceIdentifier<?>, DataObject> thisData) {
            // only used to parse createdData or updatedData
            getCcaps(thisData);
            if (ccapIidMap.isEmpty()) {
                getGates(thisData);
                if (! gateIidMap.isEmpty()){
                    gatePath = gatePathMap.get("appId") + "/" + gatePathMap.get("subId");
                }
            }
        }

        public InstanceData(final Set<InstanceIdentifier<?>> thisData) {
            // only used to parse the removedData paths
            for (final InstanceIdentifier<?> removeThis : thisData) {
                getGatePathMap(removeThis);
                if (gatePathMap.containsKey("ccapId")) {
                    gatePath = gatePathMap.get("ccapId");
                    removePathList.add(gatePath);
                } else if (gatePathMap.containsKey("gateId")) {
                    gatePath = gatePathMap.get("appId") + "/" + gatePathMap.get("subId") + "/" + gatePathMap.get("gateId");
                    removePathList.add(gatePath);
                }
            }
        }
        private void getGatePathMap(final InstanceIdentifier<?> thisInstance) {
            logger.info("onDataChanged().getGatePathMap(): " + thisInstance);
            try {
                final InstanceIdentifier<Ccaps> ccapInstance = thisInstance.firstIdentifierOf(Ccaps.class);
                if (ccapInstance != null) {
                    final CcapsKey ccapKey = InstanceIdentifier.keyOf(ccapInstance);
                    if (ccapKey != null) {
                        gatePathMap.put("ccapId", ccapKey.getCcapId());
                    }
                } else {
                    // get the gate path keys from the InstanceIdentifier Map key set if they are there
                    final InstanceIdentifier<Apps> appsInstance = thisInstance.firstIdentifierOf(Apps.class);
                    if (appsInstance != null) {
                        final AppsKey appKey = InstanceIdentifier.keyOf(appsInstance);
                        if (appKey != null) {
                            gatePathMap.put("appId", appKey.getAppId());
                        }
                    }
                    final InstanceIdentifier<Subs> subsInstance = thisInstance.firstIdentifierOf(Subs.class);
                    if (subsInstance != null) {
                        final SubsKey subKey = InstanceIdentifier.keyOf(subsInstance);
                        if (subKey != null) {
                            subId = subKey.getSubId();
                            gatePathMap.put("subId", subId);
                        }
                    }
                    final InstanceIdentifier<Gates> gatesInstance = thisInstance.firstIdentifierOf(Gates.class);
                    if (gatesInstance != null) {
                        final GatesKey gateKey = InstanceIdentifier.keyOf(gatesInstance);
                        if (gateKey != null) {
                            gatePathMap.put("gateId", gateKey.getGateId());
                        }
                    }
                }
            } catch (ClassCastException err) {
                logger.warn("Unexpected exception", err);
            }
        }

        private void getCcaps(final Map<InstanceIdentifier<?>, DataObject> thisData) {
            logger.info("onDataChanged().getCcaps(): " + thisData);
            for (final Map.Entry<InstanceIdentifier<?>, DataObject> entry : thisData.entrySet()) {

                if (entry.getKey().getTargetType().equals(Ccaps.class)) {
                    Ccaps ccaps = ((Ccaps) entry.getValue());
                    InstanceIdentifier<Ccaps> ccapsIid = InstanceIdentifier.builder(Ccap.class).child(Ccaps.class, new CcapsKey(ccaps.getCcapId())).build();
                    ccapIidMap.put(ccapsIid, ccaps);
                }

                if (entry.getKey().getTargetType().equals(Connection.class) ||
                        entry.getKey().getTargetType().equals(Ccaps.class)) {
                    reqCcapIds.add(entry.getKey());
                }
            }
        }

        private void getGates(final Map<InstanceIdentifier<?>, DataObject> thisData) {
            logger.info("onDataChanged().getGates(): " + thisData);
            for (final Map.Entry<InstanceIdentifier<?>, DataObject> entry : thisData.entrySet()) {
                if (entry.getValue() instanceof Gates) {
                    final Gates gate = (Gates)entry.getValue();

                    // TODO FIXME - Potential ClassCastException thrown here!!!
                    final InstanceIdentifier<Gates> gateIID = (InstanceIdentifier<Gates>)entry.getKey();
                    getGatePathMap(gateIID);
                    if (!gateIidMap.containsKey(gateIID)){
                        gateIidMap.put(gateIID, gate);
                    }
                }
                // TODO reconciliate gates
//                if (entry.getValue() instanceof Qos) {
//                    final Qos qos = (Qos) entry.getValue();
//                    if (qos.getApps() != null) {
//                        for (Apps apps : qos.getApps()) {
//                            if (apps.getSubs() != null) {
//                                for (Subs subs : apps.getSubs()) {
//                                    if (subs.getGates() != null) {
//                                        for (Gates gates : subs.getGates()) {
//                                            final InstanceIdentifier<Gates> gateIID = (InstanceIdentifier<Gates>)entry.getKey();
//                                            getGatePathMap(gateIID);
//                                            if (!gateIidMap.containsKey(gateIID)){
//                                                gateIidMap.put(gateIID, gates);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        logger.info("onDataChanged");
        // Determine what change action took place by looking at the change object's InstanceIdentifier sets
        // and validate all instance data
        if (!change.getCreatedData().isEmpty()) {
            if (!new ValidateInstanceData(mdsalUtils, change.getCreatedData()).validateYang()) {
                // leave now -- a bad yang object has been detected and a response object has been inserted
                return;
            }
            onCreate(new InstanceData(change.getCreatedData()));
        } else if (!change.getRemovedPaths().isEmpty()) {
            onRemove(new InstanceData(change.getRemovedPaths()));
        } else if (!change.getUpdatedData().isEmpty()) {
            onUpdate(new InstanceData(change.getUpdatedData()));
        } else {
            // we should not be here -- complain bitterly and return
            logger.error("onDataChanged(): Unknown change action: " + change);
        }
    }

    private void onCreate(final InstanceData thisData) {
        logger.info("onCreate(): " + thisData);

        // get the CCAP parameters
        String message;
        if (! thisData.reqCcapIds.isEmpty()) {
            for (Map.Entry<InstanceIdentifier<Ccaps>, Ccaps> entry : thisData.ccapIidMap.entrySet()) {
                final Ccaps thisCcap = entry.getValue();
                // get the CCAP node identity from the Instance Data
                final String ccapId = thisCcap.getCcapId();

                if (pcmmServiceMap.get(thisCcap.getCcapId()) == null) {
                    final PCMMService pcmmService = new PCMMService(IPCMMClient.CLIENT_TYPE, thisCcap);
                    // TODO - may want to use the AMID but for the client type but probably not???
/*
                            final PCMMService pcmmService = new PCMMService(
                                    thisCcap.getAmId().getAmType().shortValue(), thisCcap);
*/
                    message = pcmmService.addCcap();
                    if (message.contains("200 OK")) {
                        pcmmServiceMap.put(thisCcap.getCcapId(), pcmmService);
                        ccapMap.put(ccapId, thisCcap);
                        updateCcapMaps(thisCcap);
                        logger.info("Created CCAP: {}/{} : {}", thisData.gatePath, thisCcap, message);
                        logger.info("Created CCAP: {} : {}", thisData.gatePath, message);
                    } else {
                        logger.error("Create CCAP Failed: {} : {}", thisData.gatePath, message);
                        for (final InstanceIdentifier<?> instId : thisData.reqCcapIds) {
                            mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, instId);
                        }
                        ccapMap.remove(ccapId);
                    }
                } else {
                    logger.error("Already monitoring CCAP - " + thisCcap);
                    break;
                }
            }
        } else {
            // get the PCMM gate parameters from the ccapId/appId/subId/gateId path in the Maps entry (if new gate)
            for (final Map.Entry<InstanceIdentifier<Gates>, Gates> entry : thisData.gateIidMap.entrySet()) {
                message = null;
                final Gates gate = entry.getValue();
                final String gateId = gate.getGateId();
                final String gatePathStr = thisData.gatePath + "/" + gateId ;
                final InetAddress subId = getInetAddress(thisData.subId);
                if (subId != null) {
                    final Ccaps thisCcap = findCcapForSubscriberId(subId);
                    if (thisCcap != null) {
                        final String ccapId = thisCcap.getCcapId();
                        // verify SCN exists on CCAP and force gateSpec.Direction to align with SCN direction
                        final ServiceClassName scn = gate.getTrafficProfile().getServiceClassName();
                        if (scn != null) {
                            final ServiceFlowDirection scnDir = findScnOnCcap(scn, thisCcap);
                            if (scnDir != null) {
                                if (pcmmServiceMap.get(thisCcap.getCcapId()) != null) {
                                    message = pcmmServiceMap.get(thisCcap.getCcapId()).sendGateSet(gatePathStr, subId, gate, scnDir);
                                    gateMap.put(gatePathStr, gate);
                                    gateCcapMap.put(gatePathStr, thisCcap.getCcapId());

                                    if (message.contains("200 OK")) {
                                        logger.info("Created QoS gate {} for {}/{}/{} - {}",
                                                gateId, ccapId, gatePathStr, gate, message);
                                        logger.info("Created QoS gate {} for {}/{} - {}",
                                                gateId, ccapId, gatePathStr, message);
                                    } else {
                                        logger.info("Unable to create QoS gate {} for {}/{}/{} - {}",
                                                gateId, ccapId, gatePathStr, gate, message);
                                        logger.error("Unable to create QoS gate {} for {}/{} - {}",
                                                gateId, ccapId, gatePathStr, message);
                                    }
                                } else {
                                    logger.error("Unable to locate PCMM Service for CCAP - " + thisCcap);
                                    break;
                                }
                            } else {
                                logger.error("PCMMService: sendGateSet(): SCN {} not found on CCAP {} for {}/{}",
                                        scn.getValue(), thisCcap, gatePathStr, gate);
                                message = String.format("404 Not Found - SCN %s not found on CCAP %s for %s",
                                        scn.getValue(), thisCcap.getCcapId(), gatePathStr);
                            }
                        }
                    } else {
                        final String subIdStr = thisData.subId;
                        message = String.format("404 Not Found - no CCAP found for subscriber %s in %s",
                                subIdStr, gatePathStr);
                        logger.info("Create QoS gate {} FAILED: no CCAP found for subscriber {}: @ {}/{}",
                                gateId, subIdStr, gatePathStr, gate);
                        logger.error("Create QoS gate {} FAILED: no CCAP found for subscriber {}: @ {}",
                                gateId, subIdStr, gatePathStr);
                    }
                } else {
                    final String subIdStr = thisData.subId;
                    message = String.format("400 Bad Request - subId must be a valid IP address for subscriber %s in %s",
                            subIdStr, gatePathStr);
                    logger.info("Create QoS gate {} FAILED: subId must be a valid IP address for subscriber {}: @ {}/{}",
                            gateId, subIdStr, gatePathStr, gate);
                    logger.error("Create QoS gate {} FAILED: subId must be a valid IP address for subscriber {}: @ {}",
                            gateId, subIdStr, gatePathStr);
                }
                if (!message.contains("200 OK")) {
                    mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, entry.getKey());
                }
            }
        }
    }

    private void onRemove(final InstanceData thisData) {
        logger.info("onRemove(): " + thisData);
        for (final String gatePathStr: thisData.removePathList) {
            if (gateMap.containsKey(gatePathStr)) {
                final Gates thisGate = gateMap.remove(gatePathStr);
                final String gateId = thisGate.getGateId();
                final String ccapId = gateCcapMap.remove(gatePathStr);
                final Ccaps thisCcap = ccapMap.get(ccapId);
                final PCMMService service = pcmmServiceMap.get(thisCcap.getCcapId());
                if (service != null) {
                    service.sendGateDelete(gatePathStr);
                    logger.info("onDataChanged(): removed QoS gate {} for {}/{}/{}: ", gateId, ccapId, gatePathStr, thisGate);
                    logger.info("onDataChanged(): removed QoS gate {} for {}/{}: ", gateId, ccapId, gatePathStr);
                } else
                    logger.warn("Unable to send to locate PCMMService to send gate delete message with CCAP - "
                            + thisCcap);
            }
        }
        for (final String ccapIdStr: thisData.removePathList) {
            if (ccapMap.containsKey(ccapIdStr)) {
                final Ccaps thisCcap = ccapMap.remove(ccapIdStr);
                removeCcapFromAllMaps(thisCcap);
            }
        }
    }

    private void onUpdate(final InstanceData oldData) {
        logger.info("onUpdate(): " + oldData);
        // update operation not allowed -- restore the original config object and complain
        if (! oldData.ccapIidMap.isEmpty()) {
            for (final Map.Entry<InstanceIdentifier<Ccaps>, Ccaps> entry : oldData.ccapIidMap.entrySet()) {
                final Ccaps ccap = entry.getValue();
                final String ccapId = ccap.getCcapId();
                // restores the original data - although I don't think this is what is done here! I think the update data is put into the DS/config
                mdsalUtils.merge(LogicalDatastoreType.CONFIGURATION, entry.getKey(), ccap);
                logger.error("onDataChanged(): CCAP update not permitted {}/{}", ccapId, ccap);
            }
        } else {
            for (final Map.Entry<InstanceIdentifier<Gates>, Gates> entry : oldData.gateIidMap.entrySet()) {
                final Gates gate = entry.getValue();
                final String gatePathStr = oldData.gatePath + "/" + gate.getGateId() ;
             // restores the original data - although I don't think this is what is done here! I think the update data is put into the DS/config
                mdsalUtils.merge(LogicalDatastoreType.CONFIGURATION, entry.getKey(), gate);
                logger.error("onDataChanged(): QoS Gate update not permitted: {}/{}", gatePathStr, gate);
            }
        }
    }
}

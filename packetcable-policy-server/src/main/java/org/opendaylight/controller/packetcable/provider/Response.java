/**
 * The Response object inserts a "response" message object into the given CCAP or Gate object in the
 * config data store.
 *
 * N.B. Updates to the config datastore must be run in a separate thread from the onDataChange() notification.
 * Therefore, the Response object must always be invoked via executor.execute(resonse) after it is
 * configured during new Response(dataBroker, ccapIID, ccapbase, message) for example.
 *
 * Also note well that when a CCAP or Gate object is updated with this "response" message, it will trigger
 * another recursive onDataChange() notification seen in the change.getUpdatedData() -- this update must be ignored.
 */
package org.opendaylight.controller.packetcable.provider;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.Ccaps;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.ccap.CcapsBuilder;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.Gates;
import org.opendaylight.yang.gen.v1.urn.packetcable.rev150327.pcmm.qos.gates.apps.subs.GatesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Response implements Runnable {

	private Logger logger = LoggerFactory.getLogger(Response.class);
	private DataBroker dataBroker;
	private String message = null;
	private InstanceIdentifier<Ccaps> ccapIID = null;
	private Ccaps ccapBase = null;
	private InstanceIdentifier<Gates> gateIID = null;
	private Gates gateBase = null;

	public Response(DataBroker dataBroker, InstanceIdentifier<Ccaps> ccapIID, Ccaps ccapBase, String message) {
		this.dataBroker = dataBroker;
		this.ccapIID = ccapIID;
		this.ccapBase = ccapBase;
		this.message = message;
	}
	public Response(DataBroker dataBroker, InstanceIdentifier<Gates> gateIID, Gates gateBase, String message) {
		this.dataBroker = dataBroker;
		this.gateIID = gateIID;
		this.gateBase = gateBase;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void addMessage(String message) {
		this.message += message;
	}
	public InstanceIdentifier<Ccaps> getCcapIID() {
		return ccapIID;
	}
	public void setCcapIID(InstanceIdentifier<Ccaps> ccapIID) {
		this.ccapIID = ccapIID;
	}
	public Ccaps getCcapBase() {
		return ccapBase;
	}
	public void setCcapBase(Ccaps ccapBase) {
		this.ccapBase = ccapBase;
	}
	public InstanceIdentifier<Gates> getGateIID() {
		return gateIID;
	}
	public void setGateIID(InstanceIdentifier<Gates> gateIID) {
		this.gateIID = gateIID;
	}
	public Gates getGateBase() {
		return gateBase;
	}
	public void setGateBase(Gates gateBase) {
		this.gateBase = gateBase;
	}

	@SuppressWarnings("deprecation")
	public void setResponse(InstanceIdentifier<Ccaps> ccapIID, Ccaps ccapBase, String message) {
		CcapsBuilder ccapBuilder = new CcapsBuilder(ccapBase);
		ccapBuilder.setResponse(message);
		Ccaps ccap = ccapBuilder.build();
        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
        writeTx.merge(LogicalDatastoreType.CONFIGURATION, ccapIID, ccap, true);
        writeTx.commit();
        logger.debug("Response.setResponse(ccap) complete {} {} {}", message, ccap, ccapIID);
	}
	@SuppressWarnings("deprecation")
	public void setResponse(InstanceIdentifier<Gates> gateIID, Gates gateBase, String message) {
		GatesBuilder gateBuilder = new GatesBuilder(gateBase);
		gateBuilder.setResponse(message);
		Gates gate = gateBuilder.build();
        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
        writeTx.merge(LogicalDatastoreType.CONFIGURATION, gateIID, gate, true);
        writeTx.commit();
        logger.debug("Response.setResponse(gate) complete: {} {} {}", message, gate, gateIID);
	}

	@Override
	public void run() {
		if (ccapIID != null) {
			setResponse(ccapIID, ccapBase, message);
		} else if (gateIID != null) {
			setResponse(gateIID, gateBase, message);
		} else {
			logger.error("Unknown Response: must be for a CCAP or Gate instance");
		}
	}
}


package org.pcmm.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pcmm.objects.PCMMIDHolder;
import org.pcmm.objects.PCMMResourceSet;
import org.pcmm.objects.PCMMResourcesMapper;

public class PCMMResourceSetTest {

	
	@Test
	public void testGetMappedResources() {

	int flowID=100;
	short trId=(short)123;
	//typical use of PCMMresourceSet to add a new mapping
	PCMMResourceSet.getInstance().mapResources(	/* flow ID */flowID, 
						new PCMMResourcesMapper<Short, PCMMIDHolder>(/* transactionID */(short)trId,
										/* PCMMIDHolder */new PCMMIDHolder(/* flowID */flowID, /* gateID */0, /* transactionID */trId)));

	// if we want to retrieve or update mapped data  
	PCMMIDHolder holder = (PCMMIDHolder) PCMMResourceSet.getInstance().getMappedResources(/* flow ID */flowID).getValue();
	short transID=(short) PCMMResourceSet.getInstance().getMappedResources(/* flow ID */flowID).getKey();
	
	assertTrue(holder.getFlowID()==flowID);
	assertTrue(holder.getTransactionID()==trId);
	assertTrue(transID==trId);
	//update gate ID
	holder.setGateID(/*gate ID*/ 1234568);

	assertTrue(((PCMMIDHolder) PCMMResourceSet.getInstance().getMappedResources(/* flow ID */flowID).getValue()).getGateID()==1234568);
				
	

	}

}

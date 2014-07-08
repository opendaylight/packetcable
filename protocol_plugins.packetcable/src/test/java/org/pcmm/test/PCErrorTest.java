package org.pcmm.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pcmm.gates.IPCMMError;
import org.pcmm.gates.impl.PCMMError;

public class PCErrorTest {

	IPCMMError error;

	@Before
	public void init() {
		error = new PCMMError();
		error.setErrorCode((short) 1);
	}

	@Test
	public void testGetDescription() {
		for (IPCMMError.Description d : IPCMMError.Description.values()) {
			error.setErrorCode(d.getCode());
			Assert.assertNotNull(error.getDescription());
			System.out.println(error.getDescription());
		}

	}

}

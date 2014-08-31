package org.pcmm.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.pcmm.gates.impl.BestEffortService;

public class BestEffortServiceTest {

    private BestEffortService be;

    @Before
    public void init() {
        be = new BestEffortService((byte) 7);
        be.getAuthorizedEnvelop().setMinimumReservedTrafficRate(192);
        be.getCommittedEnvelop().setRequiredAttributeMask(938);
        be.getReservedEnvelop().setTrafficPriority((byte) 5);
    }

    @Test
    public void testGetAsBinaryArray() {
        assertTrue(be.getAsBinaryArray().length == 116);
    }

    @Test
    public void testBestEffortServiceByteArray() {
        assertTrue(new BestEffortService(be.getAsBinaryArray())
                   .getAuthorizedEnvelop().getMinimumReservedTrafficRate() == 192);
        assertTrue(new BestEffortService(be.getAsBinaryArray())
                   .getReservedEnvelop().getTrafficPriority() == 5);
        assertTrue(new BestEffortService(be.getAsBinaryArray())
                   .getCommittedEnvelop().getRequiredAttributeMask() == 938);

    }

    @Test
    public void testGetEnvelop() {
        assertTrue(be.getEnvelop() == 7);
    }

}

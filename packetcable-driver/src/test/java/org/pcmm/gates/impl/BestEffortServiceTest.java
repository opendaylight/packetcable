package org.pcmm.gates.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the data holder class BestEffortService to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class BestEffortServiceTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullAllEnvelopes() {
        new BestEffortService(null, null, null);
    }

    @Test
    public void requiredEnvelopeOnly() {
        final BEEnvelop auth = new BEEnvelop((byte)0, 1, 2, 3, 4, (short)5, (short)6, 7, 8, 9, 0, 1, 2, 3);
        final BestEffortService service = new BestEffortService(auth, null, null);
        Assert.assertNotNull(service.getAuthorizedEnvelop());
        Assert.assertNull(service.getReservedEnvelop());
        Assert.assertNull(service.getCommittedEnvelop());
        // TODO - add more validation here
    }

    @Test
    public void authAndReservedEnvelopes() {
        final BEEnvelop auth = new BEEnvelop((byte)0, 1, 2, 3, 4, (short)5, (short)6, 7, 8, 9, 0, 1, 2, 3);
        final BEEnvelop resv = new BEEnvelop((byte)10, 11, 12, 13, 14, (short)15, (short)16, 17, 18, 19, 10, 11, 12, 13);
        final BestEffortService service = new BestEffortService(auth, resv, null);
        Assert.assertNotNull(service.getAuthorizedEnvelop());
        Assert.assertNotNull(service.getReservedEnvelop());
        Assert.assertNull(service.getCommittedEnvelop());
        // TODO - add more validation here
    }

    @Test
    public void allEnvelopes() {
        final BEEnvelop auth = new BEEnvelop((byte)0, 1, 2, 3, 4, (short)5, (short)6, 7, 8, 9, 0, 1, 2, 3);
        final BEEnvelop resv = new BEEnvelop((byte)10, 11, 12, 13, 14, (short)15, (short)16, 17, 18, 19, 10, 11, 12, 13);
        final BEEnvelop cmmt = new BEEnvelop((byte)20, 21, 22, 23, 24, (short)25, (short)26, 27, 28, 29, 20, 21, 22, 23);
        final BestEffortService service = new BestEffortService(auth, resv, cmmt);
        Assert.assertNotNull(service.getAuthorizedEnvelop());
        Assert.assertNotNull(service.getReservedEnvelop());
        Assert.assertNotNull(service.getCommittedEnvelop());
        // TODO - add more validation here
    }

    @Test
    public void byteParsingAuth() {
        final BEEnvelop auth = new BEEnvelop((byte)0, 1, 2, 3, 4, (short)5, (short)6, 7, 8, 9, 0, 1, 2, 3);
        final BestEffortService service = new BestEffortService(auth, null, null);
        final BestEffortService parsed = BestEffortService.parse(service.getBytes());
        Assert.assertEquals(service, parsed);
    }

    @Test
    public void byteParsingAuthReserved() {
        final BEEnvelop auth = new BEEnvelop((byte)0, 1, 2, 3, 4, (short)5, (short)6, 7, 8, 9, 0, 1, 2, 3);
        final BEEnvelop resv = new BEEnvelop((byte)10, 11, 12, 13, 14, (short)15, (short)16, 17, 18, 19, 10, 11, 12, 13);
        final BestEffortService service = new BestEffortService(auth, resv, null);
        final BestEffortService parsed = BestEffortService.parse(service.getBytes());
        Assert.assertEquals(service, parsed);
    }

    @Test
    public void byteParsingAll() {
        final BEEnvelop auth = new BEEnvelop((byte)0, 1, 2, 3, 4, (short)5, (short)6, 7, 8, 9, 0, 1, 2, 3);
        final BEEnvelop resv = new BEEnvelop((byte)10, 11, 12, 13, 14, (short)15, (short)16, 17, 18, 19, 10, 11, 12, 13);
        final BEEnvelop cmmt = new BEEnvelop((byte)20, 21, 22, 23, 24, (short)25, (short)26, 27, 28, 29, 20, 21, 22, 23);
        final BestEffortService service = new BestEffortService(auth, resv, cmmt);
        final BestEffortService parsed = BestEffortService.parse(service.getBytes());
        Assert.assertEquals(service, parsed);
    }

}

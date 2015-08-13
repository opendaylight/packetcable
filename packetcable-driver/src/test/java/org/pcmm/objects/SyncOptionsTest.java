/*
 * (c) 2015 Cable Television Laboratories, Inc.  All rights reserved.
 */

package org.pcmm.objects;

import org.junit.Assert;
import org.junit.Test;
import org.pcmm.objects.SyncOptions.ReportType;
import org.pcmm.objects.SyncOptions.SyncType;
import org.umu.cops.stack.COPSMsgParser;

/**
 * Tests the data holder class SyncOptions to ensure both construction and byte parsing result in correct object
 * creation.
 */
public class SyncOptionsTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullReportType() {
        new SyncOptions(null, SyncType.FULL_SYNCHRONIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSyncType() {
        new SyncOptions(ReportType.STANDARD_REPORT_DATA, null);
    }

    @Test
    public void construction() {
        final SyncOptions syncOpts = new SyncOptions(ReportType.STANDARD_REPORT_DATA, SyncType.FULL_SYNCHRONIZATION);
        final byte[] dataBytes = syncOpts.getBytes();
        Assert.assertEquals(4, dataBytes.length);
        Assert.assertEquals(ReportType.STANDARD_REPORT_DATA,
                ReportType.valueOf(COPSMsgParser.bytesToShort(dataBytes[0], dataBytes[1])));
        Assert.assertEquals(SyncType.FULL_SYNCHRONIZATION,
                SyncType.valueOf(COPSMsgParser.bytesToShort(dataBytes[2], dataBytes[3])));
    }

    @Test
    public void byteParsing() {
        final SyncOptions syncOpts = new SyncOptions(ReportType.COMPLETE_GATE_DATA, SyncType.INCREMENTAL_SYNCHRONIZATION);
        final SyncOptions parsed = SyncOptions.parse(syncOpts.getBytes());
        Assert.assertEquals(syncOpts, parsed);
    }

}

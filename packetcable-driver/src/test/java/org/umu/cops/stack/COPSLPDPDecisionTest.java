package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSLPDPDecision class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSLPDPDecisionTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor1NullCType() {
        final CType cType = null;
        new COPSLPDPDecision(cType, Command.INSTALL, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1NaCType() {
        new COPSLPDPDecision(CType.NA, Command.INSTALL, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1NullCommand() {
        new COPSLPDPDecision(CType.CSI, null, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1NullFlags() {
        new COPSLPDPDecision(CType.CSI, Command.INSTALL, null, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NullHeader() {
        final COPSObjHeader hdr = null;
        new COPSLPDPDecision(hdr, Command.INSTALL, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2InvalidCNum() {
        new COPSLPDPDecision(new COPSObjHeader(CNum.ACCT_TIMER, CType.CSI), Command.INSTALL, DecisionFlag.NA,
                new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NullCommand() {
        new COPSLPDPDecision(new COPSObjHeader(CNum.LPDP_DEC, CType.CSI), null, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NullDecison() {
        new COPSLPDPDecision(new COPSObjHeader(CNum.LPDP_DEC, CType.CSI), Command.INSTALL, null, new COPSData());
    }

    public void constructor2NullData() {
        final COPSLPDPDecision decision = new COPSLPDPDecision(new COPSObjHeader(CNum.LPDP_DEC, CType.CSI),
                Command.INSTALL, DecisionFlag.NA, null);
        Assert.assertEquals(0, decision.getData().getData().length);
    }

    @Test
    public void constructor1Valid() throws Exception {
        final COPSLPDPDecision decision = new COPSLPDPDecision(CType.CSI, Command.REMOVE, DecisionFlag.REQSTATE,
                new COPSData("1234"));
        Assert.assertEquals(Command.REMOVE, decision.getCommand());
        Assert.assertEquals(8, decision.getDataLength());
        Assert.assertEquals(new COPSData("1234"), decision.getData());
        Assert.assertEquals(DecisionFlag.REQSTATE, decision.getFlag());
        Assert.assertEquals("Client specific decision data", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Local-Decision**", lines[0]);
        Assert.assertEquals("C-num: LPDP_DEC", lines[1]);
        Assert.assertEquals("C-type: CSI", lines[2]);
        Assert.assertEquals("Decision (Client specific decision data)", lines[3]);
        Assert.assertEquals("Data: 1234", lines[4]);
    }

    @Test
    public void constructor2Valid() throws Exception {
        final COPSLPDPDecision decision = new COPSLPDPDecision(new COPSObjHeader(CNum.LPDP_DEC, CType.STATELESS), Command.INSTALL,
                DecisionFlag.REQERROR, new COPSData("1234"));
        Assert.assertEquals(Command.INSTALL, decision.getCommand());
        Assert.assertEquals(8, decision.getDataLength());
        Assert.assertEquals(new COPSData("1234"), decision.getData());
        Assert.assertEquals(DecisionFlag.REQERROR, decision.getFlag());
        Assert.assertEquals("Stateless data", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Local-Decision**", lines[0]);
        Assert.assertEquals("C-num: LPDP_DEC", lines[1]);
        Assert.assertEquals("C-type: STATELESS", lines[2]);
        Assert.assertEquals("Decision (Stateless data)", lines[3]);
        Assert.assertEquals("Data: 1234", lines[4]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

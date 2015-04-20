package org.umu.cops.stack;

import org.junit.Assert;
import org.junit.Test;
import org.umu.cops.stack.COPSDecision.Command;
import org.umu.cops.stack.COPSDecision.DecisionFlag;
import org.umu.cops.stack.COPSObjHeader.CNum;
import org.umu.cops.stack.COPSObjHeader.CType;

import java.io.ByteArrayOutputStream;

/**
 * Tests for the first constructor of the COPSDecision class.
 * Should any of these tests be inaccurate it is due to the fact that they have been written after COPSAcctTimer had been
 * released and my assumptions may be incorrect.
 */
public class COPSDecisionTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructor1NullCommand() {
        new COPSDecision(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NullCType() {
        new COPSDecision(null, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NaCType() {
        new COPSDecision(CType.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor2NullData() {
        new COPSDecision(CType.DEF, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor3NullCommand() {
        new COPSDecision(null, DecisionFlag.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor3NullDecisionFlag() {
        new COPSDecision(Command.INSTALL, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor4NullCType() {
        new COPSDecision(null, Command.INSTALL, DecisionFlag.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor4NaCType() {
        new COPSDecision(CType.NA, Command.INSTALL, DecisionFlag.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor4NullCommand() {
        new COPSDecision(CType.CSI, null, DecisionFlag.NA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor4NullFlags() {
        new COPSDecision(CType.CSI, Command.INSTALL, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor5NullCType() {
        final CType cType = null;
        new COPSDecision(cType, Command.INSTALL, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor5NaCType() {
        new COPSDecision(CType.NA, Command.INSTALL, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor5NullCommand() {
        new COPSDecision(CType.CSI, null, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor5NullFlags() {
        new COPSDecision(CType.CSI, Command.INSTALL, null, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor5NullData() {
        new COPSDecision(CType.CSI, Command.INSTALL, DecisionFlag.NA, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor6NullHeader() {
        final COPSObjHeader hdr = null;
        new COPSDecision(hdr, Command.INSTALL, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor6InvalidCNum() {
        new COPSDecision(new COPSObjHeader(CNum.ACCT_TIMER, CType.CSI), Command.INSTALL, DecisionFlag.NA,
                new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor6NullCommand() {
        new COPSDecision(new COPSObjHeader(CNum.DEC, CType.CSI), null, DecisionFlag.NA, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor6NullDecison() {
        new COPSDecision(new COPSObjHeader(CNum.DEC, CType.CSI), Command.INSTALL, null, new COPSData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor6NullData() {
        new COPSDecision(new COPSObjHeader(CNum.DEC, CType.CSI), Command.INSTALL, DecisionFlag.NA, null);
    }

    @Test
    public void constructor1CommandInstall() throws Exception {
        final COPSDecision decision = new COPSDecision(Command.INSTALL);
        Assert.assertEquals(Command.INSTALL, decision.getCommand());
        Assert.assertEquals(4, decision.getDataLength());
        Assert.assertEquals(new COPSData(), decision.getData());
        Assert.assertEquals(DecisionFlag.NA, decision.getFlag());
        Assert.assertEquals("Default", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Decision (Default)", lines[3]);
        Assert.assertEquals("Command code: INSTALL", lines[4]);
        Assert.assertEquals("Command flags: NA", lines[5]);
    }

    @Test
    public void constructor1CommandRemove() throws Exception {
        final COPSDecision decision = new COPSDecision(Command.REMOVE);
        Assert.assertEquals(Command.REMOVE, decision.getCommand());
        Assert.assertEquals(4, decision.getDataLength());
        Assert.assertEquals(new COPSData(), decision.getData());
        Assert.assertEquals(DecisionFlag.NA, decision.getFlag());
        Assert.assertEquals("Default", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Decision (Default)", lines[3]);
        Assert.assertEquals("Command code: REMOVE", lines[4]);
        Assert.assertEquals("Command flags: NA", lines[5]);
    }

    @Test
    public void constructor2ValidCTypeEmptyData() throws Exception {
        final COPSDecision decision = new COPSDecision(CType.DEF, new COPSData());
        Assert.assertEquals(Command.NULL, decision.getCommand());
        Assert.assertEquals(4, decision.getDataLength());
        Assert.assertEquals(new COPSData(), decision.getData());
        Assert.assertEquals(DecisionFlag.NA, decision.getFlag());
        Assert.assertEquals("Default", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Decision (Default)", lines[3]);
        Assert.assertEquals("Command code: NULL", lines[4]);
        Assert.assertEquals("Command flags: NA", lines[5]);
    }

    @Test
    public void constructor2ValidCTypeDefUnpaddedData() throws Exception {
        final COPSDecision decision = new COPSDecision(CType.DEF, new COPSData("1234"));
        Assert.assertEquals(Command.NULL, decision.getCommand());
        Assert.assertEquals(8, decision.getDataLength());
        Assert.assertEquals(new COPSData("1234"), decision.getData());
        Assert.assertEquals(DecisionFlag.NA, decision.getFlag());
        Assert.assertEquals("Default", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Decision (Default)", lines[3]);
        Assert.assertEquals("Command code: NULL", lines[4]);
        Assert.assertEquals("Command flags: NA", lines[5]);
    }

    @Test
    public void constructor2ValidCTypeDefPaddedData() throws Exception {
        final COPSDecision decision = new COPSDecision(CType.DEF, new COPSData("12345"));
        Assert.assertEquals(Command.NULL, decision.getCommand());
        Assert.assertEquals(12, decision.getDataLength());
        Assert.assertEquals(new COPSData("12345"), decision.getData());
        Assert.assertEquals(DecisionFlag.NA, decision.getFlag());
        Assert.assertEquals("Default", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Decision (Default)", lines[3]);
        Assert.assertEquals("Command code: NULL", lines[4]);
        Assert.assertEquals("Command flags: NA", lines[5]);
    }

    @Test
    public void constructor2ValidCTypeNamedPaddedData() throws Exception {
        final COPSDecision decision = new COPSDecision(CType.NAMED, new COPSData("12345"));
        Assert.assertEquals(Command.NULL, decision.getCommand());
        Assert.assertEquals(12, decision.getDataLength());
        Assert.assertEquals(new COPSData("12345"), decision.getData());
        Assert.assertEquals(DecisionFlag.NA, decision.getFlag());
        Assert.assertEquals("Named decision data", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: NAMED", lines[2]);
        Assert.assertEquals("Decision (Named decision data)", lines[3]);
        Assert.assertEquals("Data: 12345", lines[4]);
    }

    @Test
    public void constructor3Valid() throws Exception {
        final COPSDecision decision = new COPSDecision(Command.INSTALL, DecisionFlag.REQERROR);
        Assert.assertEquals(Command.INSTALL, decision.getCommand());
        Assert.assertEquals(4, decision.getDataLength());
        Assert.assertEquals(new COPSData(), decision.getData());
        Assert.assertEquals(DecisionFlag.REQERROR, decision.getFlag());
        Assert.assertEquals("Default", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(6, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: DEF", lines[2]);
        Assert.assertEquals("Decision (Default)", lines[3]);
        Assert.assertEquals("Command code: INSTALL", lines[4]);
        Assert.assertEquals("Command flags: REQERROR", lines[5]);
    }

    @Test
    public void constructor4Valid() throws Exception {
        final COPSDecision decision = new COPSDecision(CType.NAMED, Command.REMOVE, DecisionFlag.REQSTATE);
        Assert.assertEquals(Command.REMOVE, decision.getCommand());
        Assert.assertEquals(4, decision.getDataLength());
        Assert.assertEquals(new COPSData(), decision.getData());
        Assert.assertEquals(DecisionFlag.REQSTATE, decision.getFlag());
        Assert.assertEquals("Named decision data", decision.getTypeStr());

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        decision.dump(os);

        final String out = new String(os.toByteArray());
        System.out.println(out);
        final String[] lines = out.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: NAMED", lines[2]);
        Assert.assertEquals("Decision (Named decision data)", lines[3]);
        Assert.assertEquals("Data: ", lines[4]);
    }

    @Test
    public void constructor5Valid() throws Exception {
        final COPSDecision decision = new COPSDecision(CType.CSI, Command.REMOVE, DecisionFlag.REQSTATE,
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
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: CSI", lines[2]);
        Assert.assertEquals("Decision (Client specific decision data)", lines[3]);
        Assert.assertEquals("Data: 1234", lines[4]);
    }

    @Test
    public void constructor6Valid() throws Exception {
        final COPSDecision decision = new COPSDecision(new COPSObjHeader(CNum.DEC, CType.STATELESS), Command.INSTALL,
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
        Assert.assertEquals("**Decision**", lines[0]);
        Assert.assertEquals("C-num: DEC", lines[1]);
        Assert.assertEquals("C-type: STATELESS", lines[2]);
        Assert.assertEquals("Decision (Stateless data)", lines[3]);
        Assert.assertEquals("Data: 1234", lines[4]);
    }

    // The writeData() method will be tested implicitly via any of the COPSMsg tests
}

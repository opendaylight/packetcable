/**
 @header@
 */

package org.pcmm;

public class PCMMGlobalConfig {
    // System
    public static int Debug = 0;
    public static int LogLevel = 0;
    public static int DefaultBestEffortTrafficRate = 2500000;
    // Service Flow Attributes Defaults
    public static int DefaultBestEffortClassPriority = 69;
    public static int DefaultUnsolicatedGrantSize = 1000;
    public static int DefaultUnsolicatedGrantsPerInterval = 3;
    public static int DefaultUnsolicatedGrantInterval = 8000;
    // Gate Specification Defaults
    public static int DSCPToSMark = 0;
    public static int Priority = 0;
    public static int PreEmption = 0;
    public static int GateFlags = 0;
    public static int GateTOSField = 0;
    public static int GateTOSMask = 0;
    public static int GateClass = 0;
    // Authorization life timer
    public static short GateT1 = 200;
    // Authorization renew timer
    public static short GateT2 = 300;
    // Reservation life timer
    public static short GateT3 = 0;
    // Reservation renew timer
    public static short GateT4 = 0;

    // XXX - A new home for some of these @ org.pcmm.gates.impl.BestEffortService
    public static int UGSTransmissionPolicy = 0x037F;
    public static int BETransmissionPolicy = 0x0;
    public static int BETrafficPriority = 0x0;
    public static byte EClassifierPriority = 0x45;

    // Temporary Configure Items For Demo or Lacking Design
    public static int DefaultLowBestEffortTrafficRate = 500000;
    public static int DefaultVideoSourcePort = 8081;
    public static int DefaultAlternateSourcePort = 1369;
    // public static String DefaultCMTS = "127.0.0.1"

/*  Demo Kit Layout
    public static String DefaultCMTS = "10.32.4.3";
    public static String SubscriberID = "10.32.104.2";
    public static String dstIP = "10.32.4.208";
    public static String srcIP = "10.32.154.2";
*/

/*  LAB Bench Layout */
    public static String DefaultCMTS = "10.32.15.3";
    public static String SubscriberID = "10.32.115.143";
    public static String dstIP = "10.32.0.234";
    public static String srcIP = "10.32.215.111";

    public static String DefautRadius = "192.168.50.2";
    public static short srcPort = 8081;
    public static short dstPort = 0;
    public static int GateID1 = 0;
    public static int GateID2 = 0;
    public static void setGateID1(int n) {
        GateID1 = n;
    }
    public static int getGateID1() {
        return GateID1;
    }
    public static void setGateID2(int n) {
        GateID2 = n;
    }
    public static int getGateID2() {
        return GateID2;
    }
}

/*
 *
 * // if(Constants.DEBUG.isEnabled()) { } public enum Constants { DEBUG(true),
 * PRINT_VARS(false);
 *
 * private boolean enabled;
 *
 * private Constants(boolean enabled) { this.enabled = enabled; }
 *
 * public boolean isEnabled() { return enabled; } }
 */

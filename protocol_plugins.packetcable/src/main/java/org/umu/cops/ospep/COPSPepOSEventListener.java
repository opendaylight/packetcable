package org.umu.cops.ospep;

/**
 * Abstract class for creating listeners for outsourcing events.
 */
public abstract class COPSPepOSEventListener extends Thread {
    /**
     * COPSPepOSAgent to be waked up upon event detection.
     */
    protected COPSPepOSAgent _agent;

    /**
     * Sets the COPS agent to be waked up.
     * @param anAgent   A COPSPepOSAgent
     */
    public void setAgent(COPSPepOSAgent anAgent) {
        _agent = anAgent;
    }

    /**
     * This must implement event detection, and wake up
     * the COPS agent when it occurs. The steps are:
     * <ul>
     * <li>Detect the outsourcing event</li>
     * <li>Build a <tt>Vector clientSIs</tt> from the event</li>
     * <li>Generate a <tt>COPSHandle handle</tt> for the request</li>
     * <li>Invoke <tt>_agent.dispatchEvent(handle, clientSIs)</tt></li></ul>
     */
    public abstract void run();
}

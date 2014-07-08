/**
 @header@
 */


package org.pcmm.state;

/**
 * <p>
 * Each stateful server should implement this interface, to be able to save and
 * retrieve clients' state
 * </p>
 *
 *
 *
 */
public interface IStateful {

    /**
     * records the collected client state
     */
    void recordState();

    /**
     *
     * @return recorded state.
     */
    IState getRecoredState();

}

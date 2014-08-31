/**
 @header@
 */
package org.pcmm.objects;

/**
 *
 * Resources mapper used to associate a key to a set of values
 */
public class PCMMResourcesMapper<M, T extends PCMMResource> {

    private M key;

    private T value;

    public PCMMResourcesMapper() {
    }

    public PCMMResourcesMapper(M key, T value) {
        this.key = key;
        this.value = value;
    }

    public M getKey() {
        return key;
    }

    public void setKey(M key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

/**
 * 
 */
package org.pcmm.base;

/**
 * Adapter interface
 * 
 */
public interface IAdapter<Type> {

	Object adapt(Object object, Class<?> clazz);

	Type adapt(Object object);
}

package com.aionemu.commons.scripting;

import com.aionemu.commons.scripting.classlistener.ClassListener;

/**
 * This class represents script context that can be loaded, unloaded, etc...<br>
 */
public interface ScriptContext {

	/**
	 * Initializes script context. Calls the compilation task.<br>
	 * After compilation static methods marked with {@link com.aionemu.commons.scripting.metadata.OnClassLoad} are invoked
	 */
	void init();

	/**
	 * Notifies all script classes that they must save their data and release resources to prevent memory leaks. It's done via static methods with
	 * {@link com.aionemu.commons.scripting.metadata.OnClassUnload} annotation
	 */
	void shutdown();

	/**
	 * Invokes {@link #shutdown()}, after that invokes {@link #init()}. Root folder remains the same, but new compiler and classloader are used.
	 */
	void reload();

	/**
	 * Returns compilation result of this script context
	 * 
	 * @return compilation result
	 */
	CompilationResult getCompilationResult();

	/**
	 * Returns true if this script context is loaded
	 * 
	 * @return true if context is initialized
	 */
	boolean isInitialized();

	/**
	 * Sets the class listener for this script context.
	 * 
	 * @param cl
	 *          class listener
	 */
	void setClassListener(ClassListener cl);

	/**
	 * Returns class listener associated with this ScriptContext.<br>
	 * If it's null - returns parent classListener.<br>
	 * If parent is null and classListener is null - it will add the following class listeners as default implementation (order saved):
	 * 
	 * <pre>
	 * AggregatedClassListener acl = new AggregatedClassListener();
	 * acl.addClassListener(new OnClassLoadUnloadListener());
	 * acl.addClassListener(new ScheduledTaskClassListener());
	 * </pre>
	 *
	 * @see com.aionemu.commons.scripting.classlistener.AggregatedClassListener
	 * @see com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener
	 * @return Associated class listener
	 */
	ClassListener getClassListener();

	/**
	 * Tests if this ScriptContext is equal to another ScriptContext. Comparison is done by comparing root files and parent contexts (if there is any
	 * parent)
	 * 
	 * @param obj
	 *          object to compare with
	 * @return result of comparison
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Returns hashCoded of this ScriptContext. Hashcode is calculated using root file and parent context(if available)
	 * 
	 * @return hashCode
	 */
	@Override
	int hashCode();
}

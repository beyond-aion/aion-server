package com.aionemu.commons.callbacks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Generic interface for all enhanced object.<br>
 * <font color="red">NEVER IMPLEMENT THIS CLASS MANUALLY!!!</font> <br>
 * <br>
 * <b>Thread safety, concurrency, deadlocks:</b><br>
 * It's allowed to remove/add listeners from listeners.<br>
 * Listeners are stored in the {@link java.util.concurrent.CopyOnWriteArrayList}, so their behavior is similar.<br>
 * Briefly speaking, if you will try to remove/add a listener from another listener - the current invocation won't be affected, current implementation
 * allocates all listeners that are going to be invoked before execution.<br>
 * <br>
 * {@link Callback#beforeCall(Object, Object[])} and {@link Callback#afterCall(Object, Object[], Object)} are treated as separate invocations, so
 * adding/removing listener in beforeCall will affect afterCall.
 *
 * @author SoulKeeper
 */
@SuppressWarnings("rawtypes")
public interface EnhancedObject {

	/**
	 * Adds callback to this object.<br>
	 * {@link com.aionemu.commons.callbacks.EnhancedObject concurrency description}
	 *
	 * @param callback
	 *          instance of callback to add
	 * @see com.aionemu.commons.callbacks.util.ObjectCallbackHelper#addCallback(Callback, EnhancedObject)
	 */
	public void addCallback(Callback callback);

	/**
	 * Removes callback from this object.<br>
	 * {@link com.aionemu.commons.callbacks.EnhancedObject concurrency description}
	 *
	 * @param callback
	 *          instance of callback to remove
	 * @see com.aionemu.commons.callbacks.util.ObjectCallbackHelper#removeCallback(Callback, EnhancedObject)
	 */
	public void removeCallback(Callback callback);

	/**
	 * Returns all callbacks associated with this.<br>
	 * <br>
	 * <b><font color="red"> Iteration over this map is not thread-safe, please make sure that {@link #getCallbackLock()} is locked in read mode to
	 * read.<br>
	 * <br>
	 * Same for writing. If you are going to write something here - please make sure that {@link #getCallbackLock()} is in write mode </b></font>
	 *
	 * @return map with callbacks associated with this object or null if there is no callbacks
	 */
	public Map<Class<? extends Callback>, List<Callback>> getCallbacks();

	/**
	 * Associates callback map with this object.<br>
	 * <br>
	 * <b><font color="red"> Please make sure that {@link #getCallbackLock()} is in write-mode lock when calling this method </b></font>
	 *
	 * @param callbacks
	 *          callbackMap or null
	 */
	public void setCallbacks(Map<Class<? extends Callback>, List<Callback>> callbacks);

	/**
	 * Returns lock that is used to ensure thread safety
	 *
	 * @return lock that is used to ensure thread safety
	 */
	public ReentrantReadWriteLock getCallbackLock();
}

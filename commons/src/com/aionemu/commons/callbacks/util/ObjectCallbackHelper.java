package com.aionemu.commons.callbacks.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.commons.callbacks.EnhancedObject;
import com.aionemu.commons.utils.GenericValidator;

/**
 * Class that implements helper methods for callbacks.<br>
 * All enhanced objects are delegating main part of their logic to this class
 *
 * @author SoulKeeper
 */
@SuppressWarnings("rawtypes")
public class ObjectCallbackHelper {

	/**
	 * Private empty constructor to prevent initialization
	 */
	private ObjectCallbackHelper() {

	}

	/**
	 * Adds callback to the list.<br>
	 * Sorting is done while adding to avoid extra calls.
	 *
	 * @param callback
	 *          what to add
	 * @param object
	 *          add callback to which objec
	 */
	@SuppressWarnings({ "unchecked" })
	public static void addCallback(Callback callback, EnhancedObject object) {
		try {
			object.getCallbackLock().writeLock().lock();

			Map<Class<? extends Callback>, List<Callback>> cbMap = object.getCallbacks();
			if (cbMap == null) {
				cbMap = new HashMap<>();
				object.setCallbacks(cbMap);
			}

			List<Callback> list = cbMap.get(callback.getBaseClass());
			if (list == null) {
				list = new CopyOnWriteArrayList<>();
				cbMap.put(callback.getBaseClass(), list);
			}

			CallbacksUtil.insertCallbackToList(callback, list);
		} finally {
			object.getCallbackLock().writeLock().unlock();
		}
	}

	/**
	 * Removes callback from the list
	 *
	 * @param callback
	 *          what to remove
	 * @param object
	 *          remove callback from which object
	 */
	public static void removeCallback(Callback callback, EnhancedObject object) {
		try {
			object.getCallbackLock().writeLock().lock();

			Map<Class<? extends Callback>, List<Callback>> cbMap = object.getCallbacks();
			if (GenericValidator.isBlankOrNull(cbMap)) {
				return;
			}

			List<Callback> list = cbMap.get(callback.getBaseClass());
			if (list == null || !list.remove(callback)) {
				// noinspection ThrowableInstanceNeverThrown
				LoggerFactory.getLogger(ObjectCallbackHelper.class).error("Attempt to remove callback that doesn't exists", new RuntimeException());
				return;
			}

			if (list.isEmpty()) {
				cbMap.remove(callback.getBaseClass());
			}

			if (cbMap.isEmpty()) {
				object.setCallbacks(null);
			}

		} finally {
			object.getCallbackLock().writeLock().unlock();
		}
	}

	/**
	 * This method call callbacks before actual method invocation takes place
	 *
	 * @param obj
	 *          object that callbacks are invoked for
	 * @param callbackClass
	 *          base callback class
	 * @param args
	 *          args of method
	 * @return {@link Callback#beforeCall(Object, Object[])}
	 */
	@SuppressWarnings("unchecked")
	public static CallbackResult<?> beforeCall(EnhancedObject obj, Class callbackClass, Object... args) {
		Map<Class<? extends Callback>, List<Callback>> cbMap = obj.getCallbacks();
		if (GenericValidator.isBlankOrNull(cbMap)) {
			return CallbackResult.newContinue();
		}

		CallbackResult<?> cr = null;
		List<Callback> list = null;

		try {
			obj.getCallbackLock().readLock().lock();
			list = cbMap.get(callbackClass);
		} finally {
			obj.getCallbackLock().readLock().unlock();
		}

		if (GenericValidator.isBlankOrNull(list)) {
			return CallbackResult.newContinue();
		}

		for (Callback c : list) {
			try {
				cr = c.beforeCall(obj, args);
				if (cr.isBlockingCallbacks()) {
					break;
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(ObjectCallbackHelper.class).error("Uncaught exception in callback", e);
			}
		}

		return cr == null ? CallbackResult.newContinue() : cr;
	}

	/**
	 * This method invokes callbacks after method invocation
	 *
	 * @param obj
	 *          object that invokes this method
	 * @param callbackClass
	 *          superclass of callback
	 * @param args
	 *          method args
	 * @param result
	 *          method invokation result
	 * @return {@link Callback#afterCall(Object, Object[], Object)}
	 */
	@SuppressWarnings("unchecked")
	public static CallbackResult<?> afterCall(EnhancedObject obj, Class callbackClass, Object[] args, Object result) {
		Map<Class<? extends Callback>, List<Callback>> cbMap = obj.getCallbacks();
		if (GenericValidator.isBlankOrNull(cbMap)) {
			return CallbackResult.newContinue();
		}

		CallbackResult<?> cr = null;
		List<Callback> list = null;

		try {
			obj.getCallbackLock().readLock().lock();
			list = cbMap.get(callbackClass);
		} finally {
			obj.getCallbackLock().readLock().unlock();
		}

		if (GenericValidator.isBlankOrNull(list)) {
			return CallbackResult.newContinue();
		}

		for (Callback c : list) {
			try {
				cr = c.afterCall(obj, args, result);
				if (cr.isBlockingCallbacks()) {
					break;
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(ObjectCallbackHelper.class).error("Uncaught exception in callback", e);
			}
		}

		return cr == null ? CallbackResult.newContinue() : cr;
	}
}

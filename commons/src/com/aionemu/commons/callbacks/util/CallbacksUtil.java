package com.aionemu.commons.callbacks.util;

import java.util.List;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackPriority;

import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;

@SuppressWarnings("rawtypes")
public class CallbacksUtil {

	/**
	 * Checks if annotation is present on method
	 *
	 * @param method
	 *          Method to check
	 * @param annotation
	 *          Annotation to look for
	 * @return result
	 */
	public static boolean isAnnotationPresent(CtMethod method, Class<? extends java.lang.annotation.Annotation> annotation) {
		for (Object o : method.getMethodInfo().getAttributes()) {
			if (o instanceof AnnotationsAttribute) {
				AnnotationsAttribute attribute = (AnnotationsAttribute) o;
				if (attribute.getAnnotation(annotation.getName()) != null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns priority of callback.<br>
	 * Method checks if callback is instance of {@link com.aionemu.commons.callbacks.CallbackPriority}, and returns
	 * <p/>
	 * 
	 * <pre>
	 * {@link com.aionemu.commons.callbacks.CallbackPriority#DEFAULT_PRIORITY} - {@link com.aionemu.commons.callbacks.CallbackPriority#getPriority()}
	 * </pre>
	 * <p/>
	 * .<br>
	 * If callback is not instance of CallbackPriority then it returns {@link com.aionemu.commons.callbacks.CallbackPriority#DEFAULT_PRIORITY}
	 *
	 * @param callback
	 *          priority to get from
	 * @return priority of callback
	 */
	public static int getCallbackPriority(Callback callback) {
		if (callback instanceof CallbackPriority) {
			CallbackPriority instancePriority = (CallbackPriority) callback;
			return CallbackPriority.DEFAULT_PRIORITY - instancePriority.getPriority();
		} else {
			return CallbackPriority.DEFAULT_PRIORITY;
		}
	}

	protected static void insertCallbackToList(Callback callback, List<Callback> list) {
		int callbackPriority = CallbacksUtil.getCallbackPriority(callback);

		if (!list.isEmpty()) {
			// hand-made sorting, if needed to insert to the middle
			for (int i = 0, n = list.size(); i < n; i++) {
				Callback c = list.get(i);

				int cPrio = CallbacksUtil.getCallbackPriority(c);

				if (callbackPriority < cPrio) {
					list.add(i, callback);
					return;
				}
			}
		}
		// add last
		list.add(callback);
	}
}

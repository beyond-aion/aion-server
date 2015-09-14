package com.aionemu.commons.callbacks.util;

import java.util.Comparator;

import com.aionemu.commons.callbacks.Callback;

/**
 * Compares priority of two callbacks.<br>
 * It's not necessary for callback to implement {@link com.aionemu.commons.callbacks.CallbackPriority} for callback to has the priority
 *
 * @author SoulKeeper
 */
public class CallbackPriorityComparator implements Comparator<Callback<?>> {

	@Override
	public int compare(Callback<?> o1, Callback<?> o2) {
		int p1 = CallbacksUtil.getCallbackPriority(o1);
		int p2 = CallbacksUtil.getCallbackPriority(o2);

		if (p1 < p2) {
			return -1;
		} else if (p1 == p2) {
			return 0;
		} else {
			return 1;
		}
	}
}

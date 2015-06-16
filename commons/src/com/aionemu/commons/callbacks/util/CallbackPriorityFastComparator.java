package com.aionemu.commons.callbacks.util;

import com.aionemu.commons.callbacks.Callback;
import javolution.util.FastComparator;

public class CallbackPriorityFastComparator extends FastComparator<Callback<?>>{

	private static final long serialVersionUID = 5346780764438744817L;
	private final CallbackPriorityComparator cpc = new CallbackPriorityComparator();

	@Override
	public int hashCodeOf(Callback<?> obj) {
		return obj.hashCode();
	}

	@Override
	public boolean areEqual(Callback<?> o1, Callback<?> o2) {
		return cpc.compare(o1, o2) == 0;
	}

	@Override
	public int compare(Callback<?> o1, Callback<?> o2) {
		return cpc.compare(o1, o2);
	}
}
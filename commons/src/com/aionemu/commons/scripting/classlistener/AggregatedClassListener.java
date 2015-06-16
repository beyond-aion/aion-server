package com.aionemu.commons.scripting.classlistener;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * ClassListener that aggregates a collection of ClassListeners.<br>
 * Please note that "shutdown" listeners will be executed in reverse order.
 *
 * @author SoulKeeper
 */
public class AggregatedClassListener implements ClassListener {

	private final List<ClassListener> classListeners;

	public AggregatedClassListener() {
		classListeners = Lists.newArrayList();
	}

	public AggregatedClassListener(List<ClassListener> classListeners) {
		this.classListeners = classListeners;
	}

	public List<ClassListener> getClassListeners() {
		return classListeners;
	}

	public void addClassListener(ClassListener cl) {
		getClassListeners().add(cl);
	}

	@Override
	public void postLoad(Class<?>[] classes) {
		for (ClassListener cl : getClassListeners()) {
			cl.postLoad(classes);
		}
	}

	@Override
	public void preUnload(Class<?>[] classes) {
		for (ClassListener cl : Lists.reverse(getClassListeners())) {
			cl.preUnload(classes);
		}
	}
}

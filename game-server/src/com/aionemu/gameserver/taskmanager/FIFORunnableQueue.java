package com.aionemu.gameserver.taskmanager;

import com.aionemu.commons.utils.concurrent.ExecuteWrapper;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author NB4L1
 */
public abstract class FIFORunnableQueue<T extends Runnable> extends FIFOSimpleExecutableQueue<T> {

	@Override
	protected final void removeAndExecuteFirst() {
		ExecuteWrapper.execute(removeFirst(), ThreadPoolManager.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING);
	}
}

package com.aionemu.gameserver.dataholders.loadingutils;

import javax.xml.bind.Unmarshaller;

import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Helper to get a reference to the {@linkplain StaticData} while being unmarshalled.
 * 
 * @author Neon
 */
public class StaticDataListener extends Unmarshaller.Listener {

	private final StaticData staticData;

	public StaticDataListener(StaticData staticData) {
		this.staticData = staticData;
	}

	public static StaticData get(Unmarshaller u) {
		Unmarshaller.Listener listener = u.getListener();
		return listener instanceof StaticDataListener ? ((StaticDataListener) listener).staticData : null;
	}

	/**
	 * Runs given task asynchronously and adds it to the StaticData of the current unmarshaller or runs it synchronously if there is none.
	 */
	public static void registerForAsyncExecutionOrRun(Unmarshaller u, Runnable task) {
		StaticData staticData = get(u);
		if (staticData != null) {
			staticData.addAfterUnmarshalTask(ThreadPoolManager.getInstance().submitLongRunning(task));
		} else {
			task.run();
		}
	}
}

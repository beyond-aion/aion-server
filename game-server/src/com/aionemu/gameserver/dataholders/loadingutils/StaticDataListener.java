package com.aionemu.gameserver.dataholders.loadingutils;

import javax.xml.bind.Unmarshaller;

import com.aionemu.gameserver.dataholders.StaticData;

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
}

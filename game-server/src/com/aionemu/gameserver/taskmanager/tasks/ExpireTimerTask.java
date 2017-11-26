package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;

/**
 * @author Mr. Poke
 */
public class ExpireTimerTask extends AbstractPeriodicTaskManager {

	private final Map<Expirable, Player> expirables = new ConcurrentHashMap<>();

	/**
	 * @param period
	 */
	public ExpireTimerTask() {
		super(1000);
	}

	public static ExpireTimerTask getInstance() {
		return SingletonHolder.instance;
	}

	public void registerExpirable(Expirable expirable, Player player) {
		registerExpirables(Collections.singletonList(expirable), player);
	}

	public void registerExpirables(Collection<? extends Expirable> expirables, Player player) {
		for (Expirable expirable : expirables) {
			if (expirable.getExpireTime() > 0)
				this.expirables.put(expirable, player);
		}
	}

	public void unregisterExpirables(Player player) {
		expirables.values().removeIf(owner -> player.equals(owner));
	}

	@Override
	public void run() {
		int timeNow = (int) (System.currentTimeMillis() / 1000);
		for (Iterator<Map.Entry<Expirable, Player>> i = expirables.entrySet().iterator(); i.hasNext();) {
			Map.Entry<Expirable, Player> entry = i.next();
			Expirable expirable = entry.getKey();
			Player player = entry.getValue();
			int remainingSeconds = expirable.getExpireTime() - timeNow;
			if (remainingSeconds < 0 && expirable.canExpireNow()) {
				expirable.onExpire(player);
				i.remove();
			} else {
				switch (remainingSeconds) {
					case 1800:
					case 900:
					case 600:
					case 300:
					case 60:
						expirable.onBeforeExpire(player, remainingSeconds / 60);
						break;
				}
			}
		}
	}

	private static class SingletonHolder {

		protected static final ExpireTimerTask instance = new ExpireTimerTask();
	}
}

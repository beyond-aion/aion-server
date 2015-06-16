package com.aionemu.gameserver.services.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Source
 */
public class PlayerEventService {

	private static final Logger log = LoggerFactory.getLogger(PlayerEventService.class);

	private PlayerEventService() {

		final EventCollector visitor = new EventCollector();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				World.getInstance().doOnAllPlayers(visitor);
			}

		}, EventsConfig.EVENT_PERIOD * 60000, EventsConfig.EVENT_PERIOD * 60000);
	}

	private static final class EventCollector implements Visitor<Player> {

		@Override
		public void visit(Player player) {
			int membership = player.getClientConnection().getAccount().getMembership();
			int rate = EventsConfig.EVENT_REWARD_MEMBERSHIP_RATE ? membership + 1 : 1; 
			long count = EventsConfig.EVENT_ITEM_COUNT * rate;
			if (membership >= EventsConfig.EVENT_REWARD_MEMBERSHIP) {
				try {
					int itemToReward = player.getRace() == Race.ELYOS ? EventsConfig.EVENT_ITEM_ELYOS : EventsConfig.EVENT_ITEM_ASMO;
					if (itemToReward == ItemId.KINAH.value()) {
						player.getInventory().increaseKinah(count);
					}
					else {
						if (player.getInventory().isFull()) {
							log.warn("[EventReward] player " + player.getName() + " tried to receive item with full inventory.");
						}
						else {
							ItemService.addItem(player, itemToReward, count);
						}
					}
				}
				catch (Exception ex) {
					log.error("Exception during event rewarding of player " + player.getName(), ex);
				}
			}
		}

	};

	public static PlayerEventService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final PlayerEventService instance = new PlayerEventService();
	}

}

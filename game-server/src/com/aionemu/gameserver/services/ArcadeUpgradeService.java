package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.event.ArcadeProgress;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTab;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTabItemList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPGRADE_ARCADE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import javolution.util.FastTable;

/**
 * @author ginho1
 * @reworked Estrayl
 */
public class ArcadeUpgradeService {

	public static final ArcadeUpgradeService getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * A map containing all player arcade progresses to avoid data loss caused by disconnects.
	 * Consider, each progress will be lost after restarting the server.
	 */
	private final Map<Integer, ArcadeProgress> cachedProgress = new ConcurrentHashMap<>();
	private int[] tabReward = new int[4];

	private ArcadeUpgradeService() {
		tabReward[0] = 0;
		tabReward[1] = 4;
		tabReward[2] = 6;
		tabReward[3] = 8;
	}

	private ArcadeProgress getProgress(final int objId) {
		ArcadeProgress progress = cachedProgress.putIfAbsent(objId, new ArcadeProgress(objId));
		return progress != null ? progress : cachedProgress.get(objId);
	}

	public int getRewardTabForLevel(int level) {
		int tab = 1;

		for (int i = tabReward.length; i > 0; i--) {
			if (level >= tabReward[(i - 1)]) {
				tab = i;
				break;
			}
		}
		if (tab > tabReward.length)
			return tabReward.length;
		return tab;
	}

	public void start(Player player, int sessionId) {
		ArcadeProgress progress = getProgress(player.getObjectId());
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(1, progress.getFrenzyPoints(), sessionId));
		if (progress.getCurrentLevel() > 1)
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(4, progress.getCurrentLevel()));
	}

	public void open(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(2));
	}

	public void showRewardList(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(10));
	}

	public List<ArcadeTab> getTabs() {
		return DataManager.ARCADE_UPGRADE_DATA.getArcadeTabs();
	}

	public void startTry(Player player) {
		if (!EventsConfig.ENABLE_EVENT_ARCADE)
			return;

		final ArcadeProgress progress = getProgress(player.getObjectId());

		if (progress.getCurrentLevel() >= 8) {
			return;
		} else if (progress.getCurrentLevel() == 1) {
			if (!player.getInventory().decreaseByItemId(186000389, 1))
				return;

			if (!progress.isFrenzyActive()) {
				progress.setFrenzyPoints(progress.getFrenzyPoints() + 8);

				if (progress.getFrenzyPoints() >= 100) {
					PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(7, 90));
					progress.setFrenzyActive(true);
					progress.setFrenzyPoints(8);

					ThreadPoolManager.getInstance().schedule(() -> {
						PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(7, 0));
						progress.setFrenzyActive(false);
					}, 90000);
				}
			}
		}
		final boolean success = Rnd.chance() < EventsConfig.EVENT_ARCADE_CHANCE;
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(3, success, progress.getFrenzyPoints()));
		if (success) {
			ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(4, progress.setCurrentLevel(progress.getCurrentLevel() + 1)));
			}, 3000);
		} else {
			ThreadPoolManager.getInstance().schedule(
				() -> {
					final boolean isResumeAllowed = progress.getCurrentLevel() == 7 && progress.isResumeAllowed();
					PacketSendUtility.sendPacket(player,
						new SM_UPGRADE_ARCADE(5, progress.setCurrentLevel(1), EventsConfig.ARCADE_RESUME_TOKEN, isResumeAllowed));
				}, 3000);
		}
	}

	public void resume(Player player) {
		if (!player.getInventory().decreaseByItemId(186000389, EventsConfig.ARCADE_RESUME_TOKEN)) {
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(8));
			return;
		}
		final ArcadeProgress progress = getProgress(player.getObjectId());
		progress.setResumeAllowed(false);
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(4, progress.setCurrentLevel(7)));
	}

	public void getReward(Player player) {
		if (!EventsConfig.ENABLE_EVENT_ARCADE)
			return;

		ArcadeProgress progress = getProgress(player.getObjectId());
		List<ArcadeTabItemList> rewardList = new FastTable<>();

		final int rewardTab = getRewardTabForLevel(progress.getCurrentLevel());
		for (ArcadeTab arcadeTab : getTabs()) {
			if (rewardTab == arcadeTab.getId()) {
				for (ArcadeTabItemList arcadeTabItem : arcadeTab.getArcadeTabItems()) {
					if (progress.isFrenzyActive()) {
						if (arcadeTabItem.getFrenzyCount() > 0)
							rewardList.add(arcadeTabItem);
					} else {
						if (arcadeTabItem.getNormalCount() > 0)
							rewardList.add(arcadeTabItem);
					}
				}
			}
		}

		if (rewardList.size() > 0) {
			int index = Rnd.get(0, rewardList.size() - 1);
			ArcadeTabItemList item = rewardList.get(index);
			ItemService.addItem(player, item.getItemId(), progress.isFrenzyActive() ? item.getFrenzyCount() : item.getNormalCount());
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(6, item));
			progress.setCurrentLevel(1);
			progress.setResumeAllowed(true);
		}
	}

	private static class SingletonHolder {

		protected static final ArcadeUpgradeService instance = new ArcadeUpgradeService();
	}
}

package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

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

/**
 * @author ginho1
 * @modified Estrayl
 */
public class ArcadeUpgradeService {

	public static final ArcadeUpgradeService getInstance() {
		return SingletonHolder.instance;
	}

	private final Map<Integer, ArcadeProgress> cachedProgress = new FastMap<>();
	private int[] tabReward = new int[4];

	private ArcadeUpgradeService() {
		tabReward[0] = 0;
		tabReward[1] = 4;
		tabReward[2] = 6;
		tabReward[3] = 8;
	}

	public synchronized void cacheProgress(final Player player) {
		final int objId = player.getObjectId();
		if (cachedProgress.containsKey(objId))
			cachedProgress.replace(objId, player.getArcadeProgress());
		else
			cachedProgress.put(objId, player.getArcadeProgress());
	}

	public void loadProgress(final Player player) {
		final int objId = player.getObjectId();
		player.setArcadeProgress(cachedProgress.containsKey(objId) ? cachedProgress.get(objId) : new ArcadeProgress(objId));
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
		ArcadeProgress progress = player.getArcadeProgress();
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

		final ArcadeProgress progress = player.getArcadeProgress();

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
		handleTry(player, progress, true);
	}

	public void resume(Player player) {
		final ArcadeProgress progress = player.getArcadeProgress();

		if (progress.getCurrentLevel() >= 8)
			return;

		if (!player.getInventory().decreaseByItemId(186000389, EventsConfig.ARCADE_RESUME_TOKEN)) {
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(8));
			return;
		}
		handleTry(player, progress, false);
	}
	
	private void handleTry(Player player, ArcadeProgress progress, boolean resumeAllowed) {
		final boolean success = Rnd.get(1, 100) <= EventsConfig.EVENT_ARCADE_CHANCE;
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(3, success, progress.getFrenzyPoints()));
		if (success) {
			progress.setCurrentLevel(progress.getCurrentLevel() + 1);
			ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(4, progress.getCurrentLevel()));
			}, 3000);
		} else {
			ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(5, progress.getCurrentLevel(), EventsConfig.ARCADE_RESUME_TOKEN, resumeAllowed));
			}, 3000);
		}
	}

	public void getReward(Player player) {
		if (!EventsConfig.ENABLE_EVENT_ARCADE)
			return;

		ArcadeProgress progress = player.getArcadeProgress();
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
		}
	}

	private static class SingletonHolder {

		protected static final ArcadeUpgradeService instance = new ArcadeUpgradeService();
	}
}

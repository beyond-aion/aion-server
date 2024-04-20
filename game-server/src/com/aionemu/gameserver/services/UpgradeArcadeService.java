package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.event.ArcadeProgress;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeLevel;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeRewardItem;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeRewards;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPGRADE_ARCADE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;

/**
 * @author ginho1, Estrayl, Neon
 */
public class UpgradeArcadeService {

	private static final int FRENZY_POINTS_PER_TOKEN = 8;

	/**
	 * A map containing all player arcade progresses to avoid data loss caused by disconnects.
	 * Consider, each progress will be lost after restarting the server.
	 */
	private final Map<Integer, ArcadeProgress> cachedProgress = new ConcurrentHashMap<>();

	private ArcadeProgress getProgress(int objId) {
		ArcadeProgress progress = cachedProgress.putIfAbsent(objId, new ArcadeProgress(objId));
		return progress != null ? progress : cachedProgress.get(objId);
	}

	public void start(Player player, int sessionId) {
		ArcadeProgress progress = getProgress(player.getObjectId());
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(progress, sessionId));
		if (progress.getCurrentLevel() > 1)
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(progress));
		if (progress.getFrenzyEndTimeMillis() > System.currentTimeMillis())
			sendRemainingFrenzyModeTime(player, progress);
	}

	private void sendRemainingFrenzyModeTime(Player player, ArcadeProgress progress) {
		int remainingFrenzyModeSeconds = (int) ((progress.getFrenzyEndTimeMillis() - System.currentTimeMillis()) / 1000);
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(Math.max(0, remainingFrenzyModeSeconds)));
	}

	public void open(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE());
	}

	public void showRewardList(Player player) {
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(UpgradeArcadeService.getInstance().getRewards()));
	}

	public List<ArcadeRewards> getRewards() {
		return DataManager.UPGRADE_ARCADE_DATA.getRewards();
	}

	public ArcadeRewards getRewardsForLevel(int level) {
		List<ArcadeRewards> arcadeRewards = DataManager.UPGRADE_ARCADE_DATA.getRewards();
		for (int i = arcadeRewards.size() - 1; i >= 0; i--) {
			ArcadeRewards rewards = arcadeRewards.get(i);
			if (level >= rewards.getMinLevel())
				return rewards;
		}
		return null;
	}

	public void startTry(Player player) {
		ArcadeProgress progress = getProgress(player.getObjectId());
		long nowMillis = System.currentTimeMillis();
		if (nowMillis < progress.getNextTryTimeMillis()) {
			AuditLogger.log(player, "tried to start next arcade try while the button was still greyed out");
			return;
		}
		if (progress.getCurrentLevel() >= DataManager.UPGRADE_ARCADE_DATA.getMaxUpgradeLevel().getLevel()) {
			return;
		} else if (progress.getCurrentLevel() == 0) {
			if (!player.getInventory().decreaseByItemId(186000389, 1))
				return;

			progress.setCurrentLevel(1);
			increaseFrenzyPoints(player, progress, FRENZY_POINTS_PER_TOKEN);
		} else if (progress.getCurrentLevel() == progress.getResumeLevel()) { // start after paying the tokens to resume
			increaseFrenzyPoints(player, progress, FRENZY_POINTS_PER_TOKEN * EventsConfig.ARCADE_RESUME_TOKEN);
		}
		int delayMillis = 3000;
		progress.setTimeNextTry(nowMillis + delayMillis);
		boolean success = Rnd.chance() < getUpgradeChance(progress.getCurrentLevel());
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(success, progress));
		if (success) {
			ThreadPoolManager.getInstance().schedule(() -> {
				progress.setCurrentLevel(progress.getCurrentLevel() + 1);
				PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(progress));
			}, delayMillis);
		} else {
			ThreadPoolManager.getInstance().schedule(() -> {
				boolean canResume = progress.getResumeLevel() == 0 && progress.getCurrentLevel() >= DataManager.UPGRADE_ARCADE_DATA.getMinResumableLevel();
				progress.setResumeLevel(canResume ? progress.getCurrentLevel() : 0);
				progress.setCurrentLevel(1);
				PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(progress, canResume));
			}, delayMillis);
		}
	}

	private void increaseFrenzyPoints(Player player, ArcadeProgress progress, int frenzyPoints) {
		int frenzyModeThreshold = 100;
		progress.setFrenzyPoints(progress.getFrenzyPoints() + frenzyPoints);
		if (progress.getFrenzyPoints() >= frenzyModeThreshold) {
			progress.setFrenzyPoints(progress.getFrenzyPoints() % frenzyModeThreshold);
			int frenzyDurationSeconds = 90;
			long frenzyDurationMillis = frenzyDurationSeconds * 1000;
			progress.setFrenzyEndTimeMillis(System.currentTimeMillis() + frenzyDurationMillis);
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(frenzyDurationSeconds));
			int playerId = player.getObjectId();
			ThreadPoolManager.getInstance().schedule(() -> {
				Player p = World.getInstance().getPlayer(playerId);
				if (p != null)
					sendRemainingFrenzyModeTime(p, progress);
			}, frenzyDurationMillis);
		}
	}

	private float getUpgradeChance(int currentLevel) {
		ArcadeLevel lv = DataManager.UPGRADE_ARCADE_DATA.getUpgradeLevels().stream().filter(level -> level.getLevel() == currentLevel).findFirst()
			.orElse(null);
		return lv == null ? DataManager.UPGRADE_ARCADE_DATA.getMaxUpgradeLevel().getUpgradeChance() : lv.getUpgradeChance();
	}

	public void resume(Player player) {
		ArcadeProgress progress = getProgress(player.getObjectId());
		if (progress.getResumeLevel() == 0) {
			AuditLogger.log(player, "illegally tried to resume arcade");
			return;
		}
		if (!player.getInventory().decreaseByItemId(186000389, EventsConfig.ARCADE_RESUME_TOKEN)) {
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(8, true));
			return;
		}
		progress.setCurrentLevel(progress.getResumeLevel());
		PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(progress));
	}

	public void getReward(Player player) {
		ArcadeProgress progress = getProgress(player.getObjectId());
		if (progress.getCurrentLevel() == 0) {
			AuditLogger.log(player, "tried to get arcade rewards without spending token");
			return;
		}
		List<ArcadeRewardItem> rewardList = new ArrayList<>();

		ArcadeRewards rewards = getRewardsForLevel(progress.getCurrentLevel());
		if (rewards == null)
			return;
		boolean isFrenzyActive = System.currentTimeMillis() < progress.getFrenzyEndTimeMillis();
		for (ArcadeRewardItem arcadeTabItem : rewards.getArcadeRewardItems()) {
			if (isFrenzyActive) {
				if (arcadeTabItem.getFrenzyCount() > 0)
					rewardList.add(arcadeTabItem);
			} else if (arcadeTabItem.getNormalCount() > 0) {
				rewardList.add(arcadeTabItem);
			}
		}

		ArcadeRewardItem item = Rnd.get(rewardList);
		if (item != null) {
			long itemCount = isFrenzyActive ? item.getFrenzyCount() : item.getNormalCount();
			ItemService.addItem(player, item.getItemId(), itemCount, true);
			PacketSendUtility.sendPacket(player, new SM_UPGRADE_ARCADE(item.getItemId(), itemCount));
			progress.setResumeLevel(0);
			progress.setCurrentLevel(0);
		}
	}

	public static UpgradeArcadeService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final UpgradeArcadeService instance = new UpgradeArcadeService();
	}
}

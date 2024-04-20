package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.event.ArcadeProgress;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeLevel;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeRewardItem;
import com.aionemu.gameserver.model.templates.event.upgradearcade.ArcadeRewards;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ginho1, Neon, Estrayl
 */
public class SM_UPGRADE_ARCADE extends AionServerPacket {

	private final int action;
	private ArcadeProgress progress;
	private int sessionId;
	private boolean showIcon;
	private boolean success;
	private int frenzyDurationSeconds;
	private boolean disableWindow;
	private int rewardItemId;
	private long rewardItemCount;
	private List<ArcadeRewards> arcadeRewards;

	public SM_UPGRADE_ARCADE() {
		this.action = 2;
	}

	public SM_UPGRADE_ARCADE(boolean showIcon) {
		this.action = 0;
		this.showIcon = showIcon;
	}

	public SM_UPGRADE_ARCADE(ArcadeProgress progress, int sessionId) {
		this.action = 1;
		this.progress = progress;
		this.sessionId = sessionId;
	}

	public SM_UPGRADE_ARCADE(boolean success, ArcadeProgress progress) {
		this.action = 3;
		this.success = success;
		this.progress = progress;
	}

	public SM_UPGRADE_ARCADE(ArcadeProgress progress) {
		this.action = 4;
		this.progress = progress;
	}

	public SM_UPGRADE_ARCADE(ArcadeProgress progress, boolean resumeAllowed) {
		this.action = 5;
		this.progress = progress;
	}

	public SM_UPGRADE_ARCADE(int itemId, long count) {
		this.action = 6;
		this.rewardItemId = itemId;
		this.rewardItemCount = count;
	}

	public SM_UPGRADE_ARCADE(int frenzyDurationSeconds) {
		this.action = 7;
		this.frenzyDurationSeconds = frenzyDurationSeconds;
	}

	public SM_UPGRADE_ARCADE(int action, boolean disableWindow) {
		this.action = action;
		this.disableWindow = disableWindow;
	}

	public SM_UPGRADE_ARCADE(List<ArcadeRewards> rewards) {
		this.action = 10;
		this.arcadeRewards = rewards;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);

		switch (action) {
			case 0:// show icon
				writeD(showIcon ? 1 : 0);
				break;
			case 1: // show start upgrade arcade info
				writeD(sessionId);// SessionId
				writeD(progress.getFrenzyPoints());// frenzy meter
				for (ArcadeRewards arcadeReward : DataManager.UPGRADE_ARCADE_DATA.getRewards())
					writeD(arcadeReward.getMinLevel());
				writeD(DataManager.UPGRADE_ARCADE_DATA.getMaxUpgradeLevel().getLevel());
				writeC(1);
				writeC(DataManager.UPGRADE_ARCADE_DATA.getUpgradeLevels().size() * 2);
				for (ArcadeLevel arcadeLevel : DataManager.UPGRADE_ARCADE_DATA.getUpgradeLevels())
					writeS(arcadeLevel.getIcon());
				break;
			case 2: // open upgrade arcade
				writeC(1);// unk
				break;
			case 3: // upgrade start
				writeC(success ? 1 : 0);// 1 success - 0 fail
				writeD(progress.getFrenzyPoints());
				break;
			case 4: // update success
				writeD(progress.getCurrentLevel());// upgradeLevel
				break;
			case 5: // upgrade fail
				writeD(progress.getCurrentLevel());// upgradeLevel
				writeC(progress.getResumeLevel() > 0 ? 1 : 0);// canResume? 1 yes - 0 no
				writeQ(EventsConfig.ARCADE_RESUME_TOKEN);// needed Arcade Token
				break;
			case 6: // show reward item
				writeD(rewardItemId);
				writeQ(rewardItemCount);
				break;
			case 7: // frenzy time
				writeD(frenzyDurationSeconds);
				break;
			case 8: // disable window
				writeC(disableWindow ? 1 : 0); // msg when true: you don't have enough tokens
				break;
			case 10: // show reward list
				for (ArcadeRewards arcadetab : arcadeRewards)
					writeC(arcadetab.getArcadeRewardItems().size());

				for (ArcadeRewards arcadetab : arcadeRewards) {
					for (ArcadeRewardItem arcadetabitem : arcadetab.getArcadeRewardItems()) {
						writeD(arcadetabitem.getItemId());
						writeQ(arcadetabitem.getNormalCount());
						writeQ(arcadetabitem.getFrenzyCount());
					}
				}
				break;
		}
	}
}

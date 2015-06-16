package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team2.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;

/**
 * @author Sarynth Supports PlayerGroup and PlayerAlliance movement updating.
 */
public final class TeamEffectUpdater extends AbstractIterativePeriodicTaskManager<Player> {

	private static final class SingletonHolder {

		private static final TeamEffectUpdater INSTANCE = new TeamEffectUpdater();
	}

	public static TeamEffectUpdater getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public TeamEffectUpdater() {
		super(500);
	}

	@Override
	protected void callTask(Player player) {
		if (player.isOnline()) {
			if (player.isInGroup2()) {
				PlayerGroupService.updateGroup(player, GroupEvent.MOVEMENT);
				//PlayerGroupService.updateGroup(player, GroupEvent.UPDATE_EFFECTS);
				PlayerGroupService.updateGroup(player, GroupEvent.MOVEMENT);
			}
			if (player.isInAlliance2()) {
				PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
				PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
				//PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.UNK);
				PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
			}
		}
		// Remove task from list. It will be re-added if player effect changes again.
		this.stopTask(player);
	}

	@Override
	protected String getCalledMethodName() {
		return "teamEffectUpdate()";
	}

}

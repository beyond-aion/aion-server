package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;

/**
 * @author Sarynth Supports PlayerGroup and PlayerAlliance movement updating.
 */
public final class TeamMoveUpdater extends AbstractIterativePeriodicTaskManager<Player> {

	private static final class SingletonHolder {

		private static final TeamMoveUpdater INSTANCE = new TeamMoveUpdater();
	}

	public static TeamMoveUpdater getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public TeamMoveUpdater() {
		super(2000);
	}

	@Override
	protected void callTask(Player player) {
		if (player.isInGroup()) {
			PlayerGroupService.updateGroup(player, GroupEvent.MOVEMENT);
		}
		if (player.isInAlliance()) {
			PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
		}

		// Remove task from list. It will be re-added if player moves again.
		this.stopTask(player);
	}

	@Override
	protected String getCalledMethodName() {
		return "teamMoveUpdate()";
	}

}

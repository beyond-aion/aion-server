package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;

/**
 * Supports PlayerGroup and PlayerAlliance movement updating.
 * 
 * @author Sarynth
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
		if (player.isOnline()) {
			if (player.isInGroup()) {
				PlayerGroupService.updateGroup(player, GroupEvent.MOVEMENT);
			} else if (player.isInAlliance()) {
				PlayerAllianceService.updateAlliance(player, PlayerAllianceEvent.MOVEMENT);
			}
		}
		this.stopTask(player); // task will be re-added on demand
	}

	@Override
	protected String getCalledMethodName() {
		return "teamMoveUpdate()";
	}

}

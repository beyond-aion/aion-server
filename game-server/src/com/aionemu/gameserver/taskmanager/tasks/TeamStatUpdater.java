package com.aionemu.gameserver.taskmanager.tasks;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * Supports PlayerGroup and PlayerAlliance stat updating.
 * 
 * @author Sarynth
 */
public final class TeamStatUpdater extends AbstractFIFOPeriodicTaskManager<Player> {

	private static final class SingletonHolder {

		private static final TeamStatUpdater INSTANCE = new TeamStatUpdater();
	}

	public static TeamStatUpdater getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public TeamStatUpdater() {
		super(500);
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
	}

	@Override
	protected String getCalledMethodName() {
		return "teamStatUpdate()";
	}

}

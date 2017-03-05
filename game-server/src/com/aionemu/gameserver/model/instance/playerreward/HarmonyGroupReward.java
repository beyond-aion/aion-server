package com.aionemu.gameserver.model.instance.playerreward;

import java.util.List;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author xTz
 */
public class HarmonyGroupReward extends PvPArenaPlayerReward {

	private List<AGPlayer> players;
	private int id;
	private AutoGroupType agt;

	public HarmonyGroupReward(int objectId, int timeBonus, byte buffId, List<AGPlayer> players, AutoGroupType agt) {
		super(objectId, timeBonus, buffId);
		this.players = players;
		id = IDFactory.getInstance().nextId();
		this.agt = agt;
	}

	public AutoGroupType getAgt() {
		return agt;
	}

	public List<AGPlayer> getAGPlayers() {
		return players;
	}

	public boolean containPlayer(int objectId) {
		for (AGPlayer agp : players) {
			if (agp.getObjectId() == objectId) {
				return true;
			}
		}
		return false;
	}

	public AGPlayer getAGPlayer(int objectId) {
		for (AGPlayer agp : players) {
			if (agp.getObjectId() == objectId) {
				return agp;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

}

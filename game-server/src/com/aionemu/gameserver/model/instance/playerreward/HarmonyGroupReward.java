package com.aionemu.gameserver.model.instance.playerreward;

import java.util.List;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 *
 * @author xTz
 */
public class HarmonyGroupReward extends PvPArenaPlayerReward {

	private List<AGPlayer> players;
	private int id;
        private AutoGroupType agt;
	public HarmonyGroupReward(Integer object, int timeBonus, byte buffId, List<AGPlayer> players,AutoGroupType agt) {
		super(object, timeBonus, buffId);
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

	public boolean containPlayer(Integer object) {
		for (AGPlayer agp : players) {
			if (agp.getObjectId().equals(object)) {
				return true;
			}
		}
		return false;
	}

	public AGPlayer getAGPlayer(Integer object) {
		for (AGPlayer agp : players) {
			if (agp.getObjectId().equals(object)) {
				return agp;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

}

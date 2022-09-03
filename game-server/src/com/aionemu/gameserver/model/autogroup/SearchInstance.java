package com.aionemu.gameserver.model.autogroup;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public class SearchInstance {

	private long registrationTime = System.currentTimeMillis();
	private int instanceMaskId;
	private EntryRequestType ert;
	private List<Integer> members;

	public SearchInstance(int instanceMaskId, EntryRequestType ert, Collection<Player> members) {
		this.instanceMaskId = instanceMaskId;
		this.ert = ert;
		if (members != null) {
			this.members = members.stream().map(AionObject::getObjectId).collect(Collectors.toList());
		}
	}

	public List<Integer> getMembers() {
		return members;
	}

	public int getInstanceMaskId() {
		return instanceMaskId;
	}

	public int getRemainingTime() {
		return (int) (System.currentTimeMillis() - registrationTime) / 1000 * 256;
	}

	public EntryRequestType getEntryRequestType() {
		return ert;
	}

	public boolean isDredgion() {
		return instanceMaskId == 1 || instanceMaskId == 2 || instanceMaskId == 3;
	}

	public boolean isKamarBattlefield() {
		return instanceMaskId == 107;
	}

	public boolean isEngulfedOphidanBridge() {
		return instanceMaskId == 108;
	}

	public boolean isIronWallWarfront() {
		return instanceMaskId == 109;
	}

	public boolean isIdgelDome() {
		return instanceMaskId == 111;
	}
}

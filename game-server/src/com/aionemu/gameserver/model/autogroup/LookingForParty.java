package com.aionemu.gameserver.model.autogroup;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public class LookingForParty implements Comparable<LookingForParty> {

	private final Map<Integer, AGPlayer> members;
	private final EntryRequestType ert;
	private final Race race;
	private final long registrationTime = System.currentTimeMillis();
	private final int maskId;
	private long startEnterTime;
	private int leaderObjId;

	public LookingForParty(Player player, EntryRequestType ert, int maskId) {
		this.members = createMembers(player);
		this.ert = ert;
		this.race = player.getRace();
		this.maskId = maskId;
		this.leaderObjId = player.getObjectId();
	}

	private Map<Integer, AGPlayer> createMembers(Player player) {
		if (player.isInTeam()) {
			return player.getCurrentTeam().getOnlineMembers().stream().map(AGPlayer::new)
				.collect(Collectors.toMap(AGPlayer::objectId, Function.identity()));
		}
		return new HashMap<>(Map.of(player.getObjectId(), new AGPlayer(player)));
	}

	public Map<Integer, AGPlayer> getMembers() {
		return members;
	}

	public boolean isMember(int objectId) {
		return members.get(objectId) != null;
	}

	public void unregisterMember(Integer objectId) {
		members.remove(objectId);
	}

	public EntryRequestType getEntryRequestType() {
		return ert;
	}

	public Race getRace() {
		return race;
	}

	public long getRegistrationTime() {
		return registrationTime;
	}

	public int getMaskId() {
		return maskId;
	}

	public int getLeaderObjId() {
		return leaderObjId;
	}

	public void setLeaderObjId(int leaderObjId) {
		this.leaderObjId = leaderObjId;
	}

	public boolean isLeader(int objectId) {
		return objectId == leaderObjId;
	}

	public void setStartEnterTime() {
		startEnterTime = System.currentTimeMillis();
	}

	public boolean isOnStartEnterTask() {
		return System.currentTimeMillis() - startEnterTime <= 120000;
	}

	@Override
	public int compareTo(LookingForParty lfp) {
		if (ert != lfp.ert)
			return lfp.ert.ordinal() - ert.ordinal();

		int memberDiff = lfp.getMembers().size() - members.size();
		if (memberDiff != 0)
			return memberDiff;

		return (int) (registrationTime - lfp.getRegistrationTime());
	}
}

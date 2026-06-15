package com.aionemu.gameserver.model.team;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public abstract class GeneralTeam<M extends AionObject, TM extends TeamMember<M>> extends AionObject {

	private final static Logger log = LoggerFactory.getLogger(GeneralTeam.class);
	protected final Map<Integer, TM> members = new ConcurrentHashMap<>();
	protected final Lock teamLock = new ReentrantLock();
	private TM leader;

	public GeneralTeam(int objId, boolean autoReleaseObjectId) {
		super(objId, autoReleaseObjectId);
	}

	public final void onEvent(TeamEvent event) {
		lock();
		try {
			if (event.checkCondition()) {
				event.handleEvent();
			} else {
				log.warn("[TEAM] skipped event: {} group: {}", event, this);
			}
		} finally {
			unlock();
		}
	}

	public final TM getMember(int objectId) {
		return members.get(objectId);
	}

	public final boolean hasMember(int objectId) {
		return members.get(objectId) != null;
	}

	public void addMember(TM member) {
		Objects.requireNonNull(member, "Team member should be not null");
		if (members.put(member.getObjectId(), member) != null)
			throw new IllegalStateException("Team member is already added");
	}

	public final TM removeMember(TM member) {
		Objects.requireNonNull(member, "Team member should be not null");
		return removeMember(member.getObjectId());
	}

	public final TM removeMember(int objectId) {
		TM removedMember = members.remove(objectId);
		if (removedMember == null)
			throw new IllegalStateException("Team member is already removed");
		onRemoveMember(removedMember);
		return removedMember;
	}

	protected abstract void onRemoveMember(TM member);

	/**
	 * Apply some function on all team members<br>
	 * Should be used only to change state of the group or its members
	 */
	public void forEachTeamMember(Consumer<TM> consumer) {
		lock();
		try {
			for (TM member : members.values()) {
				consumer.accept(member);
			}
		} finally {
			unlock();
		}
	}

	/**
	 * Apply some function on all team member's objects<br>
	 * Should be used only to change state of the group or its members
	 */
	public void forEach(Consumer<M> consumer) {
		lock();
		try {
			for (TM member : members.values())
				consumer.accept(member.getObject());
		} finally {
			unlock();
		}
	}

	/**
	 * Apply some function on all team member's objects, until the function returns false<br>
	 * Should be used only to change state of the group or its members
	 */
	public void applyOnMembers(Function<M, Boolean> function) {
		lock();
		try {
			for (TM member : members.values()) {
				if (!function.apply(member.getObject())) {
					return;
				}
			}
		} finally {
			unlock();
		}
	}

	public List<TM> filter(Predicate<TM> predicate) {
		return members.values().stream().filter(predicate).collect(Collectors.toList());
	}

	public List<M> filterMembers(Predicate<M> predicate) {
		return members.values().stream().map(TeamMember::getObject).filter(predicate).collect(Collectors.toList());
	}

	public List<M> getMembers() {
		return members.values().stream().map(TeamMember::getObject).collect(Collectors.toList());
	}

	public int size() {
		return members.size();
	}

	public final boolean isDisbanded() {
		return size() == 0;
	}

	public final boolean shouldDisband() {
		return size() == 1; // teams always contain at least two members
	}

	public final boolean isFull() {
		return size() == getMaxMemberCount();
	}

	public final int getTeamId() {
		return getObjectId();
	}

	@Override
	public String getName() {
		return "Leader: " + leader.getObject();
	}

	public final TM getLeader() {
		return leader;
	}

	public final M getLeaderObject() {
		return leader.getObject();
	}

	public final boolean isLeader(M member) {
		return leader.getObject().equals(member);
	}

	public final void changeLeader(TM member) {
		Objects.requireNonNull(leader, "Leader should already be set");
		Objects.requireNonNull(member, "New leader should not be null");
		if (leader.equals(member))
			throw new IllegalArgumentException(member + " is already the team leader");
		this.leader = member;
	}

	protected final void setLeader(TM member) {
		if (leader != null)
			throw new IllegalStateException("Leader should be not initialized");
		Objects.requireNonNull(member, "Leader should not be null");
		this.leader = member;
	}

	protected final void lock() {
		teamLock.lock();
	}

	protected final void unlock() {
		teamLock.unlock();
	}

	public abstract Race getRace();

	public abstract int getMaxMemberCount();

	public abstract List<Player> getOnlineMembers();

	public abstract LootGroupRules getLootGroupRules();

	public abstract void sendPackets(AionServerPacket... packets);

	public abstract void sendPacket(Predicate<M> predicate, AionServerPacket... packets);

}

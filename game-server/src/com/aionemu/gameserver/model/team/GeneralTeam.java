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

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.google.common.base.Preconditions;

/**
 * @author ATracer
 */
public abstract class GeneralTeam<M extends AionObject, TM extends TeamMember<M>> extends AionObject implements Team<M, TM> {

	private final static Logger log = LoggerFactory.getLogger(GeneralTeam.class);
	protected final Map<Integer, TM> members = new ConcurrentHashMap<>();
	protected final Lock teamLock = new ReentrantLock();
	private TM leader;

	public GeneralTeam(int objId) {
		super(objId);
	}

	@Override
	public void onEvent(TeamEvent event) {
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

	@Override
	public TM getMember(int objectId) {
		return members.get(objectId);
	}

	@Override
	public boolean hasMember(int objectId) {
		return members.get(objectId) != null;
	}

	@Override
	public void addMember(TM member) {
		Objects.requireNonNull(member, "Team member should be not null");
		Preconditions.checkState(members.get(member.getObjectId()) == null, "Team member is already added");
		members.put(member.getObjectId(), member);
	}

	@Override
	public void removeMember(TM member) {
		Objects.requireNonNull(member, "Team member should be not null");
		Preconditions.checkState(members.get(member.getObjectId()) != null, "Team member is already removed");
		members.remove(member.getObjectId());
	}

	@Override
	public final void removeMember(int objectId) {
		removeMember(members.get(objectId));
	}

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

	@Override
	public List<TM> filter(Predicate<TM> predicate) {
		return members.values().stream().filter(predicate).collect(Collectors.toList());
	}

	@Override
	public List<M> filterMembers(Predicate<M> predicate) {
		return members.values().stream().map(tm -> tm.getObject()).filter(predicate).collect(Collectors.toList());
	}

	@Override
	public List<M> getMembers() {
		return members.values().stream().map(tm -> tm.getObject()).collect(Collectors.toList());
	}

	@Override
	public int size() {
		return members.size();
	}

	@Override
	public final int getTeamId() {
		return getObjectId();
	}

	@Override
	public String getName() {
		return GeneralTeam.class.getName();
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
		this.leader = member;
	}

	protected final void setLeader(TM member) {
		Preconditions.checkState(leader == null, "Leader should be not initialized");
		Objects.requireNonNull(member, "Leader should not be null");
		this.leader = member;
	}

	protected final void lock() {
		teamLock.lock();
	}

	protected final void unlock() {
		teamLock.unlock();
	}

}

package com.aionemu.gameserver.model.team;

import java.util.Collection;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public interface Team<M, TM extends TeamMember<M>> {

	Integer getTeamId();

	TM getMember(Integer objectId);

	boolean hasMember(Integer objectId);

	void addMember(TM member);

	void removeMember(TM member);

	void removeMember(Integer objectId);

	Collection<M> getMembers();

	Collection<M> getOnlineMembers();

	void onEvent(TeamEvent event);

	Collection<TM> filter(Predicate<TM> predicate);

	Collection<M> filterMembers(Predicate<M> predicate);

	void sendPacket(AionServerPacket packet);

	void sendPacket(AionServerPacket packet, Predicate<M> predicate);

	int onlineMembers();

	Race getRace();

	int size();

	boolean isFull();

}

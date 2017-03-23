package com.aionemu.gameserver.model.team;

import java.util.List;
import java.util.function.Predicate;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public interface Team<M, TM extends TeamMember<M>> {

	int getTeamId();

	TM getMember(int objectId);

	boolean hasMember(int objectId);

	void addMember(TM member);

	void removeMember(TM member);

	void removeMember(int objectId);

	List<M> getMembers();

	List<M> getOnlineMembers();

	void onEvent(TeamEvent event);

	List<TM> filter(Predicate<TM> predicate);

	List<M> filterMembers(Predicate<M> predicate);

	void sendPackets(AionServerPacket... packets);

	void sendPacket(Predicate<M> predicate, AionServerPacket... packets);

	int onlineMembers();

	Race getRace();

	int size();

	boolean isFull();

}

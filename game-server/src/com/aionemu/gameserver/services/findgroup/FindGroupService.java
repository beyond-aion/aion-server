package com.aionemu.gameserver.services.findgroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.FindGroup;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIND_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Find Group Service
 * 
 * @author cura, MrPoke
 */
public class FindGroupService {

	private ConcurrentHashMap<Integer, FindGroup> elyosRecruitFindGroups = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, FindGroup> elyosApplyFindGroups = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, FindGroup> asmodianRecruitFindGroups = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, FindGroup> asmodianApplyFindGroups = new ConcurrentHashMap<>();

	private FindGroupService() {
	}

	public void addFindGroupList(Player player, int action, String message, int groupType) {
		AionObject object = null;
		if (player.isInTeam()) {
			object = player.getCurrentTeam();
		} else {
			object = player;
		}

		FindGroup findGroup = new FindGroup(object, message, groupType);
		int objectId = object.getObjectId();
		switch (player.getRace()) {
			case ELYOS:
				switch (action) {
					case 0x02:
						elyosRecruitFindGroups.put(objectId, findGroup);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_MATCH_OFFER_PARTY_POSTED());
						break;
					case 0x06:
						elyosApplyFindGroups.put(objectId, findGroup);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_MATCH_SEEK_PARTY_POSTED());
						break;
				}
				break;
			case ASMODIANS:
				switch (action) {
					case 0x02:
						asmodianRecruitFindGroups.put(objectId, findGroup);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_MATCH_OFFER_PARTY_POSTED());
						break;
					case 0x06:
						asmodianApplyFindGroups.put(objectId, findGroup);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_MATCH_SEEK_PARTY_POSTED());
						break;
				}
				break;
		}

		Collection<FindGroup> findGroupList = new ArrayList<>();
		findGroupList.add(findGroup);

		PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, ((int) (System.currentTimeMillis() / 1000)), findGroupList));
	}

	public void updateFindGroupList(Player player, String message, int objectId) {
		FindGroup findGroup = null;

		switch (player.getRace()) {
			case ELYOS:
				findGroup = elyosRecruitFindGroups.get(objectId);
				findGroup.setMessage(message);
				break;
			case ASMODIANS:
				findGroup = asmodianRecruitFindGroups.get(objectId);
				findGroup.setMessage(message);
				break;
		}
	}

	public Collection<FindGroup> getFindGroups(Race race, int action) {
		switch (race) {
			case ELYOS:
				switch (action) {
					case 0x00:
						return elyosRecruitFindGroups.values();
					case 0x04:
						return elyosApplyFindGroups.values();
					case 0xA:
						return Collections.emptyList();
				}
				break;
			case ASMODIANS:
				switch (action) {
					case 0x00:
						return asmodianRecruitFindGroups.values();
					case 0x04:
						return asmodianApplyFindGroups.values();
					case 0xA:
						return Collections.emptyList();
				}
				break;
		}
		return null;
	}

	public void registerInstanceGroup(Player player, int action, int instanceId, String message, int minMembers, int groupType) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(instanceId);
		if (agt != null) {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceId, 1, 0, player.getName()));
		}
	}

	public void sendFindGroups(Player player, int action) {
		PacketSendUtility.sendPacket(player,
			new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), getFindGroups(player.getRace(), action)));
	}

	public FindGroup removeFindGroup(final Race race, int action, int playerObjId) {
		FindGroup findGroup = null;
		switch (race) {
			case ELYOS:
				switch (action) {
					case 0x00:
						findGroup = elyosRecruitFindGroups.remove(playerObjId);
						break;
					case 0x04:
						findGroup = elyosApplyFindGroups.remove(playerObjId);
						break;
				}
				break;
			case ASMODIANS:
				switch (action) {
					case 0x00:
						findGroup = asmodianRecruitFindGroups.remove(playerObjId);
						break;
					case 0x04:
						findGroup = asmodianApplyFindGroups.remove(playerObjId);
						break;
				}
				break;
		}
		if (findGroup != null) {
			byte serverId = (byte) NetworkConfig.GAMESERVER_ID;
			byte unk1 = 0;
			byte unk2 = 0;
			byte unk3 = 0;
			if (findGroup.getObject() instanceof Player) {
				unk1 = 0;
				unk2 = 0;
				unk3 = 16;
			}
			PacketSendUtility.broadcastToWorld(new SM_FIND_GROUP(action + 1, playerObjId, serverId, unk1, unk2, unk3), p -> race == p.getRace());
		}
		return findGroup;
	}

	public void clean() {
		cleanMap(elyosRecruitFindGroups, Race.ELYOS, 0x00);
		cleanMap(elyosApplyFindGroups, Race.ELYOS, 0x04);
		cleanMap(asmodianRecruitFindGroups, Race.ASMODIANS, 0x00);
		cleanMap(asmodianApplyFindGroups, Race.ASMODIANS, 0x04);
	}

	private void cleanMap(ConcurrentHashMap<Integer, FindGroup> map, Race race, int action) {
		for (FindGroup group : map.values()) {
			if (group.getLastUpdate() + 60 * 60 < System.currentTimeMillis() / 1000)
				removeFindGroup(race, action, group.getObjectId());
		}
	}

	public static final FindGroupService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final FindGroupService instance = new FindGroupService();
	}

}

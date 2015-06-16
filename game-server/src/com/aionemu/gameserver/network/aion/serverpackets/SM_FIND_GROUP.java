package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.FindGroup;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author cura, MrPoke
 */
public class SM_FIND_GROUP extends AionServerPacket {

	private int action;
	private int lastUpdate;
	private Collection<FindGroup> findGroups;
	private int groupSize;
	private byte serverId, unk1, unk2, unk3;
	private int instanceId;

	public SM_FIND_GROUP(int action, int lastUpdate, Collection<FindGroup> findGroups) {
		this.lastUpdate = lastUpdate;
		this.action = action;
		this.findGroups = findGroups;
		this.groupSize = findGroups.size();
	}

	public SM_FIND_GROUP(int action, int lastUpdate, byte serverId, byte unk1, byte unk2, byte unk3) {
		this.action = action;
		this.lastUpdate = lastUpdate;
		this.serverId = serverId;
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
	}

	public SM_FIND_GROUP(int action, int instanceId) {
		this.action = action;
		this.instanceId = instanceId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		switch (action) {
			case 0x00:
			case 0x02:
				writeH(groupSize); // groupSize
				writeH(groupSize); // groupSize
				writeD(lastUpdate); // objId?
				for (FindGroup findGroup : findGroups) {
					writeD(findGroup.getObjectId()); // player object id
					writeC(NetworkConfig.GAMESERVER_ID);
					if (findGroup.getObject() instanceof Player) {
						writeC(0); // unk to do
						writeC(0); // unk to do
						writeC(16); // solo
					}
					else {
						writeC(0); // unk to do
						writeC(0); // unk to do
						writeC(0); // group | alliance
					}
					writeC(findGroup.getGroupType()); // 0:group, 1:alliance
					writeS(findGroup.getMessage()); // text
					writeS(findGroup.getName()); // writer name
					writeC(findGroup.getSize()); // members count
					writeC(findGroup.getMinLevel()); // members																																																					// level
					writeC(findGroup.getMaxLevel()); // members																																																						// level
					writeD(findGroup.getLastUpdate()); // objId?
				}
				break;
			case 0x01:
			case 0x03:
				writeD(lastUpdate); // player object id
				writeC(serverId);
				writeC(unk1);
				writeC(unk2);
				writeC(unk3);
				break;
			case 0x04:
			case 0x06:
				writeH(groupSize); // groupSize
				writeH(groupSize); // groupSize
				writeD(lastUpdate); // objId?
				for (FindGroup findGroup : findGroups) {
					writeD(findGroup.getObjectId()); // player object id
					writeC(findGroup.getGroupType()); // 0:group, 1:alliance
					writeS(findGroup.getMessage()); // text
					writeS(findGroup.getName()); // writer name
					writeC(findGroup.getClassId()); // player class id
					writeC(findGroup.getMinLevel()); // player level
					writeD(findGroup.getLastUpdate()); // objId?
				}
				break;
			case 0x05:
				writeD(lastUpdate); // player object id
				break;
			////////////// 4.0 Instance GroupSystem //////////////
			case 0x0A: //registered Groups
				writeH(groupSize);//size
				writeH(groupSize);//size
				writeD(lastUpdate);
				for (FindGroup findGroup : findGroups) {
					writeD(0);//groupregisteredId
					writeD(findGroup.getInstanceId());//instanceId
					writeD(1);//unk
					writeC(findGroup.getSize());//currentMembers
					writeC(findGroup.getMinMembers());//minMembers
					writeH(0);//unk maybe spacer
					writeD(findGroup.getObjectId());//playerObjId
					writeD(1);//unk
					writeD(0);//unk
					writeC(findGroup.getMinLevel());//playerLevel
					writeC(findGroup.getMaxLevel());//playerLevel
					writeH(0);//unk maybe spacer?
					writeD(findGroup.getLastUpdate());//lastUpdate
					writeD(0);//unk
					writeS(findGroup.getName());//writerName
					writeS(findGroup.getMessage());//Message
				}
				break;
			case 0x0E: //register new InstanceGroup
				writeC(1);//packetNumber 0 || 1 || 2 
				for (FindGroup findGroup : findGroups) {
					writeD(0);//entryId? counts forwards every entry
					writeD(findGroup.getInstanceId());//instanceId
					writeD(1);//position?	
					writeC(findGroup.getSize());//Maybe Members in Group?
					writeC(findGroup.getMinMembers());//min members to enter Instance(writer choose it)
					writeH(0);//unk maybe spacer
					writeD(findGroup.getObjectId());//playerObjId leader ID?
					writeC(1);//unk
					writeC(0);//unkGroupType?
					writeD(1);//unk
					writeH(0);//unk
					writeC(findGroup.getMinLevel());//player level
					writeC(findGroup.getMaxLevel());//player level
					writeH(0);//unk
					writeD(findGroup.getLastUpdate());//timestamp
					writeD(0);//unk
					writeS(findGroup.getName());//writer name
					writeS(findGroup.getMessage());//register message
				}
				break;
			case 0x10:
				writeH(groupSize);//size
				writeH(groupSize);//size
				writeD(lastUpdate);//systemcurrentimemillis
				for (FindGroup findGroup : findGroups) {
					writeD(0);//groupId?
					writeD(findGroup.getInstanceId());//instanceId
					writeD(findGroup.getObjectId());//playerObjId
					writeD(findGroup.getMinLevel());//playerLevel
					writeD(1);//unk
					writeH(1);//unk
					writeC(findGroup.getGroupType());//groupType?
					writeC(findGroup.getClassId());//classId?
					writeS(findGroup.getName());//writerName
				}
			case 0x16:
				writeD(0);//GroupEntryId
				writeD(0);//instanceId
				break;
			case 0x18:
				writeD(0);//GroupObjId
				writeD(0);//instanceId
				writeC(0);//classId?
				for (FindGroup findGroup : findGroups) {
					writeD(0);//GroupRegisteredId
					writeD(findGroup.getInstanceId());//instanceId
					writeD(findGroup.getObjectId());//playerObjId
					writeD(findGroup.getMinLevel());//playerLevel
					writeD(1);//unk
					writeH(1);//unk
					writeC(findGroup.getGroupType());//groupType?
					writeC(findGroup.getClassId());//classId?
					writeS(findGroup.getName());//writerName
				}
				break;
			case 0x1A:
				writeH(1);//unk
				writeD(instanceId);
				break;
		}
	}

}

package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIND_GROUP;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author cura, MrPoke
 */
public class CM_FIND_GROUP extends AionClientPacket {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_FIND_GROUP.class);

	private int action;
	private int playerObjId;
	private String message;
	private int groupType;
	@SuppressWarnings("unused")
	private int classId;
	@SuppressWarnings("unused")
	private int level;
	private byte serverId;
	private byte unk1;
	private byte unk2;
	private byte unk3;
	private int instanceId;
	private int minMembers;

	public CM_FIND_GROUP(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readUC();

		switch (action) {
			case 0x00: // recruit list
				break;
			case 0x01: // offer delete
				playerObjId = readD();
				serverId = readC();
				unk1 = readC();
				unk2 = readC();
				unk3 = readC();
				break;
			case 0x02: // send offer
				playerObjId = readD();
				message = readS();
				groupType = readUC();
				break;
			case 0x03: // recruit update
				playerObjId = readD();
				serverId = readC();
				unk1 = readC();
				unk2 = readC();
				unk3 = readC();
				message = readS();
				groupType = readUC();
				break;
			case 0x04: // apply list
				break;
			case 0x05: // post delete
				playerObjId = readD();
				break;
			case 0x06: // apply create
				playerObjId = readD();
				message = readS();
				groupType = readUC();
				classId = readUC();
				level = readUC();
				break;
			case 0x07: // apply update
				// TODO need packet check
				break;
			case 0x08: // register InstanceGroup
				instanceId = readD();
				groupType = readUC();// need to be tested
				message = readS();// text
				minMembers = readUC();// minMembers chosen by writer
				break;
			case 0x0A: // New 4.0 Group Recruitment
				break;
			default:
				log.error("Unknown find group packet? 0x" + Integer.toHexString(action).toUpperCase());
				break;
		}
	}

	@Override
	protected void runImpl() {
		final Player player = this.getConnection().getActivePlayer();
		switch (action) {
			case 0x00:
			case 0x04:
				FindGroupService.getInstance().sendFindGroups(player, action);
				break;
			case 0x01:
			case 0x05:
				FindGroupService.getInstance().removeFindGroup(player.getRace(), action - 1, playerObjId);
				break;
			case 0x02:
			case 0x06:
				FindGroupService.getInstance().addFindGroupList(player, action, message, groupType);
				break;
			case 0x03:
				FindGroupService.getInstance().updateFindGroupList(player, message, playerObjId);
				break;
			case 0x08:
				FindGroupService.getInstance().registerInstanceGroup(player, 0x0E, instanceId, message, minMembers, groupType);
				break;
			case 0x0A: // search
				FindGroupService.getInstance().sendFindGroups(player, action);
				break;
			default:
				PacketSendUtility.sendPacket(player, new SM_FIND_GROUP(action, playerObjId, serverId, unk1, unk2, unk3));
				break;
		}
	}
}

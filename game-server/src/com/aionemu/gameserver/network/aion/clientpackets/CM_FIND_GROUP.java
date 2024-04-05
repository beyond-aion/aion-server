package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.findgroup.FindGroupService;

/**
 * @author cura, MrPoke
 */
public class CM_FIND_GROUP extends AionClientPacket {

	private int action;
	private int playerOrTeamId;
	private int bannedPlayerId;
	private String message;
	private int groupType;
	private int classId;
	private int level;
	private byte serverId;
	private byte unk1;
	private byte unk2;
	private byte unk3;
	private int instanceMaskId;
	private int minMembers;
	private byte instanceApplicationReply;

	public CM_FIND_GROUP(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = readUC();

		switch (action) {
			case 0: // recruit list
				break;
			case 1: // offer delete
				playerOrTeamId = readD();
				serverId = readC();
				unk1 = readC();
				unk2 = readC();
				unk3 = readC();
				break;
			case 2: // send offer
				playerOrTeamId = readD();
				message = readS();
				groupType = readUC();
				break;
			case 3: // recruit update
				playerOrTeamId = readD();
				serverId = readC();
				unk1 = readC();
				unk2 = readC();
				unk3 = readC();
				message = readS();
				groupType = readUC();
				break;
			case 4: // apply list
				break;
			case 5: // post delete
				playerOrTeamId = readD();
				break;
			case 6: // apply create
			case 7: // apply update
				playerOrTeamId = readD();
				message = readS();
				groupType = readUC();
				classId = readUC();
				level = readUC();
				break;
			case 8: // register InstanceGroup
				instanceMaskId = readD();
				readUC(); // unk 0
				message = readS();// text
				minMembers = readUC();// minMembers chosen by writer
				break;
			case 9: // remove instance group
				playerOrTeamId = readD();
				instanceMaskId = readD();
				break;
			case 10: // show instance groups
				break;
			case 11: // apply for instance group
				playerOrTeamId = readD();
				instanceMaskId = readD();
				break;
			case 12: // accept/deny instance group applicant
				playerOrTeamId = readD();
				instanceApplicationReply = readC(); // 1: accept, 0: deny
				break;
			case 13: // triggered every 50s when instance group tab is open or option "Automatic search when the window is closed" is checked
				break;
			case 15: // show instance group member info
				playerOrTeamId = readD();
				instanceMaskId = readD();
				break;
			case 17:
				playerOrTeamId = readD();
				instanceMaskId = readD();
				message = readS();
				break;
			case 20: // clicked Enter button in Prepare for entry window
				break;
			case 25: // ban from instance group
				playerOrTeamId = readD();
				instanceMaskId = readD();
				bannedPlayerId = readD();
				break;
			default:
				LoggerFactory.getLogger(CM_FIND_GROUP.class).warn("Unknown find group action " + action);
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		switch (action) {
			case 0 -> FindGroupService.getInstance().showRecruitments(player);
			case 1 -> FindGroupService.getInstance().removeRecruitment(player, serverId, unk1, unk2, unk3);
			case 2 -> FindGroupService.getInstance().addRecruitment(player, message, groupType);
			case 3 -> FindGroupService.getInstance().updateRecruitment(player, message, groupType);
			case 4 -> FindGroupService.getInstance().showApplications(player);
			case 5 -> FindGroupService.getInstance().removeApplication(player);
			case 6 -> FindGroupService.getInstance().addApplication(player, message, groupType, classId, level);
			case 7 -> FindGroupService.getInstance().updateApplication(player, message, groupType, classId, level);
			case 8 -> FindGroupService.getInstance().registerInstanceGroup(player, instanceMaskId, message, minMembers);
			case 9 -> FindGroupService.getInstance().removeInstanceGroup(player);
			case 10 -> FindGroupService.getInstance().showInstanceGroups(player, false);
			case 11 -> FindGroupService.getInstance().sendInstanceApplication(player, playerOrTeamId);
			case 12 -> FindGroupService.getInstance().sendInstanceApplicationResult(player, playerOrTeamId, instanceApplicationReply);
			case 13 -> FindGroupService.getInstance().showInstanceGroups(player, true);
			case 15 -> FindGroupService.getInstance().showInstanceGroupMembersInfo(player, playerOrTeamId);
			case 17 -> FindGroupService.getInstance().updateInstanceGroup(player, message);
		}
	}
}

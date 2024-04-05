package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.findGroup.FindGroupEntry;
import com.aionemu.gameserver.model.gameobjects.findGroup.GroupApplication;
import com.aionemu.gameserver.model.gameobjects.findGroup.GroupRecruitment;
import com.aionemu.gameserver.model.gameobjects.findGroup.ServerWideGroup;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author cura, MrPoke
 */
public class SM_FIND_GROUP extends AionServerPacket {

	private int action;
	private List<? extends FindGroupEntry> entries;
	private byte serverId, unk1, unk2, unk3;
	private int idToDelete;
	private Player instanceApplicant;
	private boolean showEnterInstanceMessage;
	private List<Integer> instanceMaskIds;

	public SM_FIND_GROUP(int action, List<? extends FindGroupEntry> entries) {
		this.action = action;
		this.entries = entries;
	}

	public SM_FIND_GROUP(int recruitmentIdToDelete, byte serverId, byte unk1, byte unk2, byte unk3) {
		this.action = 1;
		this.idToDelete = recruitmentIdToDelete;
		this.serverId = serverId;
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
	}

	public SM_FIND_GROUP(int applicationIdToDelete) {
		this.action = 5;
		this.idToDelete = applicationIdToDelete;
	}

	public SM_FIND_GROUP(Player instanceApplicant) {
		this.action = 11;
		this.instanceApplicant = instanceApplicant;
	}

	public SM_FIND_GROUP(boolean showEnterInstanceMessage) {
		this.action = 23;
		this.showEnterInstanceMessage = showEnterInstanceMessage;
	}

	public SM_FIND_GROUP(List<Integer> instanceMaskIds) {
		this.action = 26;
		this.instanceMaskIds = instanceMaskIds;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);
		switch (action) {
			case 0 -> showRecruitments((List<GroupRecruitment>) entries, (int) (System.currentTimeMillis() / 1000));
			case 1 -> removeRecruitment(idToDelete, serverId, unk1, unk2, unk3);
			case 4 -> showApplications((List<GroupApplication>) entries, (int) (System.currentTimeMillis() / 1000));
			case 5 -> removeApplication(idToDelete);
			case 10 -> showInstanceGroups((List<ServerWideGroup>) entries, (int) (System.currentTimeMillis() / 1000));
			case 11 -> sendInstanceGroupApplicationAsWhisperChatMessage(instanceApplicant);
			case 14 -> registerInstanceGroup((List<ServerWideGroup>) entries);
			case 16 -> showInstanceGroupMemberInfo((ServerWideGroup) entries.get(0), (int) (System.currentTimeMillis() / 1000));
			case 18 -> showEnterButtonInPrepareForEntryWindow((ServerWideGroup) entries.get(0)); // window must be initialized
			case 22 -> showPrepareForEntryWindow((ServerWideGroup) entries.get(0)); // initialize window if necessary
			case 23 -> destroyPrepareForEntryWindow((ServerWideGroup) entries.get(0), showEnterInstanceMessage);
			case 24 -> updatePrepareForEntryWindow((ServerWideGroup) entries.get(0));
			case 26 -> enableRegisterForInstances(instanceMaskIds);
		}
	}

	private void showRecruitments(List<GroupRecruitment> recruitments, int lastUpdate) {
		writeH(recruitments.size());
		writeH(recruitments.size());
		writeD(lastUpdate);
		for (GroupRecruitment recruitment : recruitments) {
			writeD(recruitment.getObjectId()); // team ID or recruiter ID if still solo
			writeC(NetworkConfig.GAMESERVER_ID);
			writeC(0); // unk (always 0)
			writeC(0); // unk (always 0)
			writeC(recruitment.getObject() instanceof Player ? 16 : 0); // 16: solo, 0: group | alliance
			writeC(recruitment.getGroupType()); // 0: group, 1: alliance, 2: mentor
			writeS(recruitment.getMessage()); // text
			writeS(recruitment.getName()); // recruiter name
			writeC(recruitment.getSize()); // members count
			writeC(recruitment.getMinLevel()); // members lowest level
			writeC(recruitment.getMaxLevel()); // members highest level
			writeD(recruitment.getLastUpdate()); // client hides entries older than two hours
		}
	}

	private void removeRecruitment(int playerOrTeamId, byte serverId, byte unk1, byte unk2, byte unk3) {
		writeD(playerOrTeamId);
		writeC(serverId);
		writeC(unk1); // unk (always 0)
		writeC(unk2); // unk (always 0)
		writeC(unk3); // 16: solo, 0: group | alliance
	}

	private void showApplications(List<GroupApplication> applications, int lastUpdate) {
		writeH(applications.size());
		writeH(applications.size());
		writeD(lastUpdate);
		for (GroupApplication application : applications) {
			writeD(application.getPlayer().getObjectId());
			writeC(application.getGroupType()); // 0:group, 1:alliance
			writeS(application.getMessage()); // text
			writeS(application.getPlayer().getName(true));
			writeC(application.getClassId()); // applied player class id
			writeC(application.getLevel()); // applied player level
			writeD(application.getLastUpdate()); // client hides entries older than two hours
		}
	}

	private void removeApplication(int playerId) {
		writeD(playerId);
	}

	private void showInstanceGroups(List<ServerWideGroup> instanceGroups, int lastUpdate) {
		writeH(instanceGroups.size());
		writeH(instanceGroups.size());
		writeD(lastUpdate);
		for (ServerWideGroup instanceGroup : instanceGroups) {
			writeD(instanceGroup.getId());// GroupEntryId
			writeD(instanceGroup.getInstanceMaskId());
			writeD(1);// unk
			writeC(instanceGroup.getMembers().size());
			writeC(instanceGroup.getMinMembers());
			writeH(0);// unk maybe spacer
			writeD(instanceGroup.getRecruiter().getObjectId());// playerObjId
			writeD(1);// unk
			writeD(0);// unk
			writeC(instanceGroup.getMinLevel());// playerLevel
			writeC(instanceGroup.getMaxLevel());// playerLevel
			writeH(0);// unk maybe spacer?
			writeD(instanceGroup.getLastUpdate());// lastUpdate
			writeD(0);// unk
			writeS(instanceGroup.getRecruiter().getName(true));
			writeS(instanceGroup.getMessage());// Message
		}
	}

	private void sendInstanceGroupApplicationAsWhisperChatMessage(Player instanceApplicant) {
		writeD(instanceApplicant.getObjectId());
		writeD(0);
		writeD(0);
		writeH(0);
		writeC(0);
		writeC(instanceApplicant.getPlayerClass().getClassId());
		writeD(instanceApplicant.getLevel());
		writeS(instanceApplicant.getName(true));
	}

	private void registerInstanceGroup(List<ServerWideGroup> instanceGroups) {
		writeC(1);// packetNumber 0 || 1 || 2
		for (ServerWideGroup instanceGroup : instanceGroups) {
			writeD(instanceGroup.getId());// GroupEntryId (counts forwards every entry)
			writeD(instanceGroup.getInstanceMaskId());
			writeD(1);// position?
			writeC(instanceGroup.getMembers().size());
			writeC(instanceGroup.getMinMembers());// min members to enter Instance(writer choose it)
			writeH(0);// unk maybe spacer
			writeD(instanceGroup.getRecruiter().getObjectId());// playerObjId leader ID?
			writeC(1);// unk
			writeC(0);// unkGroupType?
			writeD(1);// unk
			writeH(0);// unk
			writeC(instanceGroup.getMinLevel());
			writeC(instanceGroup.getMaxLevel());
			writeH(0);// unk
			writeD(instanceGroup.getLastUpdate());// timestamp
			writeD(0);// unk
			writeS(instanceGroup.getRecruiter().getName(true));
			writeS(instanceGroup.getMessage());
		}
	}

	private void showInstanceGroupMemberInfo(ServerWideGroup instanceGroup, int lastUpdate) {
		List<Player> members = instanceGroup.getMembers();
		writeH(members.size());
		writeH(members.size());
		writeD(lastUpdate);
		for (Player member : members) {
			writeD(0);// groupId?
			writeD(member.getWorldId());
			writeD(member.getObjectId());
			writeD(member.getLevel());
			writeD(member.getPlayerClass().getClassId());
			writeH(1);// unk
			writeC(0);// groupType?
			writeC(0);// unk
			writeS(member.getName(true));
		}
	}

	private void showEnterButtonInPrepareForEntryWindow(ServerWideGroup instanceGroup) {
		writeD(instanceGroup.getId()); // GroupEntryId
		writeD(instanceGroup.getInstanceMaskId());
	}

	private void showPrepareForEntryWindow(ServerWideGroup instanceGroup) {
		writeD(instanceGroup.getId()); // GroupEntryId
		writeD(instanceGroup.getInstanceMaskId());
	}

	private void destroyPrepareForEntryWindow(ServerWideGroup instanceGroup, boolean showEnterInstanceMessage) {
		writeD(instanceGroup.getId()); // GroupEntryId
		writeD(instanceGroup.getInstanceMaskId());
		writeC(showEnterInstanceMessage ? 1 : 0);
	}

	private void updatePrepareForEntryWindow(ServerWideGroup instanceGroup) {
		List<Player> instanceGroupMembers = instanceGroup.getMembers();
		writeD(instanceGroup.getId()); // GroupEntryId
		writeD(instanceGroup.getInstanceMaskId());
		writeC(instanceGroupMembers.size());
		for (Player member : instanceGroupMembers) {
			writeD(0); // server ID?
			writeD(0); // server ID?
			writeD(member.getObjectId());
			writeD(member.getLevel());
			writeD(member.getPlayerClass().getClassId());
			writeH(0); // ?
			writeC(1); // 0: Preparing, 1: Ready
			writeC(member.isOnline() ? 1 : 0);
			writeS(member.getName(true));
		}
	}

	private void enableRegisterForInstances(List<Integer> instanceMaskIds) {
		writeH(instanceMaskIds.size());
		for (Integer instanceMaskId : instanceMaskIds)
			writeD(instanceMaskId);
	}
}

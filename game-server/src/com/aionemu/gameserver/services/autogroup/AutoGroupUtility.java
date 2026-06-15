package com.aionemu.gameserver.services.autogroup;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.model.autogroup.LookingForParty;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.instance.PvPArenaService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Estrayl
 */
public class AutoGroupUtility {

	public static boolean canRegisterNewEntry(Player player, AutoGroupType agt) {
		if (!agt.getTemplate().canRegisterNewEntry())
			return false;
		if (player.isInTeam()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_NOT_LEADER());
			return false;
		}
		return true;
	}

	public static boolean canRegisterQuickEntry(Player player, AutoGroupType agt) {
		if (!agt.getTemplate().canRegisterQuickEntry())
			return false;
		if (player.isInTeam()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_NOT_LEADER());
			return false;
		}
		return true;
	}

	public static boolean canRegisterGroupEntry(Player player, AutoGroupType agt, int mapId, int maskId) {
		return agt.getTemplate().hasRegisterGroup() && checkGroupRequirements(player, agt, mapId, maskId);
	}

	public static boolean checkGroupRequirements(Player player, AutoGroupType agt, int mapId, int maskId) {
		TemporaryPlayerTeam<?> team = player.getCurrentTeam();
		if (team == null || !team.isLeader(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_NOT_LEADER());
			return false;
		}
		if (agt.isPeriodicInstance()) {
			int maxMemberPerTeam = DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(agt.getTemplate().getInstanceMapId(), player.getRace());
			if (team.size() > maxMemberPerTeam) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_TOO_MANY_MEMBERS(maxMemberPerTeam, mapId));
				return false;
			}
		} else if (agt.isHarmonyArena() || agt.isTrainingHarmonyArena()) {
			if (team.size() > 3) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_TOO_MANY_MEMBERS(3, mapId));
				return false;
			}
		}

		for (Player member : team.getMembers()) {
			if (team.getLeaderObject().equals(member)) {
				continue;
			}
			if (agt.isHarmonyArena() && !PvPArenaService.checkItem(member, agt)) {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_ENTER_WITHOUT_ITEM());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
				return false;
			}
			if (hasCoolDown(member, mapId) || !agt.isInLvlRange(member.getLevel()) || AutoGroupService.getInstance().isSearching(member, maskId)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_MEMBER(member.getName()));
				return false;
			}
		}
		return true;
	}

	public static void sendSuccessfulRegistration(LookingForParty lfp, String leaderName, AutoGroupType agt, int maskId) {
		for (int objectId : lfp.getMembers().keySet()) {
			Player player = World.getInstance().getPlayer(objectId);
			if (player != null) {
				if (agt.isPeriodicInstance())
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, 6, true));
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_REGISTER_SUCCESS());
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, 1, lfp.getEntryRequestType().getId(), leaderName));
			}
		}
	}

	public static void sendWindowToPlayerIfOnline(int objectId, int maskId, int windowId) {
		Player player = World.getInstance().getPlayer(objectId);
		if (player != null)
			sendWindowToPlayer(player, maskId, windowId);
	}

	public static void sendWindowToPlayer(Player player, int maskId, int windowId) {
		PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, windowId));
	}

	public static boolean hasCoolDown(Player player, int worldId) {
		return player.getPortalCooldownList().isPortalUseDisabled(worldId);
	}
}

package com.aionemu.gameserver.services.teleport;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.team.GeneralTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
import com.aionemu.gameserver.model.templates.portal.ItemReq;
import com.aionemu.gameserver.model.templates.portal.PortalLoc;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.model.templates.portal.QuestReq;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author ATracer, xTz
 */
public class PortalService {

	private static Logger log = LoggerFactory.getLogger(PortalService.class);

	public static void port(PortalPath portalPath, Player player, Npc npc) {
		port(portalPath, player, npc, (byte) 0);
	}

	public static void port(PortalPath portalPath, Player player, Npc npc, byte difficult) {
		PortalLoc loc = DataManager.PORTAL_LOC_DATA.getPortalLoc(portalPath.getLocId());
		if (loc == null) {
			log.warn("No portal loc for locId " + portalPath.getLocId());
			return;
		}

		boolean instanceGroupReq = true;
		int mapId = loc.getWorldId();
		int playerSize = portalPath.getPlayerCount();
		boolean isInstance = portalPath.isInstance();

		if (!player.hasAccess(AdminConfig.INSTANCE_ENTER_ALL)) {
			if (playerSize > 1)
				instanceGroupReq = !player.hasPermission(MembershipConfig.INSTANCES_GROUP_REQ);

			if (!checkMentor(player, mapId))
				return;
			if (!checkRace(player, npc, portalPath))
				return;
			if (!checkRank(player, npc, portalPath))
				return;
			if (!checkTitle(player, npc, portalPath))
				return;
			if (!checkQuests(player, npc, portalPath))
				return;
			if (instanceGroupReq && !checkPlayerSize(player, npc, portalPath)) {
				return;
			}
		}

		WorldMapInstance instance = null;
		switch (playerSize) {
			case 0:
				log.warn("Tried to enter instance with player limit 0!");
				return;
			case 1: // solo
				instance = InstanceService.getRegisteredInstance(mapId, player.getObjectId());
				break;
			case 3:
			case 6: // group
				if (player.getPlayerGroup() != null) {
					instance = InstanceService.getRegisteredInstance(mapId, player.getPlayerGroup().getTeamId());
				}
				break;
			default: // alliance
				if (player.isInAlliance()) {
					if (player.isInLeague()) {
						instance = InstanceService.getRegisteredInstance(mapId, player.getPlayerAlliance().getLeague().getObjectId());
					} else {
						instance = InstanceService.getRegisteredInstance(mapId, player.getPlayerAlliance().getObjectId());
					}
				}
				break;
		}

		boolean reenter = false;
		if (instance == null || !instance.isRegistered(player.getObjectId())) {
			if (player.getPortalCooldownList().isPortalUseDisabled(mapId)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME());
				return;
			}
		} else if (player.getWorldId() != mapId || player.getInstanceId() != instance.getInstanceId()) {
			reenter = true;
		}

		if (!reenter) {
			if (!checkEnterLevel(player, npc, portalPath)) {
				return;
			}
			if (!checkAndRemoveRequiredItems(player, npc, portalPath)) {
				return;
			}
			if (mapId == player.getWorldId()) { // teleport within this instance
				TeleportService.teleportTo(player, mapId, player.getInstanceId(), loc.getX(), loc.getY(), loc.getZ(), loc.getH());
				return;
			}
		}

		PlayerGroup group = player.getPlayerGroup();
		switch (playerSize) {
			case 1:
				// If there is a group (whatever group requirement exists or not)...
				if (group != null && instanceGroupReq) {
					instance = InstanceService.getRegisteredInstance(mapId, group.getTeamId());
				}
				// But if there is no group, go to solo
				else {
					instance = InstanceService.getRegisteredInstance(mapId, player.getObjectId());
				}

				// No group instance, group on and default requirement off
				if (instance == null && group != null && instanceGroupReq) {
					// For each player from group
					for (Player member : group.getMembers()) {
						// Get his instance
						instance = InstanceService.getRegisteredInstance(mapId, member.getObjectId());

						// If some player is soloing and I found no one else yet, I get his instance
						if (instance != null) {
							break;
						}
					}

					// No solo instance found
					if (instance == null && isInstance)
						instance = registerTeam(group, mapId, playerSize, difficult);
				}

				// if already registered - just teleport
				if (instance != null) {
					if (mapId != player.getWorldId()) {
						transfer(player, loc, instance, reenter);
						return;
					}
				}
				port(player, loc, reenter, isInstance);
				break;
			case 3:
			case 6:
				if (group != null || !instanceGroupReq) {
					instance = InstanceService.getRegisteredInstance(mapId, group != null ? group.getTeamId() : player.getObjectId());

					// No instance (for group), group on and default requirement off
					if (instance == null && group != null && !instanceGroupReq) {
						// For each player from group
						for (Player member : group.getMembers()) {
							// Get his instance
							instance = InstanceService.getRegisteredInstance(mapId, member.getObjectId());

							// If some player is soloing and I found no one else yet, I get his instance
							if (instance != null) {
								break;
							}
						}

						// No solo instance found
						if (instance == null)
							instance = registerTeam(group, mapId, playerSize, difficult);
					}
					// No instance and default requirement on = Group on
					else if (instance == null && instanceGroupReq) {
						instance = registerTeam(group, mapId, playerSize, difficult);
					}
					// No instance, default requirement off, no group = Register new instance with player ID
					else if (instance == null && !instanceGroupReq && group == null) {
						instance = InstanceService.getNextAvailableInstance(mapId, difficult);
					}
					if (instance != null && instance.getPlayersInside().size() < playerSize) {
						transfer(player, loc, instance, reenter);
					}
				}
				break;
			default:
				PlayerAlliance allianceGroup = player.getPlayerAlliance();
				if (allianceGroup != null || !instanceGroupReq) {
					GeneralTeam<?, ?> team = allianceGroup;
					if (allianceGroup != null && allianceGroup.getLeague() != null)
						team = allianceGroup.getLeague();
					int teamId = team == null ? player.getObjectId() : team.getObjectId();
					instance = InstanceService.getRegisteredInstance(mapId, teamId);

					if (instance == null) {
						if (team != null)
							instance = registerTeam(team, mapId, playerSize, difficult);
						else
							instance = InstanceService.getNextAvailableInstance(mapId, difficult);
					}
					if (instance != null && instance.getPlayersInside().size() < playerSize) {
						transfer(player, loc, instance, reenter);
					}
				}
				break;
		}
	}

	private static boolean checkMentor(Player player, int mapId) {
		InstanceCooltime instancecooltime = DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(mapId);
		if (instancecooltime != null && player.isMentor()) {
			if (!instancecooltime.getCanEnterMentor()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_MENTOR_CANT_ENTER(mapId));
				return false;
			}
		}
		return true;
	}

	private static boolean checkEnterLevel(Player player, Npc npc, PortalPath portalPath) {
		if (player.hasPermission(MembershipConfig.INSTANCES_LEVEL_REQ))
			return true;
		int enterMinLvl = portalPath.getMinLevel();
		int enterMaxLvl = portalPath.getMaxLevel();
		int lvl = player.getLevel();
		if (lvl < enterMinLvl || enterMaxLvl > 0 && lvl > enterMaxLvl) {
			if (portalPath.getErrLevel() != 0) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), portalPath.getErrLevel()));
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_LEVEL());
			}
			return false;
		}
		return true;
	}

	private static boolean checkRace(Player player, Npc npc, PortalPath portalPath) {
		if (player.hasPermission(MembershipConfig.INSTANCES_RACE_REQ))
			return true;
		int siegeId = portalPath.getSiegeId();
		Race portalRace = portalPath.getRace();
		if (portalRace != Race.PC_ALL && player.getRace() != portalRace || siegeId != 0 && !checkSiegeId(player, siegeId)) {
			if (npc.getObjectTemplate().isDialogNpc()) {
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MOVE_PORTAL_ERROR_INVALID_RACE());
			}
			return false;
		}
		return true;
	}

	private static boolean checkSiegeId(Player player, int sigeId) {
		FortressLocation loc = SiegeService.getInstance().getFortress(sigeId);
		if (loc != null) {
			if (loc.getRace().getRaceId() != player.getRace().getRaceId()) {
				return false;
			}
		}
		return true;
	}

	private static boolean checkRank(Player player, Npc npc, PortalPath portalPath) {
		if (player.getAbyssRank().getRank().getId() < portalPath.getMinRank()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
			return false;
		}
		return true;
	}

	private static boolean checkPlayerSize(Player player, Npc npc, PortalPath portalPath) {
		int playerSize = portalPath.getPlayerCount();
		if (playerSize == 6 || playerSize == 3) { // group
			if (!player.isInGroup()) {
				if (portalPath.getErrGroup() != 0) {
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), portalPath.getErrGroup()));
				} else {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTER_ONLY_PARTY_DON());
				}
				return false;
			}
		} else if (playerSize > 6 && playerSize <= 24) { // alliance
			if (!player.isInAlliance()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTER_ONLY_FORCE_DON());
				return false;
			}
		} else if (playerSize > 24) { // league
			if (!player.isInLeague()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTER_ONLY_UNION_DON());
				return false;
			}
		}
		return true;
	}

	private static boolean checkTitle(Player player, Npc npc, PortalPath portalPath) {
		if (player.hasPermission(MembershipConfig.INSTANCES_TITLE_REQ))
			return true;
		int titleId = portalPath.getTitleId();
		if (titleId != 0 && player.getCommonData().getTitleId() != titleId) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
			return false;
		}
		return true;
	}

	private static boolean checkQuests(Player player, Npc npc, PortalPath portalPath) {
		if (player.hasPermission(MembershipConfig.INSTANCES_QUEST_REQ))
			return true;
		List<QuestReq> questReq = portalPath.getQuestReq();
		if (questReq != null) {
			for (QuestReq quest : questReq) {
				int questId = quest.getQuestId();
				int questStep = quest.getQuestStep();
				final QuestState qs = player.getQuestStateList().getQuestState(questId);
				if (qs != null && (qs.getStatus() == QuestStatus.COMPLETE || (questStep > 0 && qs.getQuestVarById(0) >= questStep))) {
					return true; // one requirement matched
				}
			}
			if (npc.getObjectTemplate().isDialogNpc())
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_GROUPGATE_NO_RIGHT()); // seems there's no default msg
			return false;
		}
		return true;
	}

	private static boolean checkAndRemoveRequiredItems(Player player, Npc npc, PortalPath portalPath) {
		Storage inventory = player.getInventory();
		if (inventory.getKinah() < portalPath.getKinah()) {
			if (npc.getObjectTemplate().isDialogNpc())
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(portalPath.getKinah()));
			return false;
		}
		if (portalPath.getItemReq() != null) {
			for (ItemReq item : portalPath.getItemReq()) {
				if (inventory.getItemCountByItemId(item.getItemId()) < item.getItemCount()) {
					if (npc.getObjectTemplate().isDialogNpc())
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npc.getObjectId(), DialogPage.NO_RIGHT.id()));
					else
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_ENTER_WITHOUT_ITEM());
					return false;
				}
			}
			for (ItemReq item : portalPath.getItemReq())
				inventory.decreaseByItemId(item.getItemId(), item.getItemCount());
		}
		if (portalPath.getKinah() > 0)
			inventory.decreaseKinah(portalPath.getKinah());

		return true;
	}

	private static void port(Player requester, PortalLoc loc, boolean reenter, boolean isInstance) {
		WorldMapInstance instance = null;

		if (isInstance) {
			boolean isPersonal = WorldMapType.getWorld(loc.getWorldId()).isPersonal();
			instance = InstanceService.getNextAvailableInstance(loc.getWorldId(), isPersonal ? requester.getObjectId() : 0, (byte) 0);
			InstanceService.registerPlayerWithInstance(instance, requester);
			transfer(requester, loc, instance, reenter);
		} else {
			TeleportService.teleportTo(requester, loc.getWorldId(), loc.getX(), loc.getY(), loc.getZ(), loc.getH(), TeleportAnimation.FADE_OUT_BEAM);
		}
	}

	private static WorldMapInstance registerTeam(GeneralTeam<?, ?> team, int mapId, int playerSize, byte difficult) {
		WorldMapInstance instance = InstanceService.getNextAvailableInstance(mapId, difficult);
		InstanceService.registerTeamWithInstance(instance, team, playerSize);
		return instance;
	}

	private static void transfer(Player player, PortalLoc loc, WorldMapInstance instance, boolean reenter) {
		if (instance.getStartPos() == null)
			instance.setStartPos(loc.getX(), loc.getY(), loc.getZ());
		InstanceService.registerPlayerWithInstance(instance, player);
		TeleportService.teleportTo(player, loc.getWorldId(), instance.getInstanceId(), loc.getX(), loc.getY(), loc.getZ(), loc.getH(),
			TeleportAnimation.FADE_OUT_BEAM);
		long useDelay = DataManager.INSTANCE_COOLTIME_DATA.calculateInstanceEntranceCooltime(player, instance.getMapId());
		if (useDelay > 0 && !reenter) {
			player.getPortalCooldownList().addPortalCooldown(loc.getWorldId(), useDelay);
		}
	}
}

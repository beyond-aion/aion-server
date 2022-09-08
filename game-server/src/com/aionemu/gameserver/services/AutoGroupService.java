package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.autogroup.*;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.autogroup.AutoGroupUtility;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.instance.PeriodicInstanceManager;
import com.aionemu.gameserver.services.instance.PvPArenaService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
public class AutoGroupService {

	private final Map<Integer, AutoInstance> autoInstances = new ConcurrentHashMap<>();
	private final Map<Integer, List<LookingForParty>> lookingParties = new ConcurrentHashMap<>();
	private final Set<Integer> penalties = Collections.synchronizedSet(new HashSet<>());

	private AutoGroupService() {
	}

	public void startLooking(Player player, int maskId, EntryRequestType ert) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null || !canRegister(player, ert, agt))
			return;
		lookingParties.putIfAbsent(maskId, new LinkedList<>());
		LookingForParty lfp = getLookingForPartyOfPlayer(player.getObjectId(), maskId);
		if (lfp == null) {
			lfp = new LookingForParty(
				player.isInTeam() ? player.getCurrentTeam().getOnlineMembers().stream().map(AionObject::getObjectId).collect(Collectors.toList())
					: List.of(player.getObjectId()),
				player.getRace(), ert, maskId, player.getObjectId());

			lookingParties.get(maskId).add(lfp);
		} else if (lfp.getMaskId() == maskId) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED(agt.getTemplate().getInstanceMapId()));
			return;
		}

		AutoGroupUtility.sendSuccessfulRegistration(lfp, player.getName(), agt, maskId);
		if (AutoGroupConfig.ANNOUNCE_BATTLEGROUND_REGISTRATIONS && agt.isPeriodicInstance() && ert == EntryRequestType.GROUP_ENTRY
			&& lookingParties.get(maskId).stream().filter(s -> s.getRace() == player.getRace() && s.getMaskId() == maskId).count() == 1) {
			PacketSendUtility.broadcastToWorld(
				new SM_MESSAGE(0, null, player.getRace().getL10n() + " have registered for " + agt.getL10n() + ".", ChatType.BRIGHT_YELLOW_CENTER),
				p -> p.getRace() != player.getRace() && agt.isInLvlRange(p.getLevel()));
		}

		if (checkInstancesForOpenQuickEntries(lfp, maskId))
			return;
		checkQueueForNewMatches(maskId);
	}

	private void checkQueueForNewMatches(int maskId) {
		List<LookingForParty> queuedParties = lookingParties.get(maskId);
		if (queuedParties == null || queuedParties.isEmpty())
			return;
		Collections.sort(queuedParties);
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null)
			return;
		AutoInstance autoInstance = agt.getAutoInstance();
		boolean canCreateNewInstance = false;

		List<LookingForParty> filteredParties = new LinkedList<>();
		for (LookingForParty lfp : queuedParties) {
			if (lfp.getLeaderObjId() == 0 || lfp.isOnStartEnterTask()) {
				continue;
			}
			AGQuestion question = autoInstance.addLookingForParty(lfp);
			if (question != AGQuestion.FAILED) {
				filteredParties.add(lfp);
			}
			if (question == AGQuestion.READY) {
				canCreateNewInstance = true;
				break;
			}
		}
		if (canCreateNewInstance)
			createNewInstance(autoInstance, agt, filteredParties, maskId);
		else
			autoInstance.clear();

		filteredParties.clear();
	}

	private void createNewInstance(AutoInstance autoInstance, AutoGroupType agt, List<LookingForParty> filteredParties, int maskId) {
		WorldMapInstance instance = InstanceService.getNextAvailableInstance(agt.getTemplate().getInstanceMapId(), 0, agt.getDifficultId(), null,
			autoInstance.getMaxPlayers(), false);
		autoInstance.onInstanceCreate(instance);
		autoInstances.put(instance.getInstanceId(), autoInstance);
		for (LookingForParty lfp : filteredParties) {
			lookingParties.get(maskId).remove(lfp);
			lfp.setStartEnterTime();
			lfp.getMemberObjectIds().forEach(id -> {
				searchAndRemoveAdditionalRegistrations(id);
				AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 4);
			});
		}
	}

	private boolean checkInstancesForOpenQuickEntries(LookingForParty lfp, int maskId) {
		if (lfp.getEntryRequestType() != EntryRequestType.QUICK_GROUP_ENTRY || lfp.isOnStartEnterTask())
			return false;
		for (AutoInstance autoInstance : autoInstances.values()) {
			if (autoInstance.getAutoGroupType().getTemplate().getMaskId() == maskId && autoInstance.addLookingForParty(lfp) == AGQuestion.ADDED) {
				lookingParties.get(maskId).remove(lfp);
				lfp.setStartEnterTime();
				AutoGroupUtility.sendWindowToPlayerIfOnline(lfp.getLeaderObjId(), maskId, 4);
				searchAndRemoveAdditionalRegistrations(lfp.getLeaderObjId());
				return true;
			}
		}
		return false;
	}

	private void checkQueueForQuickEntries(AutoInstance autoInstance) {
		int maskId = autoInstance.getAutoGroupType().getTemplate().getMaskId();
		List<LookingForParty> parties = lookingParties.get(maskId);
		if (parties == null || parties.isEmpty())
			return;
		for (LookingForParty lfp : parties) {
			if (lfp.getEntryRequestType() == EntryRequestType.QUICK_GROUP_ENTRY && !lfp.isOnStartEnterTask()
				&& autoInstance.addLookingForParty(lfp) == AGQuestion.ADDED) {
				lookingParties.get(maskId).remove(lfp);
				lfp.setStartEnterTime();
				AutoGroupUtility.sendWindowToPlayerIfOnline(lfp.getLeaderObjId(), maskId, 4);
				searchAndRemoveAdditionalRegistrations(lfp.getLeaderObjId());
				return;
			}
		}
	}

	private void searchAndRemoveAdditionalRegistrations(int objectId) {
		List<LookingForParty> partiesToRemove = getAllPartiesOfPlayer(objectId);
		for (LookingForParty lfp : partiesToRemove) {
			int maskId = lfp.getMaskId();
			if (lfp.isLeader(objectId)) {
				lookingParties.get(maskId).remove(lfp);
				penaliseParty(lfp);
				lfp.getMemberObjectIds().forEach(id -> AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 2));
			} else {
				lfp.unregisterMember(objectId);
				AutoGroupUtility.sendWindowToPlayerIfOnline(objectId, maskId, 2);
				penalisePlayerAndScheduleRemoval(objectId);
				checkQueueForNewMatches(maskId);
			}
		}
	}

	public void pressEnter(Player player, int instanceMaskId) {
		AutoInstance instance = getAutoInstance(player, instanceMaskId);
		if (instance == null)
			return;

		if (player.isInGroup())
			PlayerGroupService.removePlayer(player);
		if (player.isInAlliance())
			PlayerAllianceService.removePlayer(player);

		instance.onPressEnter(player);
		PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 5));
	}

	public void onEnterInstance(Player player) {
		if (player.isInInstance()) {
			int obj = player.getObjectId();
			AutoInstance autoInstance = autoInstances.get(player.getInstanceId());
			if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(obj))
				autoInstance.onEnterInstance(player);
		}
	}

	public void cancelEnter(Player player, int instanceMaskId) {
		AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
		if (autoInstance != null) {
			int objectId = player.getObjectId();
			if (!autoInstance.getRegisteredAGPlayers().get(objectId).isInInstance()) {
				autoInstance.unregister(player);
				penalisePlayerAndScheduleRemoval(objectId);
				if (destroyInstanceIfPossible(autoInstance, player.getInstanceId()))
					return;
				if (autoInstance.getAutoGroupType().getTemplate().canRegisterQuickEntry())
					checkQueueForQuickEntries(autoInstance);
			}
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 2));
		}
	}

	public void onPlayerLogin(Player player) {
		PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(player);
	}

	public List<LookingForParty> getAllPartiesOfPlayer(int objectId) {
		List<LookingForParty> parties = new ArrayList<>();
		for (int maskId : lookingParties.keySet()) {
			LookingForParty lfp = getLookingForPartyOfPlayer(objectId, maskId);
			if (lfp != null)
				parties.add(lfp);
		}
		return parties;
	}

	public LookingForParty getLookingForPartyOfPlayer(int objectId, int maskId) {
		List<LookingForParty> parties = lookingParties.get(maskId);
		if (parties != null && !parties.isEmpty()) {
			for (LookingForParty lfp : lookingParties.get(maskId))
				if (lfp.isMember(objectId))
					return lfp;
		}
		return null;
	}

	public void onPlayerLogOut(Player player) {
		int objectId = player.getObjectId();
		int instanceId = player.getInstanceId();
		for (LookingForParty lfp : getAllPartiesOfPlayer(objectId)) {
			if (lfp.isOnStartEnterTask()) {
				for (AutoInstance autoInstance : autoInstances.values()) {
					if (autoInstance.getRegisteredAGPlayers().containsKey(objectId) && !autoInstance.getRegisteredAGPlayers().get(objectId).isInInstance())
						cancelEnter(player, autoInstance.getAutoGroupType().getTemplate().getMaskId());
				}
			} else if (lfp.isLeader(objectId)) {
				lfp.setLeaderObjId(lfp.getMemberObjectIds().stream().filter(id -> id != objectId).findFirst().orElse(0));
				if (lfp.getLeaderObjId() == 0)
					lookingParties.get(lfp.getMaskId()).remove(lfp);
			} else {
				lfp.unregisterMember(objectId);
				checkQueueForNewMatches(lfp.getMaskId());
			}
		}

		if (player.isInInstance()) {
			AutoInstance autoInstance = autoInstances.get(instanceId);
			if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(objectId)) {
				WorldMapInstance instance = autoInstance.getInstance();
				if (instance != null) {
					autoInstance.getRegisteredAGPlayers().get(objectId).setOnline(false);
					destroyInstanceIfPossible(autoInstance, instanceId);
				}
			}
		}
	}

	public void onLeaveInstance(Player player) {
		if (player.isInInstance()) {
			int obj = player.getObjectId();
			int instanceId = player.getInstanceId();
			AutoInstance autoInstance = autoInstances.get(instanceId);
			if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(obj)) {
				autoInstance.onLeaveInstance(player);
				if (destroyInstanceIfPossible(autoInstance, instanceId))
					return;
				if (autoInstance.getAutoGroupType().getTemplate().canRegisterQuickEntry())
					checkQueueForQuickEntries(autoInstance);
			}
		}
		PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(player);
	}

	private boolean canRegister(Player player, EntryRequestType ert, AutoGroupType agt) {
		int mapId = agt.getTemplate().getInstanceMapId();
		int instanceMaskId = agt.getTemplate().getMaskId();
		if (!agt.isInLvlRange(player.getLevel())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ENTER_LEVEL());
			return false;
		} else if ((agt.isPvPFFAArena() || agt.isPvPSoloArena() || agt.isHarmonyArena() || agt.isGloryArena())
			&& !PvPArenaService.isPvPArenaAvailable(player, agt)) {
			return false;
		} else if (AutoGroupUtility.hasCoolDown(player, mapId)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME());
			return false;
		}
		return switch (ert) {
			case NEW_GROUP_ENTRY -> AutoGroupUtility.canRegisterNewEntry(player, agt);
			case QUICK_GROUP_ENTRY -> AutoGroupUtility.canRegisterQuickEntry(player, agt);
			case GROUP_ENTRY -> AutoGroupUtility.canRegisterGroupEntry(player, agt, mapId, instanceMaskId);
		};
	}

	private void penaliseParty(LookingForParty lfp) {
		lfp.getMemberObjectIds().forEach(this::penalisePlayerAndScheduleRemoval);
	}

	private void penalisePlayerAndScheduleRemoval(int objectId) {
		if (!penalties.contains(objectId)) {
			penalties.add(objectId);
			ThreadPoolManager.getInstance().schedule(() -> {
				penalties.remove(objectId);
				PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(objectId);
			}, 10000);
		}
	}

	public void stopRegistrationsByMaskId(int maskId) {
		List<LookingForParty> parties = lookingParties.remove(maskId);
		if (parties != null && !parties.isEmpty())
			parties.forEach(lfp -> lfp.getMemberObjectIds().forEach(id -> AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 2)));
	}

	public void cancelRegistration(Player player, int maskId) {
		cancelRegistration(getLookingForPartyOfPlayer(player.getObjectId(), maskId), player, maskId);
	}

	public void cancelRegistration(LookingForParty lfp, Player player, int maskId) {
		int objectId = player.getObjectId();
		if (lfp != null) {
			if (lfp.isLeader(objectId)) {
				lookingParties.get(maskId).remove(lfp);
				penaliseParty(lfp);
				lfp.getMemberObjectIds().forEach(id -> AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 2));
			} else {
				lfp.unregisterMember(objectId);
				AutoGroupUtility.sendWindowToPlayer(player, maskId, 2);
				penalisePlayerAndScheduleRemoval(objectId);
				checkQueueForNewMatches(maskId);
			}
		}
	}

	public boolean destroyInstanceIfPossible(AutoInstance autoInstance, int instanceId) {
		if (autoInstance.getRegisteredAGPlayers().values().stream().noneMatch(AGPlayer::isOnline)) {
			WorldMapInstance instance = autoInstance.getInstance();
			autoInstances.remove(instanceId);
			if (instance != null)
				InstanceService.destroyInstance(instance);
			autoInstance.clear();
			return true;
		}
		return false;
	}

	private AutoInstance getAutoInstance(Player player, int instanceMaskId) {
		for (AutoInstance autoInstance : autoInstances.values())
			if (autoInstance.getAutoGroupType().getTemplate().getMaskId() == instanceMaskId
				&& autoInstance.getRegisteredAGPlayers().containsKey(player.getObjectId()))
				return autoInstance;
		return null;
	}

	public boolean isAutoInstance(int instanceId) {
		return autoInstances.containsKey(instanceId);
	}

	public static AutoGroupService getInstance() {
		return NewSingletonHolder.INSTANCE;
	}

	private static class NewSingletonHolder {

		private static final AutoGroupService INSTANCE = new AutoGroupService();
	}

}

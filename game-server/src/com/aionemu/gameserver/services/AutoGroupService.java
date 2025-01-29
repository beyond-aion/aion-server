package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.autogroup.*;
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
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
public class AutoGroupService {

	private final Map<WorldMapInstance, AutoInstance> autoInstances = new ConcurrentHashMap<>();
	private final Map<Integer, List<LookingForParty>> lookingParties = new ConcurrentHashMap<>();
	private final Set<Integer> penalties = ConcurrentHashMap.newKeySet();

	private AutoGroupService() {
	}

	public void startLooking(Player player, int maskId, EntryRequestType ert) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null || !canRegister(player, ert, agt))
			return;
		List<LookingForParty> lfps = lookingParties.computeIfAbsent(maskId, k -> new ArrayList<>());
		LookingForParty lfp;
		synchronized (lfps) {
			lfp = getSearchEntry(player.getObjectId(), lfps);
			if (lfp != null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED(agt.getTemplate().getInstanceMapId()));
				return;
			}

			// Verifica se há um player registrado com o mesmo MAC address
			String playerMac = player.getClientConnection().getMacAddress();
			String playerIp = player.getClientConnection().getIP();
			boolean isMacAlreadyRegistered = false;
			for (Player p : World.getInstance().getAllPlayers()) {
				String pMac = p.getClientConnection().getMacAddress();
				String pIp = p.getClientConnection().getIP();
				if(pMac.equals(playerMac) && playerIp.equals(pIp)){
					lfp = getSearchEntry(p.getObjectId(), lfps);
					if (lfp != null) {
						isMacAlreadyRegistered = true;
					}
				}
			}

			if (isMacAlreadyRegistered) {
				PacketSendUtility.sendMessage(player, "It is not allowed to apply with two accounts!");
				return;
			}

			lfp = new LookingForParty(player, ert, maskId);
			lfps.add(lfp);

			AutoGroupUtility.sendSuccessfulRegistration(lfp, player.getName(), agt, maskId);
			if (AutoGroupConfig.ANNOUNCE_BATTLEGROUND_REGISTRATIONS && agt.isPeriodicInstance() && ert == EntryRequestType.GROUP_ENTRY
				&& lfps.stream().filter(s -> s.getRace() == player.getRace()).count() == 1) {
				PacketSendUtility.broadcastToWorld(
					new SM_MESSAGE(0, null, player.getRace().getL10n() + " have registered for " + agt.getL10n() + ".", ChatType.BRIGHT_YELLOW_CENTER),
					p -> p.getRace() != player.getRace() && agt.isInLvlRange(p.getLevel()));
			}else if( AutoGroupConfig.ANNOUNCE_BATTLEGROUND_REGISTRATIONS && (agt.isHarmonyArena() || agt.isPvPSoloArena()) ){
				PacketSendUtility.broadcastToWorld(
					new SM_MESSAGE(0, null, "Players have registered for " + agt.getL10n() + ".", ChatType.BRIGHT_YELLOW_CENTER),
					p -> agt.isInLvlRange(p.getLevel()));
			}

			if (!checkInstancesForOpenQuickEntries(lfp, maskId))
				checkQueueForNewMatches(maskId);
		}
	}

	private void checkQueueForNewMatches(int maskId) {
		List<LookingForParty> queuedParties = lookingParties.get(maskId);
		if (queuedParties == null || queuedParties.isEmpty())
			return;

		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null)
			return;

		synchronized (queuedParties) {
			queuedParties.sort(null);

			for (int i = 0; i < queuedParties.size(); i++) {
				AutoInstance autoInstance = agt.createAutoInstance();
				Map<Race, Integer> raceCount = new HashMap<>();
				raceCount.put(Race.ELYOS, 0);
				raceCount.put(Race.ASMODIANS, 0);

				LookingForParty firstParty = queuedParties.get(i);
				AGQuestion question = autoInstance.addLookingForParty(firstParty);
				if (question == AGQuestion.FAILED)
					continue;

				List<LookingForParty> filteredParties = new ArrayList<>();
				filteredParties.add(firstParty);
				raceCount.put(firstParty.getRace(), raceCount.get(firstParty.getRace()) + firstParty.getMemberObjectIds().size());

				boolean allowSameRace = agt.isHarmonyArena() || agt.isPvPSoloArena() || agt.isGloryArena() || agt.isPvPFFAArena() || agt.isTrainingPvPFFAArena() || agt.isTrainingPvPSoloArena() || agt.isTrainingHarmonyArena();

				if (question != AGQuestion.READY) {
					for (int j = i + 1; j < queuedParties.size(); j++) {
						LookingForParty secondParty = queuedParties.get(j);

						Race secondRace = secondParty.getRace();
						int secondGroupSize = secondParty.getMemberObjectIds().size();

						if(!allowSameRace) {
							if (raceCount.get(secondRace) + secondGroupSize > autoInstance.getMaxPlayers() / 2) {
								continue;
							}
						}else{
							if (raceCount.get(secondRace) + secondGroupSize > autoInstance.getMaxPlayers()) {
								continue;
							}
						}

						if (!allowSameRace && firstParty.getRace() == secondRace) {
							continue;
						}

						question = autoInstance.addLookingForParty(secondParty);
						if (question != AGQuestion.FAILED) {
							filteredParties.add(secondParty);
							raceCount.put(secondRace, raceCount.get(secondRace) + secondGroupSize);

							if (question == AGQuestion.READY) {
								break;
							}
						}
					}
				}

				if (question == AGQuestion.READY) {
					int totalPlayers = filteredParties.stream()
						.mapToInt(p -> p.getMemberObjectIds().size())
						.sum();

					if (totalPlayers == autoInstance.getMaxPlayers()) {
						createNewInstance(autoInstance, agt, filteredParties, maskId);
						break;
					}
				}
			}
		}
	}

	private void createNewInstance(AutoInstance autoInstance, AutoGroupType agt, List<LookingForParty> filteredParties, int maskId) {
		WorldMapInstance instance = InstanceService.getNextAvailableInstance(
			agt.getTemplate().getInstanceMapId(),
			0,
			agt.getDifficultId(),
			null,
			autoInstance.getMaxPlayers(),
			false
		);
		autoInstance.onInstanceCreate(instance);
		autoInstances.put(instance, autoInstance);

		Map<Race, Integer> raceCount = new HashMap<>();
		raceCount.put(Race.ELYOS, 0);
		raceCount.put(Race.ASMODIANS, 0);

		for (LookingForParty lfp : filteredParties) {
			Race groupRace = lfp.getRace();
			raceCount.put(groupRace, raceCount.get(groupRace) + lfp.getMemberObjectIds().size());
		}

		boolean allowSameRace = agt.isHarmonyArena() || agt.isPvPSoloArena() || agt.isGloryArena() || agt.isPvPFFAArena() || agt.isTrainingPvPFFAArena() || agt.isTrainingPvPSoloArena() || agt.isTrainingHarmonyArena();
		int totalPlayers = raceCount.values().stream().mapToInt(Integer::intValue).sum();

		if ((allowSameRace || raceCount.size() > 1) && totalPlayers == autoInstance.getMaxPlayers()) {
			for (LookingForParty lfp : filteredParties) {
				acceptGroupEntry(lfp, maskId, raceCount);
			}
		} else {
			autoInstances.remove(instance);
		}
	}

	private boolean checkInstancesForOpenQuickEntries(LookingForParty lfp, int maskId) {
		if (lfp.getEntryRequestType() != EntryRequestType.QUICK_GROUP_ENTRY || lfp.isOnStartEnterTask()) {
			return false;
		}

		for (AutoInstance autoInstance : autoInstances.values()) {
			if (autoInstance.getAutoGroupType().getTemplate().getMaskId() != maskId) {
				continue;
			}

			Map<Race, Integer> raceCount = new HashMap<>();
			raceCount.put(Race.ELYOS, 0);
			raceCount.put(Race.ASMODIANS, 0);

			if (autoInstance.getInstance() != null) {
				for (Player player : autoInstance.getInstance().getPlayersInside()) {
					Race playerRace = player.getRace();
					raceCount.put(playerRace, raceCount.get(playerRace) + 1);
				}
			}

			AutoGroupType agt = autoInstance.getAutoGroupType();
			boolean allowSameRace = agt.isHarmonyArena() || agt.isPvPSoloArena() || agt.isGloryArena() || agt.isPvPFFAArena() || agt.isTrainingPvPFFAArena() || agt.isTrainingPvPSoloArena() || agt.isTrainingHarmonyArena();
			Race lfpRace = lfp.getRace();
			int lfpGroupSize = lfp.getMemberObjectIds().size();

			if(!allowSameRace) {
				if (raceCount.get(lfpRace) + lfpGroupSize > autoInstance.getMaxPlayers() / 2) {
					continue;
				}
			}else{
				if (raceCount.get(lfpRace) + lfpGroupSize > autoInstance.getMaxPlayers()) {
					continue;
				}
			}

			if (!allowSameRace && raceCount.values().stream().allMatch(count -> count == 0)) {

				continue;
			}

			AGQuestion question = autoInstance.addLookingForParty(lfp);
			if (question == AGQuestion.READY) {

				acceptGroupEntry(lfp, maskId, raceCount);
				return true;
			} else if (question != AGQuestion.FAILED) {
				raceCount.put(lfpRace, raceCount.get(lfpRace) + lfpGroupSize);
			}
		}
		return false;
	}

	private void checkQueueForQuickEntries(AutoInstance autoInstance) {
		Map<Race, Integer> raceCount = new HashMap<>();
		raceCount.put(Race.ELYOS, 0);
		raceCount.put(Race.ASMODIANS, 0);

		for (Player player : autoInstance.getInstance().getPlayersInside()) {
			Race playerRace = player.getRace();
			raceCount.put(playerRace, raceCount.get(playerRace) + 1);
		}

		int maskId = autoInstance.getAutoGroupType().getTemplate().getMaskId();
		List<LookingForParty> parties = lookingParties.get(maskId);
		if (parties == null || parties.isEmpty()) {
			return;
		}

		AutoGroupType agt = autoInstance.getAutoGroupType();
		boolean allowSameRace = agt.isHarmonyArena() || agt.isPvPSoloArena() || agt.isGloryArena() || agt.isPvPFFAArena() || agt.isTrainingPvPFFAArena() || agt.isTrainingPvPSoloArena() || agt.isTrainingHarmonyArena();

		synchronized (parties) {
			List<LookingForParty> filteredParties = new ArrayList<>();

			for (LookingForParty lfp : parties) {
				Race race = lfp.getRace();
				int groupSize = lfp.getMemberObjectIds().size();

				if(!allowSameRace) {
					if (raceCount.get(race) + groupSize > autoInstance.getMaxPlayers() / 2) {
						continue;
					}
				}else{
					if (raceCount.get(race) + groupSize > autoInstance.getMaxPlayers()) {
						continue;
					}
				}

				if (!allowSameRace && filteredParties.stream().anyMatch(p -> p.getRace() == race)) {
					continue;
				}

				filteredParties.add(lfp);
				raceCount.put(race, raceCount.get(race) + groupSize);

				int totalPlayers = filteredParties.stream()
					.mapToInt(p -> p.getMemberObjectIds().size())
					.sum();

				if (totalPlayers == autoInstance.getMaxPlayers()) {
					createNewInstance(autoInstance, autoInstance.getAutoGroupType(), filteredParties, maskId);
					return;
				}
			}
		}
	}

	private void acceptGroupEntry(LookingForParty lfp, int maskId, Map<Race, Integer> sentPlayers) {
		List<Integer> memberIds = lfp.getMemberObjectIds();
		Race groupRace = null;
		removeSearchEntry(lfp);
		lfp.setStartEnterTime();
		for (int id : memberIds) {
			Player player = World.getInstance().getPlayer(id);
			if (player == null) {
				continue;
			}
			groupRace = player.getRace();
			searchAndRemoveAdditionalRegistrations(id);
			AutoGroupUtility.sendWindowToPlayerIfOnline(id, maskId, 4);
		}

		sentPlayers.put(groupRace, sentPlayers.get(groupRace) + memberIds.size());
	}

	private void searchAndRemoveAdditionalRegistrations(int objectId) {
		List<LookingForParty> partiesToRemove = getSearchEntries(objectId);
		for (LookingForParty lfp : partiesToRemove) {
			int maskId = lfp.getMaskId();
			if (lfp.isLeader(objectId)) {
				removeSearchEntry(lfp);
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
			AutoInstance autoInstance = autoInstances.get(player.getWorldMapInstance());
			if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(obj))
				autoInstance.onEnterInstance(player);
		}
	}

	public void cancelEnter(Player player, int instanceMaskId) {
		AutoInstance autoInstance = getAutoInstance(player, instanceMaskId);
		if (autoInstance != null) {
			int objectId = player.getObjectId();
			autoInstance.unregister(player);
			penalisePlayerAndScheduleRemoval(objectId);
			destroyOrAddPlayersFromQuickEntries(autoInstance);
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(instanceMaskId, 2));
		}
	}

	public void onPlayerLogin(Player player) {
		PeriodicInstanceManager.getInstance().checkAndSendOpenRegistrations(player);
	}

	public boolean isSearching(Player player, int maskId) {
		return getSearchEntry(player.getObjectId(), lookingParties.get(maskId)) != null;
	}

	private LookingForParty getSearchEntry(Player player, int maskId) {
		return getSearchEntry(player.getObjectId(), lookingParties.get(maskId));
	}

	private LookingForParty getSearchEntry(int playerObjectId, List<LookingForParty> parties) {
		if (parties != null) {
			synchronized (parties) {
				for (LookingForParty lfp : parties)
					if (lfp.isMember(playerObjectId))
						return lfp;
			}
		}
		return null;
	}

	private List<LookingForParty> getSearchEntries(int playerObjectId) {
		return lookingParties.values().stream().map(parties -> getSearchEntry(playerObjectId, parties)).filter(Objects::nonNull).toList();
	}

	public void onLogout(Player player) {
		int objectId = player.getObjectId();
		for (LookingForParty lfp : getSearchEntries(objectId)) {
			if (lfp.isOnStartEnterTask()) {
				for (AutoInstance autoInstance : autoInstances.values()) {
					cancelEnter(player, autoInstance.getAutoGroupType().getTemplate().getMaskId());
				}
			} else if (lfp.isLeader(objectId)) {
				lfp.setLeaderObjId(lfp.getMemberObjectIds().stream().filter(id -> id != objectId).findFirst().orElse(0));
				if (lfp.getLeaderObjId() == 0) {
					removeSearchEntry(lfp);
				}
			} else {
				lfp.unregisterMember(objectId);
				checkQueueForNewMatches(lfp.getMaskId());
			}
		}

		AutoInstance autoInstance = autoInstances.get(player.getWorldMapInstance());
		if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(objectId)) {
			destroyIfPossible(autoInstance);
		}
	}

	private void removeSearchEntry(LookingForParty lfp) {
		List<LookingForParty> lfps = lookingParties.get(lfp.getMaskId());
		synchronized (lfps) {
			lfps.remove(lfp);
		}
	}

	public void onLeaveInstance(Player player) {
		AutoInstance autoInstance = autoInstances.get(player.getWorldMapInstance());
		if (autoInstance != null && autoInstance.getRegisteredAGPlayers().containsKey(player.getObjectId())) {
			autoInstance.onLeaveInstance(player);
			destroyOrAddPlayersFromQuickEntries(autoInstance);
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
		if (penalties.add(objectId)) {
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
		cancelRegistration(getSearchEntry(player, maskId), player, maskId);
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

	private void destroyOrAddPlayersFromQuickEntries(AutoInstance autoInstance) {
		if (!destroyIfPossible(autoInstance) && autoInstance.getAutoGroupType().getTemplate().canRegisterQuickEntry())
			checkQueueForQuickEntries(autoInstance);
	}

	public boolean destroyIfPossible(AutoInstance autoInstance) {
		WorldMapInstance instance = autoInstance.getInstance();
		if (autoInstance.getRegisteredAGPlayers().isEmpty() && instance.getPlayersInside().stream().noneMatch(Player::isOnline)) {
			autoInstances.remove(instance);
			InstanceService.destroyInstance(instance);
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

	public boolean isInAutoInstance(Player player) {
		return autoInstances.containsKey(player.getWorldMapInstance());
	}

	public static AutoGroupService getInstance() {
		return NewSingletonHolder.INSTANCE;
	}

	private static class NewSingletonHolder {

		private static final AutoGroupService INSTANCE = new AutoGroupService();
	}

}

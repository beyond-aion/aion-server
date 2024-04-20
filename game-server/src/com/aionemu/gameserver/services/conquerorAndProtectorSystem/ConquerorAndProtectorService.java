package com.aionemu.gameserver.services.conquerorAndProtectorSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.templates.cp.CPRank;
import com.aionemu.gameserver.model.templates.cp.CPType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CONQUEROR_PROTECTOR;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionDominionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * Since 4.8 there is no serial killer system anymore, but a conqueror and protector system. See
 * https://aionpowerbook.com/powerbook/Conqueror_and_Protector_System
 * 
 * @author Source, Dtem, ginho, Yeats, Neon
 */
public class ConquerorAndProtectorService {

	private final Map<Integer, CPInfo> conquerors = new ConcurrentHashMap<>();
	private final Map<Integer, CPInfo> protectors = new ConcurrentHashMap<>();
	private final Map<Integer, Long> intruderScanCooldowns = new ConcurrentHashMap<>();
	private final Map<Integer, Race> handledWorlds = new HashMap<>();

	private ConquerorAndProtectorService() {
	}

	public void init() {
		if (!CustomConfig.CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED || CustomConfig.CONQUEROR_AND_PROTECTOR_WORLDS.isEmpty())
			return;

		for (int worldId : CustomConfig.CONQUEROR_AND_PROTECTOR_WORLDS) {
			int worldType = worldId / 10000000 % 10; // the second digit in the map ID denotes the world type (0 = all, 1 = elyos, 2 = asmodians)
			if (worldType == 1)
				handledWorlds.put(worldId, Race.ELYOS);
			else if (worldType == 2)
				handledWorlds.put(worldId, Race.ASMODIANS);
			else
				throw new IllegalArgumentException("Map " + worldId + " is not supported for conqueror and protector system (not race specific).");
		}

		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			long nowMillis = System.currentTimeMillis();
			intruderScanCooldowns.values().removeIf(cd -> nowMillis >= cd);
			Stream.concat(conquerors.values().stream(), protectors.values().stream()).forEach(info -> {
				if (info.getVictims() > 0)
					addVictims(null, info, -CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_DECREASE_COUNT);
			});
		}, CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_DECREASE_INTERVAL * 60000, CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_DECREASE_INTERVAL * 60000); // kills remove timer
	}

	public CPInfo getCPInfoForCurrentMap(Player player) {
		return getCPInfoForCurrentMap(player, false);
	}

	public CPInfo getCPInfoForCurrentMap(Player player, boolean createIfNotExists) {
		CPInfo cpInfo = null;
		CPType cpTypeForCurrentMap = getCPTypeForCurrentMap(player);
		if (cpTypeForCurrentMap != null) {
			if (createIfNotExists)
				cpInfo = cpTypeForCurrentMap == CPType.PROTECTOR
					? protectors.computeIfAbsent(player.getObjectId(), key -> new CPInfo(cpTypeForCurrentMap, player))
					: conquerors.computeIfAbsent(player.getObjectId(), key -> new CPInfo(cpTypeForCurrentMap, player));
			else
				cpInfo = cpTypeForCurrentMap == CPType.PROTECTOR ? protectors.get(player.getObjectId()) : conquerors.get(player.getObjectId());
		}
		return cpInfo;
	}

	public void onEnterMap(Player player) {
		if (!CustomConfig.CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED)
			return;
		CPInfo cpInfo = getCPInfoForCurrentMap(player);
		if (cpInfo != null && cpInfo.getLDRank() == 0) {
			int type = cpInfo.getType() == CPType.CONQUEROR ? 0 : 7;
			int intruderScanCd = cpInfo.getType() == CPType.CONQUEROR ? 0 : getOrRemoveCooldown(player.getObjectId());
			PacketSendUtility.sendPacket(player, new SM_CONQUEROR_PROTECTOR(type, cpInfo.getRank(), intruderScanCd));
			updateBuffAndNotifyNearbyPlayers(player, cpInfo);
		}
	}

	public void onLeaveMap(Player player) {
		CPInfo info = getCPInfoForCurrentMap(player);
		if (info != null)
			info.getBuff().endEffect(player);
	}

	// for Legion Dominion Zones
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (!CustomConfig.CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED)
			return;
		if (zone.isDominionZone() && isOccupiedLegionDominionZone(player, zone)) {
			CPInfo cpInfo = protectors.computeIfAbsent(player.getObjectId(), key -> new CPInfo(CPType.PROTECTOR, player));
			if (cpInfo.getLDRank() == 0) {
				cpInfo.setLDRank(3);
				cpInfo.getBuff().applyEffect(player, cpInfo.getType(), 3);
				PacketSendUtility.sendPacket(player, new SM_CONQUEROR_PROTECTOR(7, cpInfo.getLDRank(), getOrRemoveCooldown(player.getObjectId())));
			}
		}
	}

	public void onLeaveZone(Player player, ZoneInstance zone) {
		if (!CustomConfig.CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED)
			return;
		if (zone.isDominionZone() && isOccupiedLegionDominionZone(player, zone)) {
			resetLegionDominionRank(player);
		}
	}

	public void onLeaveLegion(Player player) {
		if (CustomConfig.CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED)
			resetLegionDominionRank(player);
	}

	private void resetLegionDominionRank(Player player) {
		CPInfo cpInfo = protectors.get(player.getObjectId());
		if (cpInfo != null && cpInfo.getLDRank() > 0) {
			cpInfo.setLDRank(0);
			updateBuffAndNotifyNearbyPlayers(player, cpInfo);
			if (cpInfo.getRank() == 0)
				protectors.remove(player.getObjectId());
		}
	}

	private boolean isOccupiedLegionDominionZone(Player player, ZoneInstance zone) {
		if (player.getLegion() == null)
			return false;
		LegionDominionLocation loc = LegionDominionService.getInstance().getLegionDominionLoc(player.getLegion().getOccupiedLegionDominion());
		return loc != null && loc.getZoneNameAsString().equalsIgnoreCase(zone.getAreaTemplate().getZoneName().name());
	}

	public void onKill(Player killer, Player victim) {
		if (!CustomConfig.CONQUEROR_AND_PROTECTOR_SYSTEM_ENABLED)
			return;
		if (handledWorlds.containsKey(victim.getWorldId())) {
			if (killer.getLevel() - victim.getLevel() <= CustomConfig.CONQUEROR_AND_PROTECTOR_LEVEL_DIFF) {
				CPInfo killerInfo = getCPInfoForCurrentMap(killer, true);
				CPInfo victimInfo = getCPInfoForCurrentMap(victim);

				if (victimInfo != null && victimInfo.getType() == CPType.CONQUEROR && victimInfo.getRank() == 3) {
					SM_SYSTEM_MESSAGE msg = killer.getRace() == Race.ASMODIANS
						? SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_LIGHT_DEATH_TO_B(killer.getName(), victim.getName())
						: SM_SYSTEM_MESSAGE.STR_MSG_SLAYER_DARK_DEATH_TO_B(killer.getName(), victim.getName());
					PacketSendUtility.broadcastToMap(victim, msg);
				}
				addVictims(killer, killerInfo, 1);
			}
		}
	}

	public void sendDetectCooldown(Player player) {
		CPInfo cpInfo = getCPInfoForCurrentMap(player);
		if (cpInfo == null)
			return;
		PacketSendUtility.sendPacket(player, new SM_CONQUEROR_PROTECTOR(7, cpInfo.getLDRank(), getOrRemoveCooldown(player.getObjectId())));
	}

	private void addVictims(Player player, CPInfo info, int count) {
		int newVictims = Math.min(Math.max(info.getVictims() + count, 0), CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_RANK3);
		info.setVictims(newVictims);
		int newRank = getRank(info.getVictims());
		if (info.getRank() != newRank) {
			info.setRank(newRank);
			if (info.getRank() == 0 && info.getLDRank() == 0)
				(info.getType() == CPType.CONQUEROR ? conquerors : protectors).remove(info.getPlayerId());
			if (player == null)
				player = World.getInstance().getPlayer(info.getPlayerId());
			if (player != null)
				updateBuffAndNotifyNearbyPlayers(player, info);
		}
	}

	private void updateBuffAndNotifyNearbyPlayers(Player player, CPInfo cpInfo) {
		if (cpInfo.getLDRank() == 0) { // not inside a legion dominion zone
			if (getCPTypeForCurrentMap(player) == cpInfo.getType())
				cpInfo.getBuff().applyEffect(player, cpInfo.getType(), cpInfo.getRank());
			else
				cpInfo.getBuff().endEffect(player);
			PacketSendUtility.sendPacket(player, new SM_CONQUEROR_PROTECTOR(cpInfo.getType() == CPType.CONQUEROR ? 1 : 8, cpInfo.getRank()));
			PacketSendUtility.broadcastPacket(player, new SM_CONQUEROR_PROTECTOR(cpInfo.getType() == CPType.CONQUEROR ? 6 : 9, player));
		}
	}

	public void intruderScan(Player player) {
		if (getCPTypeForCurrentMap(player) != CPType.PROTECTOR)
			return;
		if (getOrRemoveCooldown(player.getObjectId()) > 0)
			return;
		intruderScanCooldowns.put(player.getObjectId(), System.currentTimeMillis() + 180 * 1000);
		PacketSendUtility.sendPacket(player, new SM_CONQUEROR_PROTECTOR(findIntruders(player), true));
	}

	private int getOrRemoveCooldown(int objectId) {
		Long cd = intruderScanCooldowns.get(objectId);
		if (cd != null) {
			long remainingMillis = cd - System.currentTimeMillis();
			if (remainingMillis > 0) {
				return (int) (remainingMillis / 1000);
			} else {
				intruderScanCooldowns.remove(objectId);
			}
		}
		return 0;
	}

	private List<Player> findIntruders(Player player) {
		CPInfo protector = protectors.get(player.getObjectId());
		List<Player> intruders = new ArrayList<>();
		if (protector != null) {
			for (CPInfo conqueror : conquerors.values()) {
				if (canSee(protector, conqueror)) {
					Player intruder = World.getInstance().getPlayer(conqueror.getPlayerId());
					if (intruder != null && intruder.getRace() != player.getRace() && PositionUtil.isInRange(intruder, player, 500))
						intruders.add(intruder);
				}
			}
		}
		return intruders;
	}

	/**
	 * @return True if the protector has the required rank to detect the intruders position on the map
	 */
	private boolean canSee(CPInfo protector, CPInfo intruder) {
		int rank = Math.max(protector.getLDRank(), protector.getRank());
		CPRank cpRank = DataManager.CONQUEROR_AND_PROTECTOR_DATA.getRank(CPType.PROTECTOR, rank);
		return cpRank != null && cpRank.getVisibleIntruderMinRank() != 0 && cpRank.getVisibleIntruderMinRank() >= intruder.getRank();
	}

	private int getRank(int kills) {
		if (kills >= CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_RANK3)
			return 3;
		if (kills >= CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_RANK2)
			return 2;
		if (kills >= CustomConfig.CONQUEROR_AND_PROTECTOR_KILLS_RANK1)
			return 1;
		return 0;
	}

	private CPType getCPTypeForCurrentMap(Player player) {
		Race race = handledWorlds.get(player.getWorldId());
		return race == null ? null : race == player.getRace() ? CPType.PROTECTOR : CPType.CONQUEROR;
	}

	public static ConquerorAndProtectorService getInstance() {
		return ConquerorAndProtectorService.SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final ConquerorAndProtectorService instance = new ConquerorAndProtectorService();
	}

}

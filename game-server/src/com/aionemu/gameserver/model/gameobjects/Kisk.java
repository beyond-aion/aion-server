package com.aionemu.gameserver.model.gameobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.stats.KiskStatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KISK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth, nrg
 */
public class Kisk extends SummonedObject<Player> {

	private final int creatorId;
	private final int legionId;
	private final Race ownerRace;

	private KiskStatsTemplate kiskStatsTemplate;

	private int remainingResurrections;
	private long kiskSpawnTime;

	public final int KISK_LIFETIME_IN_SEC = 2 * 60 * 60; // 2 hours

	private final Set<Integer> kiskMemberIds;

	/**
	 * @param objId
	 * @param controller
	 * @param spawnTemplate
	 * @param objectTemplate
	 */
	public Kisk(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate npcTemplate, Player owner) {
		super(objId, controller, spawnTemplate, npcTemplate, npcTemplate.getLevel());

		this.kiskStatsTemplate = npcTemplate.getKiskStatsTemplate();

		if (this.kiskStatsTemplate == null)
			this.kiskStatsTemplate = new KiskStatsTemplate();

		this.kiskMemberIds = ConcurrentHashMap.newKeySet();
		this.remainingResurrections = this.kiskStatsTemplate.getMaxResurrects();
		this.kiskSpawnTime = System.currentTimeMillis() / 1000;
		this.creatorId = owner.getObjectId();
		this.legionId = owner.getLegion() == null ? 0 : owner.getLegion().getLegionId();
		this.ownerRace = owner.getRace();
	}

	@Override
	public int getCreatorId() {
		return creatorId;
	}

	@Override
	public boolean isEnemy(Creature creature) {
		return creature.isEnemyFrom(this);
	}

	/**
	 * Required so that the enemy race can attack the Kisk!
	 */
	@Override
	public boolean isEnemyFrom(Player player) {
		return !player.getRace().equals(ownerRace) && isInsidePvPZone() && player.isInsidePvPZone();
	}

	@Override
	public CreatureType getType(Creature creature) {
		if (creature instanceof Player)
			return isEnemyFrom((Player) creature) ? CreatureType.ATTACKABLE : CreatureType.SUPPORT;
		return super.getType(creature);
	}

	/**
	 * @return NpcObjectType.NORMAL
	 */
	@Override
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.NORMAL;
	}

	/**
	 * 1 ~ race 2 ~ legion 3 ~ solo 4 ~ group 5 ~ alliance
	 * 
	 * @return useMask
	 */
	public int getUseMask() {
		return kiskStatsTemplate.getUseMask();
	}

	public List<Player> getCurrentMemberList() {
		List<Player> currentMemberList = new ArrayList<>();

		for (int memberId : kiskMemberIds) {
			Player member = World.getInstance().findPlayer(memberId);
			if (member != null)
				currentMemberList.add(member);
		}

		return currentMemberList;
	}

	public int getCurrentMemberCount() {
		return kiskMemberIds.size();
	}

	public Set<Integer> getCurrentMemberIds() {
		return kiskMemberIds;
	}

	public int getMaxMembers() {
		return kiskStatsTemplate.getMaxMembers();
	}

	public int getRemainingResurrects() {
		return remainingResurrections;
	}

	public int getMaxRessurects() {
		return kiskStatsTemplate.getMaxResurrects();
	}

	public int getRemainingLifetime() {
		long timeElapsed = (System.currentTimeMillis() / 1000) - kiskSpawnTime;
		int timeRemaining = (int) (KISK_LIFETIME_IN_SEC - timeElapsed);
		return (timeRemaining > 0 ? timeRemaining : 0);
	}

	/**
	 * @return True if the player may bind to this kisk
	 */
	public boolean canBind(Player player) {
		if (getCurrentMemberCount() >= getMaxMembers())
			return false;

		return isUseAllowed(player);
	}

	private boolean isUseAllowed(Player player) {
		switch (getUseMask()) {
			case 0: // Test item (no restrictions)
				return true;
			case 1: // Race
				if (ownerRace == player.getRace())
					return true;
				break;
			case 2: // Legion
				if (player.getObjectId() == getCreatorId() || legionId != 0 && LegionService.getInstance().getLegion(legionId).isMember(player.getObjectId()))
					return true;
				break;
			case 3: // Solo
				return player.getObjectId() == getCreatorId();
			case 4: // Group (PlayerGroup or PlayerAllianceGroup)
				if (player.getObjectId() == getCreatorId() || player.isInTeam() && player.getCurrentGroup().hasMember(getCreatorId()))
					return true;
				break;
			case 5: // Alliance (PlayerGroup or PlayerAlliance)
				if (player.getObjectId() == getCreatorId() || player.isInTeam() && player.getCurrentTeam().hasMember(getCreatorId()))
					return true;
				break;
			default:
				LoggerFactory.getLogger(Kisk.class).warn("Unhandled UseMask " + getUseMask() + " for Kisk " + getNpcId());
		}

		return false;
	}

	/**
	 * @param player
	 */
	public void addPlayer(Player player) {
		if (kiskMemberIds.add(player.getObjectId())) {
			broadcastKiskUpdate();
		} else {
			PacketSendUtility.sendPacket(player, new SM_KISK_UPDATE(this));
		}
		player.setKisk(this);
	}

	/**
	 * @param player
	 */
	public void removePlayer(Player player) {
		player.setKisk(null);
		if (kiskMemberIds.remove(player.getObjectId()))
			broadcastKiskUpdate();
	}

	/**
	 * Sends SM_KISK_UPDATE to each member
	 */
	private void broadcastKiskUpdate() {
		// on all members, but not the ones in knownlist, they will receive the update in the next step
		for (Player member : getCurrentMemberList()) {
			if (!getKnownList().knows(member))
				PacketSendUtility.sendPacket(member, new SM_KISK_UPDATE(this));
		}

		// all players having the same race in knownlist
		PacketSendUtility.broadcastPacket(this, new SM_KISK_UPDATE(this), player -> player.getRace() == ownerRace);
	}

	/**
	 * @param message
	 */
	public void broadcastPacket(SM_SYSTEM_MESSAGE message) {
		for (Player member : getCurrentMemberList()) {
			PacketSendUtility.sendPacket(member, message);
		}
	}

	/**
	 * @param player
	 */
	public void resurrectionUsed() {
		remainingResurrections--;
		broadcastKiskUpdate();
		if (remainingResurrections <= 0)
			getController().delete();
	}

	/**
	 * @return ownerRace
	 */
	public Race getOwnerRace() {
		return ownerRace;
	}

	public boolean isActive() {
		return !isDead() && getRemainingResurrects() > 0;
	}
}

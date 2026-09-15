package com.aionemu.gameserver.model.gameobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.stats.KiskStatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_KISK_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author Sarynth, nrg
 */
public class Kisk extends SummonedObject<Player> {

	private static final long KISK_LIFETIME_IN_SEC = TimeUnit.HOURS.toSeconds(2);
	private final int legionId;
	private final Race ownerRace;

	private final KiskStatsTemplate kiskStatsTemplate;

	private int remainingResurrections;

	private final Set<Integer> kiskMemberIds;

	public Kisk(NpcController controller, SpawnTemplate spawnTemplate, Player owner) {
		super(controller, spawnTemplate, DataManager.NPC_DATA.getNpcTemplate(spawnTemplate.getNpcId()).getLevel(), null);
		this.kiskStatsTemplate = getObjectTemplate().getKiskStatsTemplate() == null ? new KiskStatsTemplate()
			: getObjectTemplate().getKiskStatsTemplate();
		this.kiskMemberIds = ConcurrentHashMap.newKeySet();
		this.remainingResurrections = this.kiskStatsTemplate.getMaxResurrects();
		this.legionId = owner.getLegion() == null ? 0 : owner.getLegion().getLegionId();
		this.ownerRace = owner.getRace();
		setCreatorId(owner.getObjectId());
		setMasterName(owner.getName());
		setKnownlist(new PlayerAwareKnownList(this));
		setEffectController(new EffectController(this));
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
		if (creature instanceof Player player)
			return isEnemyFrom(player) ? CreatureType.ATTACKABLE : CreatureType.SUPPORT;
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
			Player member = World.getInstance().getPlayer(memberId);
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
		if (isDead())
			return 0;
		long timeElapsed = getMillisSinceSpawn() / 1000;
		int timeRemaining = (int) (KISK_LIFETIME_IN_SEC - timeElapsed);
		return Math.max(timeRemaining, 0);
	}

	/**
	 * @return True if the player may bind to this kisk
	 */
	public boolean canBind(Player player) {
		return getCurrentMemberCount() < getMaxMembers() && isUseAllowed(player);
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

	public void addPlayer(Player player) {
		if (kiskMemberIds.add(player.getObjectId())) {
			broadcastKiskUpdate();
		} else {
			PacketSendUtility.sendPacket(player, new SM_KISK_UPDATE(this));
		}
		player.setKisk(this);
	}

	public void removePlayer(Player player) {
		player.setKisk(null);
		if (kiskMemberIds.remove(player.getObjectId()))
			broadcastKiskUpdate();
	}

	private void broadcastKiskUpdate() {
		// on all members, but not the ones in knownlist, they will receive the update in the next step
		for (Player member : getCurrentMemberList()) {
			if (!getKnownList().knows(member))
				PacketSendUtility.sendPacket(member, new SM_KISK_UPDATE(this));
		}

		// all players having the same race in knownlist
		PacketSendUtility.broadcastPacket(this, new SM_KISK_UPDATE(this), player -> player.getRace() == ownerRace);
	}

	public void broadcastPacket(SM_SYSTEM_MESSAGE message) {
		for (Player member : getCurrentMemberList()) {
			PacketSendUtility.sendPacket(member, message);
		}
	}

	public void resurrectionUsed() {
		remainingResurrections--;
		broadcastKiskUpdate();
		if (remainingResurrections <= 0)
			getController().delete();
	}

	public Race getOwnerRace() {
		return ownerRace;
	}

	public boolean isActive() {
		return !isDead() && getRemainingResurrects() > 0;
	}
}

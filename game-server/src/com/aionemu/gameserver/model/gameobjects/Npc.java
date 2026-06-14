package com.aionemu.gameserver.model.gameobjects;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.movement.NpcMoveController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.loadingutils.adapters.NpcEquipmentList;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.NpcSkillList;
import com.aionemu.gameserver.model.skill.NpcSkillTemplateEntry;
import com.aionemu.gameserver.model.stats.container.NpcGameStats;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.npc.*;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.skillengine.effect.SummonOwner;
import com.aionemu.gameserver.spawnengine.WalkerGroup;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * This class is a base class for all in-game NPCs, what includes: monsters and npcs that player can talk to (aka Citizens)
 * 
 * @author Luno
 */
public class Npc extends Creature {

	private final NpcSkillList skillList;
	private final Queue<NpcSkillEntry> queuedSkills = new LinkedList<>();
	private WalkerGroup walkerGroup;
	private String masterName;
	private int creatorId = 0;
	private CreatureType overriddenType = null;
	private NpcEquippedGear overriddenEquipment;
	private SummonOwner summonOwner = null;

	public Npc(NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
		Objects.requireNonNull(objectTemplate);
		super(IDFactory.getInstance().nextId(), controller, spawnTemplate, objectTemplate, new WorldPosition(spawnTemplate.getWorldId()), true);
		controller.setOwner(this);
		moveController = new NpcMoveController(this);
		skillList = new NpcSkillList(this);
		setupStatContainers();
	}

	@Override
	public NpcMoveController getMoveController() {
		return (NpcMoveController) super.getMoveController();
	}

	protected void setupStatContainers() {
		setGameStats(new NpcGameStats(this));
		setLifeStats(new NpcLifeStats(this));
	}

	@Override
	public NpcTemplate getObjectTemplate() {
		return (NpcTemplate) super.getObjectTemplate();
	}

	public int getNpcId() {
		return getObjectTemplate().getTemplateId();
	}

	@Override
	public byte getLevel() {
		return getObjectTemplate().getLevel();
	}

	public AbyssNpcType getAbyssNpcType() {
		return getObjectTemplate().getAbyssNpcType();
	}

	public NpcRating getRating() {
		return getObjectTemplate().getRating();
	}

	public NpcRank getRank() {
		return getObjectTemplate().getRank();
	}

	public NpcTemplateType getNpcTemplateType() {
		return getObjectTemplate().getNpcTemplateType();
	}

	public int getHpGauge() {
		return getObjectTemplate().getHpGauge();
	}

	@Override
	public NpcLifeStats getLifeStats() {
		return (NpcLifeStats) super.getLifeStats();
	}

	@Override
	public NpcGameStats getGameStats() {
		return (NpcGameStats) super.getGameStats();
	}

	@Override
	public NpcController getController() {
		return (NpcController) super.getController();
	}

	@Override
	public ItemAttackType getAttackType() {
		return getAi().modifyAttackType(ItemAttackType.PHYSICAL);
	}

	public NpcSkillList getSkillList() {
		return skillList;
	}

	public NpcSkillEntry getNextQueuedSkill() {
		synchronized (queuedSkills) {
			return queuedSkills.peek();
		}
	}

	public boolean hasQueuedSkill(Predicate<NpcSkillEntry> filter) {
		synchronized (queuedSkills) {
			return  queuedSkills.stream().anyMatch(filter);
		}
	}

	public void removeNextQueuedSkill(NpcSkillEntry skill) {
		synchronized (queuedSkills) {
			if (queuedSkills.peek() == skill) {
				queuedSkills.poll();
			}
		}
	}

	public void clearQueuedSkills() {
		synchronized (queuedSkills) {
			queuedSkills.clear();
		}
	}

	public void queueSkill(NpcSkillEntry skill) {
		synchronized (queuedSkills) {
			queuedSkills.offer(skill);
		}
	}

	public void queueSkill(int skillId, int level) {
		queueSkill(new NpcSkillTemplateEntry(new QueuedNpcSkillTemplate(skillId, level)));
	}

	public void queueSkill(int skillId, int level, int nextSkillTime) {
		queueSkill(new NpcSkillTemplateEntry(new QueuedNpcSkillTemplate(skillId, level, nextSkillTime, NpcSkillTargetAttribute.MOST_HATED)));
	}

	public void queueSkill(int skillId, int level, int nextSkillTime, NpcSkillTargetAttribute npcSkillTargetAttribute) {
		queueSkill(new NpcSkillTemplateEntry(new QueuedNpcSkillTemplate(skillId, level, nextSkillTime, npcSkillTargetAttribute)));
	}

	public boolean isWalker() {
		return isRandomWalker() || isPathWalker();
	}

	public boolean isRandomWalker() {
		return getSpawn().getRandomWalkRange() > 0;
	}

	public boolean isPathWalker() {
		return getSpawn().getWalkerId() != null;
	}

	@Override
	public TribeClass getTribe() {
		if (getCreator() instanceof Player player)
			return player.getTribe();
		TribeClass transformTribe = isTransformed() ? getTransformModel().getTribe() : null;
		if (transformTribe != null) {
			return transformTribe;
		}
		return getObjectTemplate().getTribe();
	}

	@Override
	public TribeClass getBaseTribe() {
		return DataManager.TRIBE_RELATIONS_DATA.getBaseTribe(getTribe());
	}

	public int getAggroRange() {
		return getAi().modifyAggroRange(getObjectTemplate().getAggroRange());
	}

	public int getShortAggroRange() {
		int aggroRange = getAggroRange();
		return aggroRange < 8 ? aggroRange / 2 : 4;
	}

	public int getAggroAngle() {
		return getAi().modifyAggroAngle(getObjectTemplate().getAggroAngle());
	}

	/**
	 * @return True if the npc is within 1m of it's spawn location
	 */
	public boolean isAtSpawnLocation() {
		return PositionUtil.isInRange(this, getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 1);
	}

	@Override
	public boolean isEnemy(Creature creature) {
		return creature.isEnemyFrom(this) || this.isEnemyFrom(creature);
	}

	@Override
	public boolean isEnemyFrom(Creature creature) {
		return TribeRelationService.isAggressive(creature, this) || TribeRelationService.isHostile(creature, this);
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		return TribeRelationService.isAggressive(this, npc) || TribeRelationService.isHostile(this, npc);
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		return player.isEnemyFrom(this);
	}

	public CreatureType getType(Creature creature) {
		CreatureType type = overriddenType != null ? overriddenType : getRelationBasedType(creature);
		if (creature instanceof Player player) {
			if (player.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_NPCS) && type != CreatureType.ATTACKABLE && type != CreatureType.AGGRESSIVE)
				return CreatureType.ATTACKABLE;
			if (player.isInCustomState(CustomPlayerState.NEUTRAL_TO_ALL_NPCS) && (type == CreatureType.ATTACKABLE || type == CreatureType.AGGRESSIVE))
				return CreatureType.PEACE;
		}
		return type;
	}

	private CreatureType getRelationBasedType(Creature creature) {
		if (TribeRelationService.isNone(this, creature))
			return CreatureType.PEACE;
		else if (TribeRelationService.isAggressive(this, creature))
			return CreatureType.AGGRESSIVE;
		else if (TribeRelationService.isHostile(this, creature))
			return CreatureType.ATTACKABLE;
		else if (TribeRelationService.isFriend(this, creature) || TribeRelationService.isNeutral(this, creature))
			return CreatureType.FRIEND;
		else if (TribeRelationService.isSupport(this, creature))
			return CreatureType.SUPPORT;
		return CreatureType.ATTACKABLE;
	}

	/**
	 * Sets a constant type and broadcasts it, if the npc is spawned. Set to null, to disable it.
	 */
	public void overrideNpcType(CreatureType newType) {
		overriddenType = newType;
		if (isSpawned()) {
			if (overriddenType != null)
				PacketSendUtility.broadcastPacket(this, new SM_CUSTOM_SETTINGS(getObjectId(), 0, overriddenType.getId(), 0));
			else
				getKnownList().forEachPlayer(p -> PacketSendUtility.sendPacket(p, new SM_CUSTOM_SETTINGS(getObjectId(), 0, getType(p).getId(), 0)));
		}
	}

	/**
	 * @return distance to spawn location
	 */
	public double getDistanceToSpawnLocation() {
		return PositionUtil.getDistance(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), getX(), getY(), getZ());
	}

	@Override
	public int getSeeState() {
		int skillSeeState = super.getSeeState();
		int congenitalSeeState = getObjectTemplate().getRating().getCongenitalSeeState().getId();
		return Math.max(skillSeeState, congenitalSeeState);
	}

	/**
	 * @return Name of the Master
	 */
	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

	/**
	 * @return UniqueId of the VisibleObject which created this Npc (could be player or house)
	 */
	public int getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}

	public VisibleObject getCreator() {
		return creatorId == 0 ? null : World.getInstance().findVisibleObject(creatorId);
	}

	public void setWalkerGroup(WalkerGroup wg) {
		this.walkerGroup = wg;
	}

	public WalkerGroup getWalkerGroup() {
		return walkerGroup;
	}

	@Override
	public boolean isFlag() {
		return getObjectTemplate().getNpcTemplateType() == NpcTemplateType.FLAG;
	}

	@Override
	public boolean isRaidMonster() {
		return getObjectTemplate().getNpcTemplateType() == NpcTemplateType.RAID_MONSTER;
	}

	public boolean isBoss() {
		return getObjectTemplate().getRating() == NpcRating.HERO || getObjectTemplate().getRating() == NpcRating.LEGENDARY;
	}

	public boolean hasStatic() {
		return getSpawn().getStaticId() != 0;
	}

	@Override
	public Race getRace() {
		return getObjectTemplate().getRace();
	}

	/**
	 * @return True if this npc sells items.
	 */
	public boolean canSell() {
		return DataManager.TRADE_LIST_DATA.getTradeListTemplate(getNpcId()) != null && getObjectTemplate().supportsAction(DialogAction.BUY);
	}

	/**
	 * @return True if this npc buys items.
	 */
	public boolean canBuy() {
		return getObjectTemplate().supportsAction(DialogAction.SELL) || canSell();
	}

	/**
	 * @return True if this npc trades items for other items.
	 */
	public boolean canTradeIn() {
		return DataManager.TRADE_LIST_DATA.getTradeInListTemplate(getNpcId()) != null && getObjectTemplate().supportsAction(DialogAction.TRADE_IN);
	}

	/**
	 * @return True if this npc buys specific items.
	 */
	public boolean canPurchase() {
		return DataManager.TRADE_LIST_DATA.getPurchaseTemplate(getNpcId()) != null && getObjectTemplate().supportsAction(DialogAction.TRADE_SELL_LIST);
	}

	public GroupDropType getGroupDrop() {
		return getObjectTemplate().getGroupDrop();
	}

	public void overrideEquipmentList(NpcEquipmentList v) {
		overriddenEquipment = new NpcEquippedGear(v);
	}

	@Override
	public NpcEquippedGear getOverrideEquipment() {
		if (overriddenEquipment != null)
			return overriddenEquipment;
		return getObjectTemplate().getEquipment();
	}

	public void setSummonOwner(SummonOwner summonOwner) {
		this.summonOwner = summonOwner;
	}

	public SummonOwner getSummonOwner() {
		return summonOwner;
	}
}

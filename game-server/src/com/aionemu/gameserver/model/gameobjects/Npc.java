package com.aionemu.gameserver.model.gameobjects;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aionemu.gameserver.ai2.AI2Engine;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.movement.NpcMoveController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.NpcSkillList;
import com.aionemu.gameserver.model.stats.container.NpcGameStats;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.GroupDropType;
import com.aionemu.gameserver.model.templates.npc.NpcRank;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOKATOBJECT;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.spawnengine.WalkerGroup;
import com.aionemu.gameserver.spawnengine.WalkerGroupShift;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * This class is a base class for all in-game NPCs, what includes: monsters and npcs that player can talk to (aka Citizens)
 * 
 * @author Luno
 */
public class Npc extends Creature {

	private WalkerGroup walkerGroup;
	private boolean isQuestBusy = false;
	private NpcSkillList skillList;
	private ConcurrentLinkedQueue<NpcSkillEntry> queuedSkills;
	private WalkerGroupShift walkerGroupShift;
	private String masterName = "";
	private int creatorId = 0;
	private int townId;
	private ItemAttackType attacktype = ItemAttackType.PHYSICAL;
	private int aRange = getObjectTemplate().getAggroRange();

	public Npc(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
		super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition(spawnTemplate.getWorldId()));
		Objects.requireNonNull(objectTemplate, "Npcs should be based on template");
		controller.setOwner(this);
		moveController = new NpcMoveController(this);
		skillList = new NpcSkillList(this);
		queuedSkills = new ConcurrentLinkedQueue<>();
		setupStatContainers();

		boolean aiOverride = false;
		if (spawnTemplate.getModel() != null) {
			if (spawnTemplate.getModel().getAi() != null) {
				aiOverride = true;
				AI2Engine.getInstance().setupAI(spawnTemplate.getModel().getAi(), this);
			}
		}

		if (!aiOverride)
			AI2Engine.getInstance().setupAI(objectTemplate.getAi(), this);
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
		return (NpcTemplate) objectTemplate;
	}

	@Override
	public String getName() {
		return getObjectTemplate().getName();
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
		return getAi2().modifyAttackType(attacktype);
	}

	public NpcSkillList getSkillList() {
		return this.skillList;
	}

	public ConcurrentLinkedQueue<NpcSkillEntry> getQueuedSkills() {
		return this.queuedSkills;
	}

	public boolean hasWalkRoutes() {
		return getSpawn().getWalkerId() != null || (getSpawn().hasRandomWalk() && AIConfig.ACTIVE_NPC_MOVEMENT);
	}

	@Override
	public TribeClass getTribe() {
		TribeClass transformTribe = getTransformModel().getTribe();
		if (transformTribe != null) {
			return transformTribe;
		}
		return this.getObjectTemplate().getTribe();
	}

	@Override
	public TribeClass getBaseTribe() {
		return DataManager.TRIBE_RELATIONS_DATA.getBaseTribe(getTribe());
	}

	public int getAggroRange() {
		return getAi2().modifyARange(aRange);
	}

	/**
	 * Check whether npc located near initial spawn location
	 * 
	 * @return true or false
	 */
	public boolean isAtSpawnLocation() {
		return getDistanceToSpawnLocation() < 3;
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
		return player.isEnemyFrom(this) || type == CreatureType.AGGRESSIVE || type == CreatureType.ATTACKABLE;
	}

	@Override
	public CreatureType getType(Creature creature) {
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
		return CreatureType.NULL;
	}

	/**
	 * @return distance to spawn location
	 */
	public double getDistanceToSpawnLocation() {
		return MathUtil.getDistance(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), getX(), getY(), getZ());
	}

	@Override
	public int getSeeState() {
		int skillSeeState = super.getSeeState();
		int congenitalSeeState = getObjectTemplate().getRating().getCongenitalSeeState().getId();
		return Math.max(skillSeeState, congenitalSeeState);
	}

	public boolean getIsQuestBusy() {
		return isQuestBusy;
	}

	public void setIsQuestBusy(boolean busy) {
		isQuestBusy = busy;
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

	public int getTownId() {
		return townId;
	}

	public void setTownId(int townId) {
		this.townId = townId;
	}

	public VisibleObject getCreator() {
		return null;
	}

	@Override
	public void setTarget(VisibleObject creature) {
		if (getTarget() != creature) {
			super.setTarget(creature);
			super.clearAttackedCount();
			getGameStats().renewLastChangeTargetTime();
			if (!getLifeStats().isAlreadyDead()) {
				if (creature != null && !this.equals(creature))
					getPosition().setH(MathUtil.getHeadingTowards(this, creature));
				PacketSendUtility.broadcastPacket(this, new SM_LOOKATOBJECT(this));
			}
		}
	}

	public void setWalkerGroup(WalkerGroup wg) {
		this.walkerGroup = wg;
	}

	public WalkerGroup getWalkerGroup() {
		return walkerGroup;
	}

	public void setWalkerGroupShift(WalkerGroupShift shift) {
		this.walkerGroupShift = shift;
	}

	public WalkerGroupShift getWalkerGroupShift() {
		return walkerGroupShift;
	}

	@Override
	public boolean isFlag() {
		return getObjectTemplate().getNpcTemplateType().equals(NpcTemplateType.FLAG);
	}

	@Override
	public boolean isRaidMonster() {
		return getObjectTemplate().getNpcTemplateType().equals(NpcTemplateType.RAID_MONSTER);
	}

	public boolean isBoss() {
		return getObjectTemplate().getRating() == NpcRating.HERO || getObjectTemplate().getRating() == NpcRating.LEGENDARY;
	}

	public boolean hasStatic() {
		return getSpawn().getStaticId() != 0;
	}

	@Override
	public Race getRace() {
		return this.getObjectTemplate().getRace();
	}

	public NpcDrop getNpcDrop() {
		return getObjectTemplate().getNpcDrop();
	}

	public void setNpcType(CreatureType newType) {
		type = newType;
	}

	public boolean canBuyFrom() {
		return DataManager.TRADE_LIST_DATA.getTradeListTemplate(this.getNpcId()) != null && getObjectTemplate().hasBuyList();
	}

	public boolean canSell() {
		return DataManager.TRADE_LIST_DATA.getTradeListTemplate(this.getNpcId()) != null && getObjectTemplate().hasSellList();
	}

	public boolean canTradeIn() {
		return DataManager.TRADE_LIST_DATA.getTradeInListTemplate(this.getNpcId()) != null && getObjectTemplate().hasTradeInList();
	}

	public boolean canPurchase() {
		return DataManager.TRADE_LIST_DATA.getPurchaseTemplate(this.getNpcId()) != null && getObjectTemplate().hasPurchaseList();
	}

	public GroupDropType getGroupDrop() {
		return getObjectTemplate().getGroupDrop();
	}
}

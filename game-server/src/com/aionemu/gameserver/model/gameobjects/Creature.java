package com.aionemu.gameserver.model.gameobjects;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.ai.AIEngine;
import com.aionemu.gameserver.ai.AbstractAI;
import com.aionemu.gameserver.controllers.CreatureController;
import com.aionemu.gameserver.controllers.ObserveController;
import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.stats.container.CreatureGameStats;
import com.aionemu.gameserver.model.stats.container.CreatureLifeStats;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * This class is representing movable objects, its base class for all in game objects that may move
 * 
 * @author -Nemesiss-
 */
public abstract class Creature extends VisibleObject {

	private final AbstractAI<? extends Creature> ai;
	private CreatureGameStats<? extends Creature> gameStats;
	private CreatureLifeStats<? extends Creature> lifeStats;
	private EffectController effectController;
	protected CreatureMoveController<? extends Creature> moveController;
	private int state = CreatureState.ACTIVE.getId();
	private int visualState = CreatureVisualState.VISIBLE.getId();
	private int seeState = CreatureSeeState.NORMAL.getId();
	private Skill castingSkill;
	private Map<Integer, Long> skillCoolDowns;
	private ObserveController observeController;
	private TransformModel transformModel;
	private final AggroList aggroList;
	private final byte[] zoneTypes = new byte[ZoneType.values().length];
	private int skillNumber;
	private int attackedCount;
	private long spawnTime = System.currentTimeMillis();

	public Creature(int objId, CreatureController<? extends Creature> controller, SpawnTemplate spawnTemplate, CreatureTemplate objectTemplate,
		WorldPosition position, boolean autoReleaseObjectId) {
		super(objId, controller, spawnTemplate, objectTemplate, position, autoReleaseObjectId);
		String aiName = objectTemplate.getAiName();
		if (spawnTemplate != null && spawnTemplate.getAiName() != null)
			aiName = SpawnTemplate.NO_AI.equals(spawnTemplate.getAiName()) ? null : spawnTemplate.getAiName();
		this.ai = AIEngine.getInstance().newAI(aiName, this);
		this.observeController = new ObserveController();
		this.aggroList = createAggroList();
	}

	public CreatureMoveController<? extends Creature> getMoveController() {
		return moveController;
	}

	protected AggroList createAggroList() {
		return new AggroList(this);
	}

	@Override
	public CreatureController<? extends Creature> getController() {
		return (CreatureController<?>) super.getController();
	}

	public CreatureLifeStats<? extends Creature> getLifeStats() {
		return lifeStats;
	}

	public void setLifeStats(CreatureLifeStats<? extends Creature> lifeStats) {
		this.lifeStats = lifeStats;
	}

	public CreatureGameStats<? extends Creature> getGameStats() {
		return gameStats;
	}

	public void setGameStats(CreatureGameStats<? extends Creature> gameStats) {
		this.gameStats = gameStats;
	}

	public abstract byte getLevel();

	public EffectController getEffectController() {
		return effectController;
	}

	public void setEffectController(EffectController effectController) {
		this.effectController = effectController;
	}

	public AbstractAI<? extends Creature> getAi() {
		return ai;
	}

	public boolean isDead() {
		return lifeStats.isDead();
	}

	/**
	 * @return True if the creature is a flag (symbol on map)
	 */
	public boolean isFlag() {
		return false;
	}

	public boolean isCasting() {
		return castingSkill != null;
	}

	/**
	 * Set current casting skill or null when skill ends
	 */
	public void setCasting(Skill castingSkill) {
		if (castingSkill != null)
			skillNumber++;
		this.castingSkill = castingSkill;
	}

	public int getCastingSkillId() {
		return castingSkill != null ? castingSkill.getSkillTemplate().getSkillId() : 0;
	}

	public Skill getCastingSkill() {
		return castingSkill;
	}

	public int getSkillNumber() {
		return skillNumber;
	}

	public void setSkillNumber(int skillNumber) {
		this.skillNumber = skillNumber;
	}

	public int getAttackedCount() {
		return this.attackedCount;
	}

	public void incrementAttackedCount() {
		this.attackedCount++;
	}

	public void clearAttackedCount() {
		attackedCount = 0;
	}

	/**
	 * All abnormal effects are checked that disable movements
	 */
	public boolean canPerformMove() {
		return (!(getEffectController().isInAnyAbnormalState(AbnormalState.CANT_MOVE_STATE) && isSpawned() && canUseSkillInMove()));
	}

	private boolean canUseSkillInMove() {
		if (castingSkill != null) {
			SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(castingSkill.getSkillId());
			if (st.getStartconditions() != null && st.getMovedCondition() != null) {
				if (!st.getMovedCondition().isAllow())
					return false;
			}
		}
		return true;
	}

	/**
	 * All abnormal effects are checked that disable attack
	 */
	public boolean canAttack() {
		return (!getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE) && !isCasting() && !isInState(CreatureState.RESTING)
			&& !isInState(CreatureState.PRIVATE_SHOP));
	}

	public int getState() {
		return state;
	}

	/**
	 * Sets the given state while keeping all present ones
	 */
	public void setState(CreatureState state) {
		setState(state, false);
	}

	/**
	 * Sets the given state. If {@code replace} is true, previous states will be completely replaced.
	 */
	public void setState(CreatureState state, boolean replace) {
		if (replace)
			this.state = state.getId();
		else
			this.state |= state.getId();
	}

	/**
	 * @param state
	 *          taken usually from templates
	 */
	public void setState(int state) {
		this.state = state;
	}

	public void unsetState(CreatureState state) {
		this.state &= ~state.getId();
	}

	public boolean isInState(CreatureState state) {
		if (state.mustMatchExact())
			return this.state == state.getId();
		else
			return (this.state & state.getId()) == state.getId();
	}

	public int getVisualState() {
		return visualState;
	}

	public void setVisualState(CreatureVisualState visualState) {
		this.visualState |= visualState.getId();
	}

	public void unsetVisualState(CreatureVisualState visualState) {
		this.visualState &= ~visualState.getId();
	}

	public boolean isInVisualState(CreatureVisualState visualState) {
		return (this.visualState & visualState.getId()) == visualState.getId();
	}

	public boolean isInAnyHide() {
		return visualState != CreatureVisualState.VISIBLE.getId() && visualState != CreatureVisualState.BLINKING.getId();
	}

	public int getSeeState() {
		return seeState;
	}

	public void setSeeState(CreatureSeeState seeState) {
		this.seeState |= seeState.getId();
	}

	public void unsetSeeState(CreatureSeeState seeState) {
		this.seeState &= ~seeState.getId();
	}

	public boolean isInSeeState(CreatureSeeState seeState) {
		int isSeeState = this.seeState & seeState.getId();

		if (isSeeState == seeState.getId())
			return true;

		return false;
	}

	public TransformModel getTransformModel() {
		if (transformModel == null)
			transformModel = new TransformModel(this);
		return transformModel;
	}

	public void endTransformation() {
		getTransformModel().apply(0);
	}

	public boolean isTransformed() {
		return transformModel != null && getTransformModel().isActive();
	}

	public final AggroList getAggroList() {
		return aggroList;
	}

	public ObserveController getObserveController() {
		return observeController;
	}

	public boolean isEnemy(Creature creature) {
		return creature.isEnemyFrom(this);
	}

	public boolean isEnemyFrom(Creature creature) {
		return false;
	}

	public boolean isEnemyFrom(Player player) {
		return false;
	}

	public boolean isEnemyFrom(Npc npc) {
		return false;
	}

	public TribeClass getTribe() {
		return TribeClass.GENERAL;
	}

	public TribeClass getBaseTribe() {
		return TribeClass.GENERAL;
	}

	@Override
	public boolean canSee(VisibleObject object) {
		if (object instanceof Creature creature) {
			int visualStateExcludingBlinking = creature.getVisualState() & ~CreatureVisualState.BLINKING.getId();
			if (visualStateExcludingBlinking <= getSeeState())
				return true;
			return equals(creature.getMaster()); // traps, summons, etc. should always be visible to the master
		} else if (object instanceof Pet pet) {
			// we must prevent sending the pet's spawn packet to others before the master's, as this causes the pet to stay invisible
			return equals(pet.getMaster()) || canSee(pet.getMaster()) && getKnownList().sees(pet.getMaster());
		}
		return super.canSee(object);
	}

	/**
	 * @return NpcObjectType.NORMAL
	 */
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.NORMAL;
	}

	/**
	 * For summons and different kind of servants<br>
	 * it will return currently acting player.<br>
	 * This method is used for duel and enemy relations,<br>
	 * rewards<br>
	 * 
	 * @return Master of this creature or self
	 */
	public Creature getMaster() {
		return this;
	}

	/**
	 * For summons it will return summon object and for <br>
	 * servants - player object.<br>
	 * Used to find attackable target for npcs.<br>
	 * 
	 * @return acting master - player in case of servants
	 */
	public Creature getActingCreature() {
		return getMaster();
	}

	public boolean isSkillDisabled(SkillTemplate template) {
		if (skillCoolDowns == null)
			return false;

		int cooldownId = template.getCooldownId();
		Long coolDown = skillCoolDowns.get(cooldownId);
		if (coolDown == null) {
			return false;
		}

		if (coolDown < System.currentTimeMillis()) {
			removeSkillCoolDown(cooldownId);
			return false;
		}
		return true;
	}

	public long getSkillCoolDown(int cooldownId) {
		return skillCoolDowns == null ? 0L : skillCoolDowns.getOrDefault(cooldownId, 0L);
	}

	public void setSkillCoolDown(int cooldownId, long time) {
		if (cooldownId == 0) {
			return;
		}
		if (skillCoolDowns == null)
			skillCoolDowns = new ConcurrentHashMap<>();
		skillCoolDowns.put(cooldownId, time);
	}

	public Map<Integer, Long> getSkillCoolDowns() {
		return skillCoolDowns;
	}

	public void removeSkillCoolDown(int cooldownId) {
		if (skillCoolDowns == null)
			return;
		skillCoolDowns.remove(cooldownId);
	}

	/**
	 * @return True if this creature can not receive any damage.
	 */
	public boolean isInvulnerable() {
		return false;
	}

	public ItemAttackType getAttackType() {
		return ItemAttackType.PHYSICAL;
	}

	/**
	 * Creature is flying (FLY or GLIDE states)
	 */
	public boolean isFlying() {
		return (isInState(CreatureState.FLYING) && !isInState(CreatureState.RESTING)) || isInState(CreatureState.GLIDING);
	}

	public boolean isInFlyingState() {
		return isInState(CreatureState.FLYING) && !isInState(CreatureState.RESTING);
	}

	public boolean isPvpTarget(Creature creature) {
		return false;
	}

	/**
	 * @return All zones the the creature currently is in (even if not currently spawned, so make sure to check isSpawned yourself if needed).
	 */
	public List<ZoneInstance> findZones() {
		return getPosition().getMapRegion() == null ? Collections.emptyList() : getPosition().getMapRegion().findZones(this);
	}

	public void revalidateZones() {
		if (!isSpawned())
			return;
		MapRegion mapRegion = getPosition().getMapRegion();
		if (mapRegion != null)
			mapRegion.revalidateZones(this);
	}

	public boolean isInsideZone(ZoneName zoneName) {
		if (!isSpawned())
			return false;
		return getPosition().getMapRegion().isInsideZone(zoneName, this);
	}

	public boolean isInsideItemUseZone(ZoneName zoneName) {
		if (!isSpawned())
			return false;
		return getPosition().getMapRegion().isInsideItemUseZone(zoneName, this);
	}

	/**
	 * Increments an internal counter for the given zone type, to support nested zones
	 */
	public void setInsideZoneType(ZoneType zoneType) {
		synchronized (zoneTypes) {
			zoneTypes[zoneType.ordinal()]++;
		}
	}

	/**
	 * Decrements an internal counter for the given zone type, to support nested zones
	 */
	public void unsetInsideZoneType(ZoneType zoneType) {
		synchronized (zoneTypes) {
			zoneTypes[zoneType.ordinal()]--;
		}
	}

	/**
	 * @return True, if the creature is inside one or more zones of the specified type.
	 */
	public boolean isInsideZoneType(ZoneType zoneType) {
		synchronized (zoneTypes) {
			return zoneTypes[zoneType.ordinal()] > 0;
		}
	}

	public boolean isInsidePvPZone() {
		synchronized (zoneTypes) {
			if (zoneTypes[ZoneType.SIEGE.ordinal()] > 0) {
				return true;
			}
			int pvpValue = zoneTypes[ZoneType.PVP.ordinal()];
			return pvpValue == 0 || pvpValue == 2;
		}
	}

	public Race getRace() {
		return Race.NONE;
	}

	public int getSkillCooldown(SkillTemplate template) {
		return template.getCooldown();
	}

	public long getMillisSinceSpawn() {
		return System.currentTimeMillis() - spawnTime;
	}

	public boolean isNewSpawn() {
		return getMillisSinceSpawn() < 1500;
	}

	public boolean isRaidMonster() {
		return false;
	}

	public boolean isWorldRaidMonster() {
		return getTribe() == TribeClass.WORLDRAID_MONSTER || getTribe() == TribeClass.WORLDRAID_MONSTER_SANDWORMSUM && isRaidMonster();
	}

	public NpcEquippedGear getOverrideEquipment() {
		return null;
	}

}

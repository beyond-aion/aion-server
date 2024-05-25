package com.aionemu.gameserver.model.skill;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillCondition;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillSpawn;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.SignetBurstEffect;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Skill entry which inherits properties from template (regular npc skills)
 *
 * @author ATracer, nrg, Yeats
 */
public class NpcSkillTemplateEntry extends NpcSkillEntry {

	private final NpcSkillTemplate template;

	public NpcSkillTemplateEntry(NpcSkillTemplate template) {
		super(template.getSkillId(), template.getSkillLevel());
		this.template = template;
	}

	@Override
	public boolean isReady(int hpPercentage, long fightingTimeInMSec) {
		if (hasCooldown() || !chanceReady())
			return false;

		return switch (template.getConjunctionType()) {
			case XOR -> (hpReady(hpPercentage) && !timeReady(fightingTimeInMSec)) || (!hpReady(hpPercentage) && timeReady(fightingTimeInMSec));
			case OR -> hpReady(hpPercentage) || timeReady(fightingTimeInMSec);
			case AND -> hpReady(hpPercentage) && timeReady(fightingTimeInMSec);
		};
	}

	@Override
	public boolean chanceReady() {
		return Rnd.chance() < template.getProbability();
	}

	@Override
	public boolean hpReady(int hpPercentage) {
		if (template.getMaxhp() == 100 && template.getMinhp() == 0) // it's not about hp
			return true;
		else if (template.getMaxhp() >= hpPercentage && template.getMinhp() <= hpPercentage) // in hp range
			return true;
		else
			return false;
	}

	@Override
	public boolean timeReady(long elapsedFightTime) {
		long minTime = template.getMinTime();
		long maxTime = template.getMaxTime();
		return maxTime == 0 && minTime == 0 || maxTime == 0 && minTime <= elapsedFightTime || maxTime >= elapsedFightTime && minTime <= elapsedFightTime;
	}

	@Override
	public boolean hasCooldown() {
		return template.getCooldown() > (System.currentTimeMillis() - lastTimeUsed);
	}

	@Override
	public boolean hasPostSpawnCondition() {
		return template.isPostSpawn();
	}

	@Override
	public int getPriority() {
		return template.getPriority();
	}

	@Override
	public boolean conditionReady(Creature creature) {
		if (creature == null || creature.isDead() || creature.getLifeStats().isAboutToDie()) {
			return false;
		}
		NpcSkillConditionTemplate condTemp = getConditionTemplate();
		if (condTemp == null)
			return true;
		VisibleObject curTarget = creature.getTarget();
		switch (condTemp.getCondType()) {
			case NONE:
				return true;
			case SELECT_TARGET_AFFECTED_BY_SKILL:
				for (VisibleObject obj : creature.getKnownList().getKnownObjects().values()) {
					if (obj instanceof Creature target) {
						if (target.isDead() || target.getLifeStats().isAboutToDie())
							continue;
						if (creature.canSee(target) && target.getEffectController().hasAbnormalEffect(condTemp.getSkillId())
							&& PositionUtil.isInRange(creature, target, condTemp.getRange(), false) && GeoService.getInstance().canSee(creature, target)) {
							creature.setTarget(target);
							return true;
						}
					}
				}
				return false;
			case HELP_FRIEND:
				for (VisibleObject obj : creature.getKnownList().getKnownObjects().values()) {
					if (obj instanceof Creature target) {
						if (target.isDead() || target.getLifeStats().isAboutToDie())
							continue;
						if ((TribeRelationService.isSupport(creature, target) || TribeRelationService.isFriend(creature, target))
							&& target.getLifeStats().getHpPercentage() <= condTemp.getHpBelow() && creature.canSee(target)
							&& PositionUtil.isInRange(creature, target, condTemp.getRange(), false) && GeoService.getInstance().canSee(creature, target)) {
							creature.setTarget(target);
							return true;
						}
					}
				}
				return false;
			case TARGET_IS_AETHERS_HOLD:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.OPENAERIAL);
				}
				return false;
			case TARGET_IS_STUNNED:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.STUN);
				}
				return false;
			case TARGET_IS_IN_ANY_STUN:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.ANY_STUN);
				}
				return false;
			case TARGET_IS_IN_STUMBLE:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.STUMBLE);
				}
				return false;
			case TARGET_IS_SLEEPING:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.SLEEP);
				}
				return false;
			case TARGET_IS_FLYING:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).isInFlyingState();
				}
				return false;
			case TARGET_IS_POISONED:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.POISON);
				}
				return false;
			case TARGET_IS_BLEEDING:
				if (curTarget instanceof Creature) {
					return ((Creature) curTarget).getEffectController().isInAnyAbnormalState(AbnormalState.BLEED);
				}
				return false;
			case TARGET_IS_GATE:
				if (curTarget instanceof Creature) {
					NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(curTarget.getObjectId());
					return template != null && template.getAbyssNpcType() == AbyssNpcType.DOOR;
				}
				return false;
			case TARGET_IS_PLAYER:
				return curTarget instanceof Player;
			case TARGET_IS_NPC:
				return curTarget instanceof Npc;
			case TARGET_IS_MAGICAL_CLASS:
				return curTarget instanceof Player && !((Player) curTarget).getPlayerClass().isPhysicalClass();
			case TARGET_IS_PHYSICAL_CLASS:
				return curTarget instanceof Player && ((Player) curTarget).getPlayerClass().isPhysicalClass();
			case TARGET_HAS_CARVED_SIGNET:
				return hasCarvedSignet(curTarget, template.getSkillTemplate(), 0);
			case TARGET_HAS_CARVED_SIGNET_LEVEL_II:
				return hasCarvedSignet(curTarget, template.getSkillTemplate(), 1);
			case TARGET_HAS_CARVED_SIGNET_LEVEL_III:
				return hasCarvedSignet(curTarget, template.getSkillTemplate(), 2);
			case TARGET_HAS_CARVED_SIGNET_LEVEL_IV:
				return hasCarvedSignet(curTarget, template.getSkillTemplate(), 3);
			case TARGET_HAS_CARVED_SIGNET_LEVEL_V:
				return hasCarvedSignet(curTarget, template.getSkillTemplate(), 4);
			case NPC_IS_ALIVE:
				VisibleObject object = creature.getKnownList().findObject(condTemp.getNpcId());
				return object instanceof Creature npc && !npc.isDead();
			case TARGET_IS_IN_RANGE:
				if (curTarget instanceof Creature target && (target.isDead() || target.getLifeStats().isAboutToDie()))
					return false;
				if (!creature.canSee(curTarget) || !PositionUtil.isInRange(creature, curTarget, condTemp.getRange(), false))
					return false;
				if (!GeoService.getInstance().canSee(creature, curTarget))
					return false;
				return true;
			default:
				return true;
		}
	}

	private boolean hasCarvedSignet(VisibleObject curTarget, SkillTemplate skillTemp, int signetLvl) {
		if (skillTemp != null && curTarget instanceof Creature && !((Creature) curTarget).isDead()
			&& !((Creature) curTarget).getLifeStats().isAboutToDie()) {
			for (EffectTemplate effectTemp : skillTemp.getEffects().getEffects()) {
				if (effectTemp instanceof SignetBurstEffect signetEffect) {
					String signet = signetEffect.getSignet();
					Creature target = (Creature) curTarget;
					Effect signetEffectOnTarget = target.getEffectController().getAbnormalEffect(signet);
					if (signetEffectOnTarget != null && signetEffectOnTarget.getSkillLevel() > signetLvl) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void fireOnEndCastEvents(Npc npc) {
		NpcSkillSpawn spawn = template.getSpawn();
		if (spawn == null || npc.isDead() || npc.getLifeStats().isAboutToDie())
			return;
		if (spawn.getDelay() == 0)
			spawnNpc(npc, spawn);
		else
			ThreadPoolManager.getInstance().schedule(() -> {
				if (!npc.isDead() && !npc.getLifeStats().isAboutToDie())
					spawnNpc(npc, spawn);
			}, spawn.getDelay());
	}

	private void spawnNpc(Npc npc, NpcSkillSpawn spawn) {
		int count = spawn.getMaxCount() > 1 ? Rnd.get(spawn.getMinCount(), spawn.getMaxCount()) : spawn.getMinCount();
		for (int i = 0; i < count; i++) {
			float x1 = 0;
			float y1 = 0;
			if (spawn.getMinDistance() > 0) {
				double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
				double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(npc.getHeading())) + angleRadians;
				float distance = spawn.getMaxDistance() > 0 ? Rnd.get(spawn.getMinDistance(), spawn.getMaxDistance()) : spawn.getMinDistance();
				x1 = (float) (Math.cos(radian) * distance);
				y1 = (float) (Math.sin(radian) * distance);
			}
			SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(npc.getWorldId(), spawn.getNpcId(), npc.getX() + x1, npc.getY() + y1, npc.getZ(),
				npc.getHeading(), npc.getObjectId());
			SpawnEngine.spawnObject(template, npc.getInstanceId());
		}
	}

	@Override
	public NpcSkillConditionTemplate getConditionTemplate() {
		return template.getConditionTemplate();
	}

	@Override
	public boolean hasCondition() {
		return getConditionTemplate() != null && getConditionTemplate().getCondType() != NpcSkillCondition.NONE;
	}

	@Override
	public int getNextSkillTime() {
		return template.getNextSkillTime();
	}

	@Override
	public boolean hasChain() {
		return template.getNextChainId() > 0;
	}

	@Override
	public int getNextChainId() {
		return template.getNextChainId();
	}

	@Override
	public int getChainId() {
		return template.getChainId();
	}

	@Override
	public NpcSkillTemplate getTemplate() {
		return template;
	}

	@Override
	public boolean canUseNextChain(Npc owner) {
		return owner != null && (System.currentTimeMillis() - owner.getGameStats().getLastSkillTime()) < template.getMaxChainTime();
	}

}

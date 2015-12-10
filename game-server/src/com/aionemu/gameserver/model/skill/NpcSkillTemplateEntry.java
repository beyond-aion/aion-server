package com.aionemu.gameserver.model.skill;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillCondition;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.services.TribeRelationService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.EffectType;
import com.aionemu.gameserver.skillengine.effect.SignetBurstEffect;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Skill entry which inherits properties from template (regular npc skills)
 * 
 * @author ATracer, nrg
 * @reworked Yeats
 */
public class NpcSkillTemplateEntry extends NpcSkillEntry {

	private final NpcSkillTemplate template;

	public NpcSkillTemplateEntry(NpcSkillTemplate template) {
		super(template.getSkillid(), template.getSkillLevel());
		this.template = template;
	}

	@Override
	public boolean isReady(int hpPercentage, long fightingTimeInMSec) {
		if (hasCooldown() || !chanceReady())
			return false;

		switch (template.getConjunctionType()) {
			case XOR:
				return (hpReady(hpPercentage) && !timeReady(fightingTimeInMSec)) || (!hpReady(hpPercentage) && timeReady(fightingTimeInMSec));
			case OR:
				return hpReady(hpPercentage) || timeReady(fightingTimeInMSec);
			case AND:
				return hpReady(hpPercentage) && timeReady(fightingTimeInMSec);
			default:
				return false;
		}
	}

	@Override
	public boolean chanceReady() {
		return Rnd.get(0, 100) < template.getProbability();
	}

	@Override
	public boolean hpReady(int hpPercentage) {
		if (template.getMaxhp() == 0 && template.getMinhp() == 0) // it's not about hp
			return true;
		else if (template.getMaxhp() >= hpPercentage && template.getMinhp() <= hpPercentage) // in hp range
			return true;
		else
			return false;
	}

	@Override
	public boolean timeReady(long fightingTimeInMSec) {
		if (template.getMaxTime() == 0 && template.getMinTime() == 0) // it's not about time
			return true;
		else if (template.getMaxTime() >= fightingTimeInMSec && template.getMinTime() <= fightingTimeInMSec) // in time range
			return true;
		else
			return false;
	}

	@Override
	public boolean hasCooldown() {
		return template.getCooldown() > (System.currentTimeMillis() - lastTimeUsed);
	}

	@Override
	public boolean UseInSpawned() {
		return template.getUseInSpawned();
	}

	@Override
	public int getPriority() {
		return template.getPriority();
	}

	@Override
	public boolean conditionReady(Creature creature) {
		NpcSkillConditionTemplate condTemp = template.getConditionTemplate();	
		if (creature == null) {
			return false;
		} else if (condTemp == null) {
			return true;
		}
		
		VisibleObject curTarget = creature.getTarget();
		NpcSkillCondition condType = condTemp.getCondType();
		switch (condType) {
			case NONE:
				return true;
			case SELECT_RANDOM_ENEMY_EXCEPT_MOST_HATED:
			case SELECT_RANDOM_ENEMY:
				for (VisibleObject obj : creature.getKnownList().getKnownObjects().values()) {
					if (obj != null && obj instanceof Creature) {
						Creature target = (Creature) obj;
						if (condType == NpcSkillCondition.SELECT_RANDOM_ENEMY_EXCEPT_MOST_HATED 
								&& creature.getAggroList().getMostHated().equals(target)) {
								continue;
						}
						if (creature.isEnemy(target) && creature.canSee(target)
								&& MathUtil.isIn3dRange(creature, target, condTemp.getRange())
								&& GeoService.getInstance().canSee(creature, target)) {
								creature.setTarget(target);
								return true;
						}
					}
				}
				return false;
			case HELP_SELF:
				if (creature != null && !creature.getLifeStats().isAlreadyDead()) {
					if (creature.getLifeStats().getHpPercentage() <= condTemp.getHpBelow()) {
						creature.setTarget(creature);
						return true;
					}
				}
				return false;
			case HELP_FRIEND:
				for (VisibleObject obj : creature.getKnownList().getKnownObjects().values()) {
					if (obj != null && obj instanceof Creature) {
						Creature target = (Creature) obj;
						if (target.getLifeStats().isAlreadyDead()) 
							continue;
						if ((TribeRelationService.isSupport(creature, target) || TribeRelationService.isFriend(creature, target)) 
								&& target.getLifeStats().getHpPercentage() <= condTemp.getHpBelow() && creature.canSee(target) 
								&& MathUtil.isIn3dRange(creature, target, condTemp.getRange())
								&& GeoService.getInstance().canSee(creature, target)) {
								creature.setTarget(target);
								return true;
						}
					}
				}
				return false;
			case TARGET_IS_AETHERS_HOLD:
				if (curTarget != null && curTarget instanceof Creature) {
					Creature target = (Creature) curTarget;
					return target.getEffectController().isInAnyAbnormalState(AbnormalState.OPENAERIAL);
				}
				return false;
			case TARGET_IS_STUNNED:
				if (curTarget != null && curTarget instanceof Creature) {
					Creature target = (Creature) curTarget;
					if (target.getEffectController().isInAnyAbnormalState(AbnormalState.ANY_STUN)) {
						return true;
					}
				}
				return false;
			case TARGET_IS_SLEEPING:
				if (curTarget != null && curTarget instanceof Creature) {
					Creature target = (Creature) curTarget;
					return target.getEffectController().isInAnyAbnormalState(AbnormalState.SLEEP);
				}
				return false;
			case TARGET_IS_POISONED:
				if (curTarget != null && curTarget instanceof Creature) {
					Creature target = (Creature) curTarget;
					return target.getEffectController().isInAnyAbnormalState(AbnormalState.POISON);
				}
				return false;
			case TARGET_IS_BLEEDING:
				if (curTarget != null && curTarget instanceof Creature) {
					Creature target = (Creature) curTarget;
					return target.getEffectController().isInAnyAbnormalState(AbnormalState.BLEED);
				}
				return false;
			case TARGET_IS_GATE:
				if (curTarget != null && curTarget instanceof Creature) {
					Creature target = (Creature) curTarget;
					NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(target.getObjectId());
					if (template != null && template.getAbyssNpcType() == AbyssNpcType.DOOR) {
						return true;
					}
				}
				return false;
			case TARGET_IS_PLAYER:
				if (curTarget != null && curTarget instanceof Player) {
					return true;
				}
				return false;
			case TARGET_IS_MAGICAL_CLASS:
			case TARGET_IS_PHYSICAL_CLASS:
				boolean magical = condType == NpcSkillCondition.TARGET_IS_PHYSICAL_CLASS ? false : true;
				if (curTarget != null && curTarget instanceof Player) {
					Player player = (Player) curTarget;
					if (magical && player.getPlayerClass() == PlayerClass.MAGICAL_CLASS) {
						return true;
					} else if (!magical && player.getPlayerClass() == PlayerClass.PHYSICAL_CLASS) {
						return true;
					}
				}
				return false;
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
			default:
				return true;
		}
		
	}

	private boolean hasCarvedSignet(VisibleObject curTarget, SkillTemplate skillTemp, int signetLvl) {
		if (curTarget != null && curTarget instanceof Creature) {
			if (skillTemp != null && skillTemp.getEffects().getEffectTypes().contains(EffectType.SIGNETBURST)) {
				for (EffectTemplate effectTemp : skillTemp.getEffects().getEffects()) {
					if (effectTemp instanceof SignetBurstEffect) {
						SignetBurstEffect signetEffect = (SignetBurstEffect) effectTemp;
						String signet = signetEffect.getSignet();
						Creature target = (Creature) curTarget;
						Effect signetEffectOnTarget = target.getEffectController().getAnormalEffect(signet);
						if (signetEffectOnTarget != null && signetEffectOnTarget.getSkillLevel() > signetLvl) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public NpcSkillCondition getCondition() {
		return template.getConditionTemplate().getCondType();
	}

	@Override
	public NpcSkillConditionTemplate getConditionTemplate() {
		return template.getConditionTemplate();
	}

	@Override
	public boolean hasCondition() {
		if (getConditionTemplate() != null && getConditionTemplate().getCondType() != NpcSkillCondition.NONE) {
			return true;
		}
		return false;
	}

	@Override
	public int getRange() {
		return getConditionTemplate().getRange();
	}

	@Override
	public int getHpBelow() {
		return getConditionTemplate().getHpBelow();
	}

	@Override
	public int getNextSkillTime() {
		return template.getNextSkillTime();
	}
}

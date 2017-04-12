package com.aionemu.gameserver.model.skill;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillCondition;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
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
 * @author ATracer, nrg
 * @reworked Yeats
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
		return Rnd.chance() < template.getProbability();
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
		return template.isPostSpawn();
	}

	@Override
	public int getPriority() {
		return template.getPriority();
	}

	@Override
	public boolean conditionReady(Creature creature) {
		if (creature == null || creature.getLifeStats().isAlreadyDead() || creature.getLifeStats().isAboutToDie()) {
			return false;
		}
		NpcSkillConditionTemplate condTemp = getConditionTemplate();
		if (condTemp == null)
			return true;
		VisibleObject curTarget = creature.getTarget();
		NpcSkillCondition condType = condTemp.getCondType();
		switch (condType) {
			case NONE:
				return true;
			case SELECT_TARGET_AFFECTED_BY_SKILL:
				for (VisibleObject obj : creature.getKnownList().getKnownObjects().values()) {
					if (obj instanceof Creature) {
						Creature target = (Creature) obj;
						if (target.getLifeStats().isAlreadyDead() || target.getLifeStats().isAboutToDie())
							continue;
						if (creature.canSee(target) && target.getEffectController().hasAbnormalEffect(condTemp.getSkillId())
							&& PositionUtil.isInRange(creature, target, condTemp.getRange()) && GeoService.getInstance().canSee(creature, target)) {
							creature.setTarget(target);
							return true;
						}
					}
				}
				return false;
			case HELP_FRIEND:
				for (VisibleObject obj : creature.getKnownList().getKnownObjects().values()) {
					if (obj instanceof Creature) {
						Creature target = (Creature) obj;
						if (target.getLifeStats().isAlreadyDead() || target.getLifeStats().isAboutToDie())
							continue;
						if ((TribeRelationService.isSupport(creature, target) || TribeRelationService.isFriend(creature, target))
							&& target.getLifeStats().getHpPercentage() <= condTemp.getHpBelow() && creature.canSee(target)
							&& PositionUtil.isInRange(creature, target, condTemp.getRange()) && GeoService.getInstance().canSee(creature, target)) {
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
					NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(((Creature) curTarget).getObjectId());
					if (template != null && template.getAbyssNpcType() == AbyssNpcType.DOOR) {
						return true;
					}
				}
				return false;
			case TARGET_IS_PLAYER:
				if (curTarget instanceof Player) {
					return true;
				}
				return false;
			case TARGET_IS_MAGICAL_CLASS:
			case TARGET_IS_PHYSICAL_CLASS:
				boolean magical = condType == NpcSkillCondition.TARGET_IS_PHYSICAL_CLASS ? false : true;
				if (curTarget instanceof Player) {
					if (magical && ((Player) curTarget).getPlayerClass() == PlayerClass.MAGICAL_CLASS) {
						return true;
					} else if (!magical && ((Player) curTarget).getPlayerClass() == PlayerClass.PHYSICAL_CLASS) {
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
			case NPC_IS_ALIVE:
				VisibleObject npc = creature.getKnownList().findObject(condTemp.getNpcId());
				if (npc instanceof Npc)
					return !((Npc) npc).getLifeStats().isAlreadyDead();
				return false;
			default:
				return true;
		}
	}

	private boolean hasCarvedSignet(VisibleObject curTarget, SkillTemplate skillTemp, int signetLvl) {
		if (skillTemp != null && curTarget instanceof Creature && !((Creature) curTarget).getLifeStats().isAlreadyDead()
			&& !((Creature) curTarget).getLifeStats().isAboutToDie()) {
			for (EffectTemplate effectTemp : skillTemp.getEffects().getEffects()) {
				if (effectTemp instanceof SignetBurstEffect) {
					SignetBurstEffect signetEffect = (SignetBurstEffect) effectTemp;
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
	public void fireOnStartCastEvents(Npc npc) {
		NpcSkillConditionTemplate condTemp = getConditionTemplate();
		if (condTemp == null) {
			if (!npc.getLifeStats().isAboutToDie() && !npc.getLifeStats().isAlreadyDead()) {
				npc.getAi().onStartUseSkill(this);
			}
			return;
		}
	}

	@Override
	public void fireOnEndCastEvents(Npc npc) {
		NpcSkillConditionTemplate condTemp = getConditionTemplate();
		if (condTemp == null) {
			if (!npc.getLifeStats().isAboutToDie() && !npc.getLifeStats().isAlreadyDead()) {
				npc.getAi().onEndUseSkill(this);
			}
			return;
		}
		NpcSkillCondition condType = condTemp.getCondType();
		switch (condType) {
			case SPAWN_NPC:
				if (condTemp.getDelay() > 0) {
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (npc == null || npc.getLifeStats().isAlreadyDead() || npc.getLifeStats().isAboutToDie()) {
								return;
							}
							int amount = condTemp.getMaxAmount() > 1 ? Rnd.get(condTemp.getMinAmount(), condTemp.getMaxAmount()) : condTemp.getMinAmount();
							for (int i = 0; i < amount; i++) {
								float x1 = 0;
								float y1 = 0;
								if (condTemp.getMinDistance() > 0) {
									float direction = condTemp.isRandomDirection() ? Rnd.get(0, 199) / 100f : condTemp.getDirection() / 100f;
									double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(npc.getHeading()));
									float distance = condTemp.getMaxDistance() > 0 ? Rnd.get(condTemp.getMinDistance(), condTemp.getMaxDistance())
										: condTemp.getMinDistance();
									x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
									y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
								}
								SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(npc.getWorldId(), condTemp.getNpcId(), npc.getX() + x1, npc.getY() + y1,
									npc.getZ(), npc.getHeading());
								if (template != null) {
									template.setCreatorId(npc.getObjectId());
									SpawnEngine.spawnObject(template, npc.getInstanceId());
								} else {
									break;
								}
							}
						}
					}, condTemp.getDelay());
				} else {
					if (npc == null || npc.getLifeStats().isAlreadyDead() || npc.getLifeStats().isAboutToDie()) {
						return;
					}
					int amount = condTemp.getMaxAmount() > 1 ? Rnd.get(condTemp.getMinAmount(), condTemp.getMaxAmount()) : condTemp.getMinAmount();
					for (int i = 0; i < amount; i++) {
						float x1 = 0;
						float y1 = 0;
						if (condTemp.getMinDistance() > 0) {
							float direction = condTemp.isRandomDirection() ? Rnd.get(0, 199) / 100f : condTemp.getDirection() / 100f;
							double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(npc.getHeading()));
							float distance = condTemp.getMaxDistance() > 0 ? Rnd.get(condTemp.getMinDistance(), condTemp.getMaxDistance())
								: condTemp.getMinDistance();
							x1 = (float) (Math.cos(Math.PI * direction + radian) * distance);
							y1 = (float) (Math.sin(Math.PI * direction + radian) * distance);
						}
						SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(npc.getWorldId(), condTemp.getNpcId(), npc.getX() + x1, npc.getY() + y1,
							npc.getZ(), npc.getHeading());
						if (template != null) {
							template.setCreatorId(npc.getObjectId());
							SpawnEngine.spawnObject(template, npc.getInstanceId());
						} else {
							break;
						}
					}
				}
				break;
		}
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
		if (owner != null && (System.currentTimeMillis() - owner.getGameStats().getLastSkillTime()) > template.getMaxChainTime())
			return false;
		return true;
	}

	@Override
	public boolean isQueued() {
		return false;
	}

	@Override
	public boolean ignoreNextSkillTime() {
		return false;
	}
}

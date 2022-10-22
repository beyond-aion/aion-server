package com.aionemu.gameserver.ai.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AILogger;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.NpcSkillList;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTemplate;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.skillengine.properties.Properties;
import com.aionemu.gameserver.skillengine.properties.TargetRangeAttribute;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer, Yeats
 */
public class SkillAttackManager {

	/**
	 * @param npcAI
	 * @param delay
	 */
	public static void performAttack(NpcAI npcAI, int delay) {
		if (npcAI.getOwner().getObjectTemplate().getAttackRange() == 0) {
			if (npcAI.getOwner().getTarget() != null
				&& !PositionUtil.isInRange(npcAI.getOwner(), npcAI.getOwner().getTarget(), npcAI.getOwner().getAggroRange())) {
				npcAI.getOwner().getController().abortCast();
				npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
				return;
			}
		}
		if (npcAI.setSubStateIfNot(AISubState.CAST)) {
			if (delay > 0) {
				ThreadPoolManager.getInstance().schedule(() -> skillAction(npcAI), delay);
			} else {
				skillAction(npcAI);
			}
		}
	}

	protected static void skillAction(NpcAI npcAI) {
		if (npcAI.getSubState() != AISubState.CAST)
			return;
		Npc owner = npcAI.getOwner();
		VisibleObject target = owner.getTarget();
		NpcSkillEntry skill = owner.getGameStats().getLastSkill();
		if (!(target instanceof Creature) || ((Creature) target).isDead() || skill == null) {
			npcAI.setSubStateIfNot(AISubState.NONE);
			npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
			return;
		}
		if (owner.getObjectTemplate().getAttackRange() == 0 && !PositionUtil.isInRange(owner, target, owner.getAggroRange())) {
			owner.getController().abortCast();
			npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
			return;
		}
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId());
		if (npcAI.isLogging()) {
			AILogger.info(npcAI, "Using skill " + skill.getSkillId() + " level: " + skill.getSkillLevel() + " duration: " + template.getDuration());
		}
		if ((template.getType() == SkillType.MAGICAL && owner.getEffectController().isAbnormalSet(AbnormalState.SILENCE))
			|| (template.getType() == SkillType.PHYSICAL && owner.getEffectController().isAbnormalSet(AbnormalState.BIND))
			|| (owner.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
			|| (owner.getTransformModel().isActive() && owner.getTransformModel().getBanUseSkills() == 1)) {
			afterUseSkill(npcAI);
		} else {
			if (template.getProperties().getFirstTarget() == FirstTargetAttribute.ME) {
				owner.setTarget(owner);
			} else {
				NpcSkillEntry lastSkill = owner.getGameStats().getLastSkill();
				if (lastSkill != null) {
					NpcSkillTemplate temp = lastSkill.getTemplate();
					if (temp != null) {
						switch (temp.getTarget()) {
							case FRIEND:
								owner.getKnownList().getKnownObjects().values().stream()
									.filter(vo -> vo instanceof Npc npc && !npc.isDead() && !npc.getLifeStats().isAboutToDie() && !owner.isEnemy(npc)
										&& owner.canSee(npc) && PositionUtil.isInRange(owner, npc, template.getProperties().getFirstTargetRange(), false)
										&& GeoService.getInstance().canSee(owner, npc))
									.findAny().ifPresent(owner::setTarget);
								break;
							case ME:
								if (!target.equals(owner)) {
									owner.setTarget(owner);
								}
								break;
							case MOST_HATED:
								Creature target2 = owner.getAggroList().getMostHated();
								if (target2 != null && !target2.isDead()) {
									if (!target.equals(target2)) {
										owner.setTarget(target2);
									}
								}
								break;
							case RANDOM:
							case RANDOM_EXCEPT_MOST_HATED:
								List<Creature> knownCreatures = new ArrayList<>();
								for (VisibleObject obj : owner.getKnownList().getKnownObjects().values()) {
									if (obj instanceof Creature target3 && !(obj instanceof Summon) && !(obj instanceof SummonedObject)) {
										if (target3.isDead() || target3.getLifeStats().isAboutToDie())
											continue;
										if (temp.getTarget() == NpcSkillTargetAttribute.RANDOM_EXCEPT_MOST_HATED && owner.getAggroList().getMostHated().equals(target3))
											continue;
										if (owner.isEnemy(target3) && owner.canSee(target3)
											&& PositionUtil.isInRange(owner, target3, template.getProperties().getFirstTargetRange(), false)
											&& GeoService.getInstance().canSee(owner, target3)) {
											knownCreatures.add(target3);
										}
									}
								}
								if (!knownCreatures.isEmpty()) {
									Creature target3 = Rnd.get(knownCreatures);
									if (target3 != null) {
										owner.setTarget(target3);
									}
								}
								break;
						}
					}
				}
			}
			boolean success = owner.getController().useSkill(skill.getSkillId(), skill.getSkillLevel());
			if (!success) {
				afterUseSkill(npcAI);
			}
		}
	}

	/**
	 * @param npcAI
	 */
	public static void afterUseSkill(NpcAI npcAI) {
		npcAI.setSubStateIfNot(AISubState.NONE);
		npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
	}

	/**
	 * @param npcAI
	 * @return
	 */
	public static NpcSkillEntry chooseNextSkill(NpcAI npcAI) {
		if (npcAI.isInSubState(AISubState.CAST)) {
			return null;
		}

		Npc owner = npcAI.getOwner();

		NpcSkillEntry queuedSkill = owner.getQueuedSkills().peek();
		if (queuedSkill != null && queuedSkill.isQueued() && queuedSkill.getNextSkillTime() == 0 && isReady(owner, queuedSkill)) {
			return getNpcSkillEntryIfNotTooFarAway(owner, queuedSkill);
		}

		if (((System.currentTimeMillis() - owner.getGameStats().getFightStartingTime()) > owner.getGameStats().getInitialSkillDelay())
			&& owner.getGameStats().canUseNextSkill()) {
			if (queuedSkill != null && isReady(owner, queuedSkill)) {
				return getNpcSkillEntryIfNotTooFarAway(owner, queuedSkill);
			}

			NpcSkillList skillList = owner.getSkillList();
			if (skillList.isEmpty()) {
				return null;
			}

			NpcSkillEntry lastSkill = owner.getGameStats().getLastSkill();
			if (lastSkill != null && lastSkill.hasChain() && lastSkill.canUseNextChain(owner)) {
				List<NpcSkillEntry> chainSkills = skillList.getChainSkills(lastSkill);
				if (chainSkills.size() > 1)
					Collections.shuffle(chainSkills);
				for (NpcSkillEntry entry : chainSkills) {
					if (entry != null && isReady(owner, entry)) {
						return getNpcSkillEntryIfNotTooFarAway(owner, entry);
					}
				}
			}

			int[] priorities = skillList.getPriorities();
			if (priorities != null) {
				for (int index = priorities.length - 1; index >= 0; index--) {
					List<NpcSkillEntry> skillsByPriority = skillList.getSkillsByPriority(priorities[index]);
					if (skillsByPriority.size() > 1)
						Collections.shuffle(skillsByPriority);

					for (NpcSkillEntry entry : skillsByPriority) {
						if (entry.getChainId() == 0 && isReady(owner, entry)) {
							return getNpcSkillEntryIfNotTooFarAway(owner, entry);
						}
					}
				}
			}
		}
		return null;
	}

	private static NpcSkillEntry getNpcSkillEntryIfNotTooFarAway(Npc owner, NpcSkillEntry entry) {
		if (targetTooFar(owner, entry)) {
			owner.getGameStats().setNextSkillDelay(5000);
			return null;
		}
		return entry;
	}

	// check for bind/silence/fear/stun etc debuffs on npc
	private static boolean isReady(Npc owner, NpcSkillEntry entry) {
		if (entry.isReady(owner.getLifeStats().getHpPercentage(), System.currentTimeMillis() - owner.getGameStats().getFightStartingTime())) {
			if (entry.conditionReady(owner)) {
				SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(entry.getSkillId());
				if ((template.getType() == SkillType.MAGICAL && owner.getEffectController().isAbnormalSet(AbnormalState.SILENCE))
					|| (template.getType() == SkillType.PHYSICAL && owner.getEffectController().isAbnormalSet(AbnormalState.BIND))
					|| (owner.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
					|| (owner.getTransformModel().isActive() && owner.getTransformModel().getBanUseSkills() == 1)) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean targetTooFar(Npc owner, NpcSkillEntry entry) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(entry.getSkillId());
		Properties prop = template.getProperties();
		if (prop.getFirstTarget() != FirstTargetAttribute.ME && entry.getTemplate().getTarget() != NpcSkillTargetAttribute.NONE
			&& entry.getTemplate().getTarget() != NpcSkillTargetAttribute.MOST_HATED && entry.getTemplate().getTarget() != NpcSkillTargetAttribute.ME) {
			if (owner.getTarget()instanceof Creature target) {
				if (target.isDead() || !owner.canSee(target)) {
					return true;
				}
				if (prop.getTargetType() != TargetRangeAttribute.AREA) {
					if (!PositionUtil.isInRange(owner, target, prop.getFirstTargetRange(), false)) {
						return true;
					}
				}
			} else {
				return true;
			}
		}
		return false;
	}

}

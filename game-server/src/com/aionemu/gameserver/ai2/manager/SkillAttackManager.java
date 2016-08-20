package com.aionemu.gameserver.ai2.manager;

import java.util.Collections;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Logger;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.SummonedObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 * @reworked Yeats
 */
public class SkillAttackManager {

	/**
	 * @param npcAI
	 * @param delay
	 */
	public static void performAttack(NpcAI2 npcAI, int delay) {
		if (npcAI.getOwner().getObjectTemplate().getAttackRange() == 0) {
			if (npcAI.getOwner().getTarget() != null
				&& !MathUtil.isInRange(npcAI.getOwner(), npcAI.getOwner().getTarget(), npcAI.getOwner().getAggroRange())) {
				npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
				npcAI.getOwner().getController().abortCast();
				return;
			}
		}
		if (npcAI.setSubStateIfNot(AISubState.CAST)) {
			if (delay > 0) {
				ThreadPoolManager.getInstance().schedule(new SkillAction(npcAI), delay);
			} else {
				skillAction(npcAI);
			}
		}
	}

	/**
	 * @param npcAI
	 */
	protected static void skillAction(NpcAI2 npcAI) {
		Npc owner = npcAI.getOwner();
		if (owner.getObjectTemplate().getAttackRange() == 0) {
			if (owner.getTarget() != null && !MathUtil.isInRange(owner, owner.getTarget(), owner.getAggroRange())) {
				npcAI.onGeneralEvent(AIEventType.TARGET_TOOFAR);
				owner.getController().abortCast();
				return;
			}
		}
		Creature target;
		if (owner.getTarget() instanceof Creature && !(target = (Creature) owner.getTarget()).getLifeStats().isAlreadyDead()) {
			final int skillId = npcAI.getSkillId();
			final int skillLevel = npcAI.getSkillLevel();

			SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
			if (npcAI.isLogging()) {
				AI2Logger.info(npcAI, "Using skill " + skillId + " level: " + skillLevel + " duration: " + template.getDuration());
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
								case ME:
									if (!target.equals(owner)) {
										owner.setTarget(owner);
									}
									break;
								case MOST_HATED:
									Creature target2 = owner.getAggroList().getMostHated();
									if (target2 != null && !target2.getLifeStats().isAlreadyDead()) {
										if (!target.equals(target2)) {
											owner.setTarget(target2);
										}
									}
									break;
								case RANDOM:
								case RANDOM_EXCEPT_MOST_HATED:
									List<Creature> knownCreatures = new FastTable<>();
									for (VisibleObject obj : owner.getKnownList().getKnownObjects().values()) {
										if (obj instanceof Creature && !(obj instanceof Summon) && !(obj instanceof SummonedObject)) {
											Creature target3 = (Creature) obj;
											if (target3.getLifeStats().isAlreadyDead() || target3.getLifeStats().isAboutToDie())
												continue;
											if (temp.getTarget() == NpcSkillTargetAttribute.RANDOM_EXCEPT_MOST_HATED && owner.getAggroList().getMostHated().equals(target3))
												continue;
											if (owner.isEnemy(target3) && owner.canSee(target3)
												&& MathUtil.isIn3dRange(owner, target3, template.getProperties().getFirstTargetRange())
												&& GeoService.getInstance().canSee(owner, target3)) {
												knownCreatures.add(target3);
											}
										}
									}
									if (!knownCreatures.isEmpty()) {
										Creature target3 = knownCreatures.get(Rnd.get(0, knownCreatures.size() - 1));
										if (target3 != null) {
											owner.setTarget(target3);
										}
									}
									break;
							}
						}
					}
				}
				boolean success = owner.getController().useSkill(skillId, skillLevel);
				if (!success) {
					afterUseSkill(npcAI);
				}
			}
		} else {
			npcAI.setSubStateIfNot(AISubState.NONE);
			npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
		}

	}

	/**
	 * @param npcAI
	 */
	public static void afterUseSkill(NpcAI2 npcAI) {
		npcAI.setSubStateIfNot(AISubState.NONE);
		npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
	}

	/**
	 * @param npcAI
	 * @return
	 */
	public static NpcSkillEntry chooseNextSkill(NpcAI2 npcAI) {
		if (npcAI.isInSubState(AISubState.CAST)) {
			return null;
		}

		Npc owner = npcAI.getOwner();

		NpcSkillEntry queuedSkill = owner.getQueuedSkills().peek();
		if (queuedSkill != null && queuedSkill.ignoreNextSkillTime() && isReady(owner, queuedSkill)) {
			return getNpcSkillEntryIfNotTooFarAway(owner, queuedSkill);
		}

		if (((System.currentTimeMillis() - owner.getGameStats().getFightStartingTime()) > owner.getGameStats().getAttackSpeed().getCurrent())
			&& owner.getGameStats().canUseNextSkill()) {
			if (queuedSkill != null && isReady(owner, queuedSkill)) {
				return getNpcSkillEntryIfNotTooFarAway(owner, queuedSkill);
			}

			NpcSkillList skillList = owner.getSkillList();
			if (skillList == null || skillList.size() == 0) {
				return null;
			}

			NpcSkillEntry lastSkill = owner.getGameStats().getLastSkill();
			if (lastSkill != null && lastSkill.hasChain() && lastSkill.canUseNextChain(owner)) {
				List<NpcSkillEntry> chainSkills = skillList.getChainSkills(lastSkill);
				if (chainSkills != null && !chainSkills.isEmpty()) {
					if (chainSkills.size() > 1)
						Collections.shuffle(chainSkills);
					for (NpcSkillEntry entry : chainSkills) {
						if (entry != null && isReady(owner, entry)) {
							return getNpcSkillEntryIfNotTooFarAway(owner, entry);
						}
					}
				}
			}

			List<Integer> priorities = skillList.getPriorities();
			if (priorities != null && !priorities.isEmpty()) {

				for (int index = priorities.size() - 1; index >= 0; index--) {
					List<NpcSkillEntry> skillsByPriority = skillList.getSkillsByPriority(priorities.get(index));
					if (skillsByPriority != null && !skillsByPriority.isEmpty()) {
						if (skillsByPriority.size() > 2)
							Collections.shuffle(skillsByPriority);

						for (NpcSkillEntry entry : skillsByPriority) {
							if (entry != null && entry.getChainId() == 0 && isReady(owner, entry)) {
								return getNpcSkillEntryIfNotTooFarAway(owner, entry);
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static NpcSkillEntry getNpcSkillEntryIfNotTooFarAway(Npc owner, NpcSkillEntry entry) {
		if (targetTooFar(owner, entry)) {
			owner.getGameStats().setNextSkillTime(5000);
			return null;
		}
		owner.getGameStats().setLastSkill(entry);
		return entry;
	}

	// check for bind/silence/fear/stun etc debuffs on npc
	private static boolean isReady(Npc owner, NpcSkillEntry entry) {
		if (entry.isReady(owner.getLifeStats().getHpPercentage(), System.currentTimeMillis() - owner.getGameStats().getFightStartingTime())) {
			if (entry.conditionReady(owner)) {
				SkillTemplate template = entry.getSkillTemplate();
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
		SkillTemplate template = entry.getSkillTemplate();
		Properties prop = template.getProperties();
		if (prop.getFirstTarget() != FirstTargetAttribute.ME && entry.getTemplate().getTarget() != NpcSkillTargetAttribute.NONE
			&& entry.getTemplate().getTarget() != NpcSkillTargetAttribute.MOST_HATED) {
			if (owner.getTarget() instanceof Creature) {
				Creature target = (Creature) owner.getTarget();
				if (target.getLifeStats().isAlreadyDead() || !owner.canSee(target)) {
					return true;
				}
				if (prop.getTargetType() != TargetRangeAttribute.AREA) {
					float collision = owner.getCollision() < 1f ? 1f : owner.getCollision();
					float targetCollision = target.getCollision() < 1f ? 1f : target.getCollision();
					if (!MathUtil.isIn3dRange(owner, target, prop.getFirstTargetRange() + collision + targetCollision)) {
						return true;
					}
				}
			} else {
				return true;
			}
		}
		return false;
	}

	private final static class SkillAction implements Runnable {

		private NpcAI2 npcAI;

		SkillAction(NpcAI2 npcAI) {
			this.npcAI = npcAI;
		}

		@Override
		public void run() {
			skillAction(npcAI);
			npcAI = null;
		}
	}
}

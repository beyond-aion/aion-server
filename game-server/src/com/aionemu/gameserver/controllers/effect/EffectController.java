package com.aionemu.gameserver.controllers.effect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_EFFECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.*;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 * @modified Wakizashi, Sippolo, Cheatkiller, Neon
 */
public class EffectController {

	private final Creature owner;
	private final Map<String, Effect> passiveEffectMap = new LinkedHashMap<>();
	private final Map<String, Effect> noshowEffects = new LinkedHashMap<>();
	private final Map<String, Effect> abnormalEffectMap = new LinkedHashMap<>();
	private long abnormalMapUpdate;

	protected final ReadWriteLock lock = new ReentrantReadWriteLock();

	protected int abnormals;

	private boolean isUnderShield = false;
	private boolean keepBuffsOnDie;

	public EffectController(Creature owner) {
		this.owner = owner;
	}

	/**
	 * @return the owner
	 */
	public Creature getOwner() {
		return owner;
	}

	/**
	 * @return the isUnderShield
	 */
	public boolean isUnderShield() {
		return isUnderShield;
	}

	/**
	 * @param isUnderShield
	 *          the isUnderShield to set
	 */
	public void setUnderShield(boolean isUnderShield) {
		this.isUnderShield = isUnderShield;
	}

	/**
	 * @param nextEffect
	 */
	public void addEffect(Effect nextEffect) {
		Map<String, Effect> mapToUpdate = getMapForEffect(nextEffect);

		boolean useEffectId = true;
		if (nextEffect.isPassive()) {
			Effect existingEffect;
			lock.readLock().lock();
			try {
				existingEffect = mapToUpdate.get(nextEffect.getStack());
			} finally {
				lock.readLock().unlock();
			}
			if (existingEffect != null && existingEffect.isPassive()) {
				// check stack level
				if (existingEffect.getSkillStackLvl() > nextEffect.getSkillStackLvl())
					return;

				// check skill level (when stack level same)
				if (existingEffect.getSkillStackLvl() == nextEffect.getSkillStackLvl() && existingEffect.getSkillLevel() > nextEffect.getSkillLevel())
					return;

				existingEffect.endEffect();
				useEffectId = false;
			}
		}

		if (useEffectId) {
			// idea here is that effects with same effectId shouldn't stack, effect with higher basic lvl takes priority
			if (searchConflict(mapToUpdate, nextEffect))
				return;
		}
		endConflictedEffect(mapToUpdate, nextEffect);
		checkEffectCooldownId(nextEffect);

		// max 3 aura effects or 1 toggle skill in noshoweffects
		if (nextEffect.isToggle() && nextEffect.getTargetSlot() == SkillTargetSlot.NOSHOW) {
			int mts = nextEffect.getSkillSubType() == SkillSubType.CHANT ? 3 : 1;
			// Rangers & Riders are allowed to use 2 Toggle skills. There might be a pattern.
			if (nextEffect.getEffector() instanceof Player && (((Player) nextEffect.getEffector()).getPlayerClass() == PlayerClass.RANGER
				|| ((Player) nextEffect.getEffector()).getPlayerClass() == PlayerClass.RIDER)) {
				mts = 2;
			}
			List<Effect> filteredMap = nextEffect.getSkillSubType() == SkillSubType.CHANT ? getAuraEffects() : getNoShowToggleEffectsExceptAuras();
			if (filteredMap.size() >= mts)
				filteredMap.get(0).endEffect();
		}

		// max 4 chants
		if (nextEffect.isChant()) {
			List<Effect> chants = filterEffects(abnormalEffectMap, Effect::isChant);
			if (chants.size() >= 4)
				chants.get(0).endEffect();
		}
		lock.writeLock().lock();
		try {
			if (nextEffect.getSkill() != null && System.currentTimeMillis() - abnormalMapUpdate < nextEffect.getSkill().getHitTime()) {
				addBeforeLastElement(mapToUpdate, nextEffect);
			} else {
				abnormalMapUpdate = System.currentTimeMillis();
				mapToUpdate.put(nextEffect.getStack(), nextEffect);
			}
		} finally {
			lock.writeLock().unlock();
		}

		nextEffect.startEffect();

		if (!nextEffect.isPassive())
			broadCastEffects(nextEffect);
	}

	private void endConflictedEffect(Map<String, Effect> effectMap, Effect newEffect) {
		int conflictId = newEffect.getSkillTemplate().getConflictId();
		if (conflictId == 0)
			return;
		Effect effectToEnd = findFirstEffect(effectMap, effect -> effect.getSkillTemplate().getConflictId() == conflictId);
		if (effectToEnd != null)
			effectToEnd.endEffect();
	}

	private boolean searchConflict(Map<String, Effect> mapToUpdate, Effect nextEffect) {
		if (checkExtraEffect(mapToUpdate, nextEffect))
			return false;
		Effect effectToEnd = null;
		lock.readLock().lock();
		try {
			mainLoop:
			for (Effect effect : mapToUpdate.values()) {
				if (effect.getSkillSubType() == nextEffect.getSkillSubType() || effect.getTargetSlot() == nextEffect.getTargetSlot()) {
					for (EffectTemplate et : effect.getEffectTemplates()) {
						if (et.getEffectId() == 0)
							continue;
						for (EffectTemplate et2 : nextEffect.getEffectTemplates()) {
							if (et2.getEffectId() == 0)
								continue;
							if ((et.getEffectId() == et2.getEffectId()) || (et instanceof SilenceEffect && et2 instanceof SilenceEffect)) {
								if (et.getBasicLvl() > et2.getBasicLvl()) {
									if (!nextEffect.isPassive() && nextEffect.getTargetSlot() != SkillTargetSlot.DEBUFF)
										nextEffect.setEffectResult(EffectResult.CONFLICT);
									return true;
								} else {
									effectToEnd = effect;
									break mainLoop;
								}
							}
						}
					}
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		if (effectToEnd != null)
			effectToEnd.endEffect(false);
		return false;
	}

	/**
	 * Checks whether {@code newEffectTemplate} is in conflict
	 * with any existing effects without removing any effects. <br>
	 * {@code newEffect}'s EffectResult is set to {@code EffectResult.CONFLICT} if a conflict is found. <br>
	 * Note: EffectResult is not changed in case of passive effects or effects with {@code SkillTargetSlot.DEBUFF}.
	 * 
	 * @param newEffect
	 *          The effect {@code newEffectTemplate} belongs to.
	 * @param newEffectTemplate
	 *          The {@code EffectTemplate} to check for conflicts.
	 * @return True if {@code newEffectTemplate} is in conflict with another existing effect.
	 */
	public boolean isConflicting(Effect newEffect, EffectTemplate newEffectTemplate) {
		if (newEffectTemplate.getEffectId() == 0)
			return false;
		Map<String, Effect> mapToUpdate = getMapForEffect(newEffect);
		lock.readLock().lock();
		try {
			mainLoop:
			for (Effect currentEffect : mapToUpdate.values()) {
				if (currentEffect.getSkillSubType() == newEffect.getSkillSubType() || currentEffect.getTargetSlot() == newEffect.getTargetSlot()) {
					for (EffectTemplate currentEffectTemplate : currentEffect.getEffectTemplates()) {
						if (currentEffectTemplate.getEffectId() == 0)
							continue;
						if ((currentEffectTemplate.getEffectId() == newEffectTemplate.getEffectId())
							|| (currentEffectTemplate instanceof SilenceEffect && newEffectTemplate instanceof SilenceEffect)) {
							if (currentEffectTemplate.getBasicLvl() > newEffectTemplate.getBasicLvl() && !(currentEffectTemplate instanceof HideEffect)) {
								return true;
							} else {
								break mainLoop;
							}
						}
					}
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
	}

	private boolean checkExtraEffect(Map<String, Effect> effectMap, Effect nextEffect) {
		if (nextEffect.isPassive() || nextEffect.getDispelCategory() != DispelCategoryType.EXTRA)
			return false;
		Effect extraEffect = findFirstEffect(effectMap, effect -> effect.getDispelCategory() == DispelCategoryType.EXTRA
			&& !effect.getSkillTemplate().getStack().startsWith("IDSEAL_BOSS_VRITRA_BUFF"));
		if (extraEffect != null) {
			extraEffect.endEffect();
			return true;
		}
		return false;
	}

	private List<Effect> getAuraEffects() {
		return filterEffects(noshowEffects, effect -> effect.getSkillSubType() == SkillSubType.CHANT);
	}

	private List<Effect> getNoShowToggleEffectsExceptAuras() {
		return filterEffects(noshowEffects, effect -> effect.getSkillSubType() != SkillSubType.CHANT);
	}

	private void checkEffectCooldownId(Effect effect) {
		if (effect.isPassive() || effect.getTargetSlot() == SkillTargetSlot.NOSHOW)
			return;
		int cdId = effect.getSkillTemplate().getCooldownId();
		if (cdId == 1)
			return;
		int size = 1;
		switch (cdId) {
			case 273: // Erosion & Flamecage
			case 353: // Lockdown & Dazing Severe Blow
				size = 2;
				break;
			case 6:
				size = 10;
				break;
		}
		List<Effect> effects = filterEffects(abnormalEffectMap,
			e -> e.getTargetSlot() == effect.getTargetSlot() && e.getSkillTemplate().getCooldownId() == cdId);
		if (effects.size() >= size)
			effects.get(0).endEffect();
		// archer buffs
		if (cdId >= 2020 && cdId <= 2030) {
			int count = 0;
			Effect toRemove = null;
			for (Effect eff : getAbnormalEffectsToShow()) {
				switch (eff.getSkillTemplate().getCooldownId()) {
					case 2020: // Dodging
					case 2022: // Focused Shots
					case 2024: // Aiming
					case 2026: // Bestial Fury
					case 2028: // Hunter's Eye
					case 2030: // Strong Shots
						if (toRemove == null)
							toRemove = eff;
						if (++count >= 2) {
							toRemove.endEffect();
							return;
						}
						break;
				}
			}
		}
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private void addBeforeLastElement(Map<String, Effect> mapToUpdate, Effect newEffect) {
		Effect lastEffect = null;
		for (Effect effect : mapToUpdate.values()) {
			lastEffect = effect;
		}

		if (lastEffect != null && !(lastEffect.getEffector() instanceof Player))
			mapToUpdate.remove(lastEffect.getStack());
		mapToUpdate.put(newEffect.getStack(), newEffect);
		if (lastEffect != null && !(lastEffect.getEffector() instanceof Player))
			mapToUpdate.put(lastEffect.getStack(), lastEffect);
	}

	protected Map<String, Effect> getMapForEffect(Effect effect) {
		return getMapForEffect(effect.getSkillTemplate());
	}

	private Map<String, Effect> getMapForEffect(SkillTemplate template) {
		if (template.isPassive())
			return passiveEffectMap;

		if (template.getActivationAttribute() == ActivationAttribute.TOGGLE && template.getTargetSlot() == SkillTargetSlot.NOSHOW)
			return noshowEffects;

		return abnormalEffectMap;
	}

	public Effect getAbnormalEffect(String stack) {
		lock.readLock().lock();
		try {
			return abnormalEffectMap.get(stack);
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean hasAbnormalEffect(Predicate<Effect> predicate) {
		return findFirstEffect(abnormalEffectMap, predicate) != null;
	}

	public boolean hasAbnormalEffect(int skillId) {
		return hasAbnormalEffect(effect -> effect.getSkillId() == skillId);
	}

	public void broadCastEffects(Effect effect) {
		int slot = effect != null ? effect.getTargetSlot().getId() : SkillTargetSlot.FULLSLOTS;
		List<Effect> effects = getAbnormalEffects();
		PacketSendUtility.broadcastPacket(getOwner(), new SM_ABNORMAL_EFFECT(getOwner(), abnormals, effects, slot));
	}

	public void clearEffect(Effect effect) {
		clearEffect(effect, true);
	}

	public void clearEffect(Effect effect, boolean broadCastEffects) {
		Map<String, Effect> effectMap = getMapForEffect(effect);
		lock.writeLock().lock();
		try {
			Effect oldEffect = effectMap.get(effect.getStack());
			if (oldEffect != null) {
				if (!oldEffect.equals(effect))
					return; // effect in map was already replaced by a newer one (e.g. when toggling many auras), so there's no need to re-broadcast
				effectMap.remove(effect.getStack());
			}
		} finally {
			lock.writeLock().unlock();
		}
		if (broadCastEffects)
			broadCastEffects(effect);
	}

	public Effect findBySkillId(int skillId) {
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		if (skillTemplate == null)
			throw new NullPointerException("Skill with ID " + skillId + " does not exist");
		return findFirstEffect(getMapForEffect(skillTemplate), effect -> effect.getSkillId() == skillId);
	}

	/**
	 * Removes the effect by skillId.
	 * 
	 * @param skillId
	 */
	public void removeEffect(int skillId) {
		Effect effect = findBySkillId(skillId);
		if (effect != null)
			effect.endEffect();
	}

	public void removeHideEffects() {
		removeEffects(abnormalEffectMap, Effect::isHideEffect);
	}

	/**
	 * Removes Petorderunsummon effects from owner.
	 */
	public void removePetOrderUnSummonEffects() {
		removeEffects(abnormalEffectMap, Effect::isPetOrderUnSummonEffect);
	}

	/**
	 * Removes Paralyze effects from owner.
	 */
	public void removeParalyzeEffects() {
		removeEffects(abnormalEffectMap, Effect::isParalyzeEffect);
	}

	/**
	 * Removes Stun effects from owner.
	 */
	public void removeStunEffects() {
		removeEffects(abnormalEffectMap, Effect::isStunEffect);
	}

	/**
	 * Removes Transform effects from owner. For more info see {@link TransformEffect} it doesn't remove Avatar transforms (TODO find out on retail)
	 */
	public void removeTransformEffects() {
		removeEffects(abnormalEffectMap, effect -> {
			for (EffectTemplate et : effect.getEffectTemplates()) {
				if (et instanceof TransformEffect && ((TransformEffect) et).getTransformType() != TransformType.AVATAR)
					return true;
			}
			return false;
		});
	}

	private void removeEffects(Map<String, Effect> mapForEffect, Predicate<Effect> predicate) {
		for (Effect effect : filterEffects(mapForEffect, predicate))
			effect.endEffect();
	}

	private Effect findFirstEffect(Map<String, Effect> effectMap, Predicate<Effect> filter) {
		lock.readLock().lock();
		try {
			for (Effect effect : effectMap.values()) {
				if (filter.test(effect))
					return effect;
			}
		} finally {
			lock.readLock().unlock();
		}
		return null;
	}

	public List<Effect> getAllEffects() {
		List<Effect> effects = new ArrayList<>();
		lock.readLock().lock();
		try {
			effects.addAll(abnormalEffectMap.values());
			effects.addAll(noshowEffects.values());
			effects.addAll(passiveEffectMap.values());
		} finally {
			lock.readLock().unlock();
		}
		return effects;
	}

	private List<Effect> filterEffects(Map<String, Effect> effectMap, Predicate<Effect> filter) {
		List<Effect> effects = new ArrayList<>();
		lock.readLock().lock();
		try {
			for (Effect effect : effectMap.values()) {
				if (filter.test(effect))
					effects.add(effect);
			}
		} finally {
			lock.readLock().unlock();
		}
		return effects;
	}

	/**
	 * Removes all effects by given dispel slot type
	 */
	public void removeByDispelSlotType(DispelSlotType dispelSlotType) {
		removeByDispelEffect(null, dispelSlotType, 255, 100, 100);
	}

	public boolean removeByEffectId(int effectId, int dispelLevel, int power) {
		if (removeByEffectId(abnormalEffectMap, effectId, dispelLevel, power))
			return true;
		return removeByEffectId(noshowEffects, effectId, dispelLevel, power);
	}

	private boolean removeByEffectId(Map<String, Effect> effectMap, int effectId, int dispelLevel, int power) {
		Effect effectToEnd = findFirstEffect(effectMap,
			effect -> effect.getReqDispelLevel() <= dispelLevel && effect.containsEffectId(effectId) && removePower(effect, power));
		if (effectToEnd != null) {
			effectToEnd.endEffect();
			return true;
		} else {
			return false;
		}
	}

	public void removeByDispelEffect(EffectType effectType, DispelSlotType dispelSlotType, int count, int dispelLevel, int power) {
		count = removeByDispelEffect(abnormalEffectMap, effectType, dispelSlotType, count, dispelLevel, power);
		if (count > 0)
			removeByDispelEffect(noshowEffects, effectType, dispelSlotType, count, dispelLevel, power);
	}

	private int removeByDispelEffect(Map<String, Effect> effectMap, EffectType effectType, DispelSlotType dispelSlotType, int count, int dispelLevel,
		int power) {
		List<Effect> effectsToEnd = new ArrayList<>();
		lock.readLock().lock();
		try {
			for (Effect effect : effectMap.values()) {
				// check count
				if (count == 0)
					break;

				if (effectType != null) {
					if (!effect.getSkillTemplate().hasAnyEffect(effectType))
						continue;
					if (effectType.equals(EffectType.STUN) && effect.getSkillId() == 11904) { // avatar skill irremovable by Remove Shock TODO: logic
						continue;
					}
				}
				if (dispelSlotType != null) {
					if (effect.getTargetSlot() != SkillTargetSlot.of(dispelSlotType))
						continue;
				}
				// check dispel level
				if (effect.getReqDispelLevel() > dispelLevel)
					continue;

				if (removePower(effect, power))
					effectsToEnd.add(effect);
				// decrease count
				count--;
			}
		} finally {
			lock.readLock().unlock();
		}
		for (Effect effect : effectsToEnd)
			effect.endEffect();
		return count;
	}

	/**
	 * Calculates effects that will be removed and reduces their power. Used only in DispelBuffCounterAtkEffect
	 * 
	 * @return number of effects that will be removed
	 */
	public int calculateBuffsOrEffectorDebuffsToRemove(Effect effect, int count, int dispelLevel, int power) {
		int dispelledEffectCount = 0;
		lock.readLock().lock();
		try {
			for (Effect ef : abnormalEffectMap.values()) {
				if (count == 0)
					break;

				DispelCategoryType dispelCat = ef.getDispelCategory();
				SkillTargetSlot targetSlot = ef.getSkillTemplate().getTargetSlot();

				// skip effects that are about to be removed by another dispel effect
				if (ef.getDesignatedDispelEffect() != null)
					continue;

				// effects with duration 86400000 cant be dispelled
				// TODO recheck
				if (ef.getDuration() >= 86400000 && !isRemovableEffect(ef))
					continue;

				if (ef.isSanctuaryEffect())
					continue;

				// check for targetslot, effects with target slot higher or equal to 2 cant be removed (ex. skillId: 11885)
				if (targetSlot != SkillTargetSlot.BUFF && (targetSlot != SkillTargetSlot.DEBUFF && dispelCat != DispelCategoryType.ALL)
					|| ef.getTargetSlotLevel() >= 2)
					continue;

				// remove only debuffs of the effector
				if (targetSlot == SkillTargetSlot.DEBUFF && !effect.getEffector().equals(ef.getEffector()))
					continue;

				switch (dispelCat) {
					case ALL:
					case BUFF:// DispelBuffCounterAtkEffect
						if (ef.getReqDispelLevel() <= dispelLevel) {
							if (removePower(ef, power)) {
								ef.setDesignatedDispelEffect(effect);
								dispelledEffectCount++;
							}
							count--;
						}
						break;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return dispelledEffectCount;
	}

	public void removeEffectByDispelCat(DispelCategoryType dispelCat, SkillTargetSlot targetSlot, int count, int dispelLevel, int power) {
		List<Effect> effectsToEnd = new ArrayList<>();
		boolean insufficientDispelPower = false;
		boolean insufficientDispelLevel = false;
		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				if (count == 0)
					break;
				// effects with duration 86400000 cant be dispelled
				// TODO recheck
				if (effect.getDuration() >= 86400000 && !isRemovableEffect(effect)) {
					continue;
				}

				if (effect.isSanctuaryEffect())
					continue;

				// skip effects that are about to be removed by another dispel effect
				if (effect.getDesignatedDispelEffect() != null)
					continue;

				// check for targetslot, effects with target slot level higher or equal to 2 cant be removed (ex. skillId: 11885)
				if (effect.getTargetSlot() != targetSlot || effect.getTargetSlotLevel() >= 2)
					continue;

				boolean remove = false;
				switch (dispelCat) {
					case ALL:// DispelDebuffEffect
						if ((effect.getDispelCategory() == DispelCategoryType.ALL || effect.getDispelCategory() == DispelCategoryType.DEBUFF_MENTAL
							|| effect.getDispelCategory() == DispelCategoryType.DEBUFF_PHYSICAL) && effect.getReqDispelLevel() <= dispelLevel)
							remove = true;
						break;
					case DEBUFF_MENTAL:// DispelDebuffMentalEffect
						if ((effect.getDispelCategory() == DispelCategoryType.ALL || effect.getDispelCategory() == DispelCategoryType.DEBUFF_MENTAL)
							&& effect.getReqDispelLevel() <= dispelLevel)
							remove = true;
						break;
					case DEBUFF_PHYSICAL:// DispelDebuffPhysicalEffect
						if ((effect.getDispelCategory() == DispelCategoryType.ALL || effect.getDispelCategory() == DispelCategoryType.DEBUFF_PHYSICAL)
							&& effect.getReqDispelLevel() <= dispelLevel)
							remove = true;
						break;
					case BUFF:// DispelBuffEffect or DispelBuffCounterAtkEffect
						if (effect.getDispelCategory() == DispelCategoryType.BUFF && effect.getReqDispelLevel() <= dispelLevel)
							remove = true;
						break;
					case STUN:
						if (effect.getDispelCategory() == DispelCategoryType.STUN)
							remove = true;
						break;
					case NPC_BUFF:// DispelNpcBuff
						if (effect.getDispelCategory() == DispelCategoryType.NPC_BUFF)
							remove = true;
						break;
					case NPC_DEBUFF_PHYSICAL:// DispelNpcDebuff
						if (effect.getDispelCategory() == DispelCategoryType.NPC_DEBUFF_PHYSICAL)
							remove = true;
						break;
				}

				if (remove) {
					if (removePower(effect, power)) {
						effectsToEnd.add(effect);
					} else if (owner instanceof Player) {
						insufficientDispelPower = true;
					}
					count--;
				} else
					insufficientDispelLevel = true;
			}
		} finally {
			lock.readLock().unlock();
		}
		for (Effect effect : effectsToEnd) // end outside lock so broadcasting effects can't cause deadlocks
			effect.endEffect();
		if (owner instanceof Player) {
			if (insufficientDispelPower)
				PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELCOUNT());
			if (insufficientDispelLevel)
				PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELLEVEL());
		}
	}

	public void dispelBuffCounterAtkEffect(Effect effect) {
		List<Effect> effectsToEnd = new ArrayList<>();
		lock.readLock().lock();
		try {
			for (Effect ef : abnormalEffectMap.values()) {
				if (effect.equals(ef.getDesignatedDispelEffect())) {
					effectsToEnd.add(ef);
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		for (Effect ef : effectsToEnd) {
			ef.endEffect();
		}
	}

	// TODO this should be removed
	private boolean isRemovableEffect(Effect effect) {
		int skillId = effect.getSkillId();
		switch (skillId) {
			case 20941:
			case 20942:
			case 19370:
			case 19371:
			case 19372:
			case 20530:
			case 20531:
			case 19345:
			case 19346:
			case 21438:
			case 21121:
			case 21124:
				// TODO
				return true;
			default:
				return false;
		}
	}

	private boolean removePower(Effect effect, int power) {
		return effect.removePower(power) <= 0;
	}

	/**
	 * Removes all effects from controllers and ends them appropriately Passive effect will not be removed
	 */
	public void removeAllEffects() {
		removeAllEffects(false);
	}

	public void removeAllEffects(boolean logout) {
		List<Effect> effects;
		if (logout) { // remove all effects on logout
			effects = getAllEffects();
		} else {
			effects = new ArrayList<>();
			lock.readLock().lock();
			try {
				for (Effect effect : abnormalEffectMap.values()) {
					if (effect.canRemoveOnDie() && (!keepBuffsOnDie || effect.getTargetSlot() == SkillTargetSlot.DEBUFF))
						effects.add(effect);
				}
				effects.addAll(noshowEffects.values());
			} finally {
				lock.readLock().unlock();
			}
		}
		for (Effect effect : effects) // end outside lock so broadcasting effects can't cause deadlocks
			effect.endEffect(false);
		if (!logout)
			broadCastEffects(null);
	}

	/**
	 * return true if creature is under Fear effect
	 */
	public boolean isUnderFear() {
		return isAbnormalSet(AbnormalState.FEAR);
	}

	/**
	 * @return true if creature is under Confuse effect
	 */
	public boolean isConfused() {
		return isAbnormalSet(AbnormalState.CONFUSE);
	}

	/**
	 * @return copy of abnormals list
	 */
	public List<Effect> getAbnormalEffects() {
		lock.readLock().lock();
		try {
			return new ArrayList<>(abnormalEffectMap.values());
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @return list of effects to display as top icons
	 */
	public List<Effect> getAbnormalEffectsToTargetSlot(final int slot) {
		lock.readLock().lock();
		try {
			return abnormalEffectMap.values().stream().filter(e -> e.getTargetSlot().getId() == slot).collect(Collectors.toList());
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @return list of effects to display as top icons
	 */
	public List<Effect> getAbnormalEffectsToShow() {
		lock.readLock().lock();
		try {
			return abnormalEffectMap.values().stream().filter(e -> e.getTargetSlot() != SkillTargetSlot.NOSHOW).collect(Collectors.toList());
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setAbnormal(AbnormalState state) {
		owner.getObserveController().notifyAbnormalSettedObservers(state);
		abnormals |= state.getId();

		// TODO move to observer?
		// if player is sitting when setting certain abnormal states, he is forcefully made to stand up
		if (owner instanceof Player && owner.isInState(CreatureState.RESTING)) {
			if (isInAnyAbnormalState(AbnormalState.AUTOMATICALLY_STANDUP)) {
				owner.unsetState(CreatureState.RESTING);
				PacketSendUtility.broadcastPacket((Player) owner, new SM_EMOTION(owner, EmotionType.STAND), true);
			}
		}
	}

	public void unsetAbnormal(AbnormalState state) {
		int count = 0;
		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				if ((effect.getAbnormals() & state.getId()) == state.getId()) {
					if (++count == 2)
						return;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		abnormals &= ~state.getId();
	}

	/**
	 * Used for checking unique abnormal states
	 * 
	 * @param state
	 * @return
	 */
	public boolean isAbnormalSet(AbnormalState state) {
		if (state == AbnormalState.NONE)
			return abnormals == 0;
		return (abnormals & state.getId()) == state.getId();
	}

	/**
	 * Used for compound abnormal state checks
	 * 
	 * @param state
	 * @return
	 */
	public boolean isInAnyAbnormalState(AbnormalState state) {
		if (state == AbnormalState.NONE)
			return abnormals == 0;
		return (abnormals & state.getId()) != 0;
	}

	public int getAbnormals() {
		return abnormals;
	}

	public boolean isEmpty() {
		return abnormalEffectMap.isEmpty();
	}

	public void setKeepBuffsOnDie(boolean keepBuffsOnDie) {
		this.keepBuffsOnDie = keepBuffsOnDie;
	}

	public void resetDesignatedDispelEffect(Effect effect) {
		lock.readLock().lock();
		try {
			for (Effect ef : abnormalEffectMap.values()) {
				if (effect.equals(ef.getDesignatedDispelEffect())) {
					ef.resetDesignatedDispelEffect();
				}
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}

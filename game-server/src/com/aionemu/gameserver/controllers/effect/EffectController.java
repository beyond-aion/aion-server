package com.aionemu.gameserver.controllers.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.EffectType;
import com.aionemu.gameserver.skillengine.effect.TransformEffect;
import com.aionemu.gameserver.skillengine.model.ActivationAttribute;
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.DispelSlotType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectResult;
import com.aionemu.gameserver.skillengine.model.SkillSubType;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.TransformType;
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

		lock.writeLock().lock();
		try {
			if (nextEffect.isPassive()) {
				boolean useEffectId = true;
				Effect existingEffect = mapToUpdate.get(nextEffect.getStack());
				if (existingEffect != null && existingEffect.isPassive()) {
					// check stack level
					if (existingEffect.getSkillStackLvl() > nextEffect.getSkillStackLvl()) {
						return;
					}

					// check skill level (when stack level same)
					if (existingEffect.getSkillStackLvl() == nextEffect.getSkillStackLvl() && existingEffect.getSkillLevel() > nextEffect.getSkillLevel()) {
						return;
					}
					existingEffect.endEffect();
					useEffectId = false;
				}

				if (useEffectId) {
					// idea here is that effects with same effectId shouldn't stack effect with higher basic lvl takes priority
					mainLoop:
					for (Iterator<Effect> iter = mapToUpdate.values().iterator(); iter.hasNext();) {
						Effect effect = iter.next();
						if (effect.getTargetSlot() == nextEffect.getTargetSlot()) {
							for (EffectTemplate et : effect.getEffectTemplates()) {
								if (et.getEffectId() == 0)
									continue;
								for (EffectTemplate et2 : nextEffect.getEffectTemplates()) {
									if (et2.getEffectId() == 0)
										continue;
									if (et.getEffectId() == et2.getEffectId()) {
										if (et.getBasicLvl() > et2.getBasicLvl())
											return;
										else {
											iter.remove();
											effect.endEffect();
											break mainLoop;
										}
									}
								}
							}
						}
					}
				}
			}

			endConflictedEffect(mapToUpdate, nextEffect);

			if (!nextEffect.isPassive()) {
				if (searchConflict(mapToUpdate, nextEffect))
					return;

				checkEffectCooldownId(nextEffect);
			}

			// max 3 aura effects or 1 toggle skill in noshoweffects
			if (nextEffect.isToggle() && nextEffect.getTargetSlot() == SkillTargetSlot.NOSHOW) {
				int mts = nextEffect.getSkillSubType() == SkillSubType.CHANT ? 3 : 1;
				// Rangers & Riders are allowed to use 2 Toggle skills. There might be a pattern.
				if (nextEffect.getEffector() instanceof Player && (((Player) nextEffect.getEffector()).getPlayerClass() == PlayerClass.RANGER
					|| ((Player) nextEffect.getEffector()).getPlayerClass() == PlayerClass.RIDER)) {
					mts = 2;
				}
				Collection<Effect> filteredMap = nextEffect.getSkillSubType() == SkillSubType.CHANT ? getAuraEffects() : getNoShowToggleEffectsExceptAuras();
				if (filteredMap.size() >= mts) {
					Iterator<Effect> iter = filteredMap.iterator();
					iter.next().endEffect();
				}
			}

			// max 4 chants
			if (nextEffect.isChant()) {
				Collection<Effect> chants = abnormalEffectMap.values().stream().filter(e -> e.isChant()).collect(Collectors.toList());
				if (chants.size() >= 4) {
					Iterator<Effect> chantIter = chants.iterator();
					chantIter.next().endEffect();
				}
			}

			if (shouldAddBeforeLastEffect(nextEffect)) {
				addBeforeLastElement(mapToUpdate, nextEffect);
			} else {
				abnormalMapUpdate = System.currentTimeMillis();
				mapToUpdate.put(nextEffect.getStack(), nextEffect);
			}
		} finally {
			lock.writeLock().unlock();
		}

		// ? move into lock area
		nextEffect.startEffect(false);

		if (!nextEffect.isPassive()) {
			broadCastEffects(nextEffect);
		}
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private void endConflictedEffect(Map<String, Effect> mapToUpdate, Effect newEffect) {
		int conflictId = newEffect.getSkillTemplate().getConflictId();
		if (conflictId == 0)
			return;
		for (Iterator<Effect> iter = mapToUpdate.values().iterator(); iter.hasNext();) {
			Effect effect = iter.next();
			if (effect.getSkillTemplate().getConflictId() == conflictId) {
				iter.remove();
				effect.endEffect();
				return;
			}
		}
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private boolean searchConflict(Map<String, Effect> mapToUpdate, Effect nextEffect) {
		if (checkExtraEffect(mapToUpdate, nextEffect))
			return false;
		for (Iterator<Effect> iter = mapToUpdate.values().iterator(); iter.hasNext();) {
			Effect effect = iter.next();
			if (effect.getSkillSubType().equals(nextEffect.getSkillSubType()) || effect.getTargetSlot() == nextEffect.getTargetSlot()) {
				effectCheck:
				for (EffectTemplate et : effect.getEffectTemplates()) {
					if (et.getEffectId() == 0)
						continue;
					for (EffectTemplate et2 : nextEffect.getEffectTemplates()) {
						if (et2.getEffectId() == 0)
							continue;
						if (et.getEffectId() == et2.getEffectId()) {
							if (et.getBasicLvl() > et2.getBasicLvl()) {
								if (nextEffect.getTargetSlot() != SkillTargetSlot.DEBUFF)
									nextEffect.setEffectResult(EffectResult.CONFLICT);
								return true;
							} else {
								iter.remove();
								effect.endEffect(false);
								break effectCheck;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private boolean checkExtraEffect(Map<String, Effect> mapToUpdate, Effect nextEffect) {
		for (Iterator<Effect> iter = mapToUpdate.values().iterator(); iter.hasNext();) {
			Effect effect = iter.next();
			if (nextEffect.getDispelCategory() == DispelCategoryType.EXTRA) {
				if (effect.getDispelCategory() == DispelCategoryType.EXTRA) {
					if (effect.getSkillTemplate().getSkillId() != 21610 && effect.getSkillTemplate().getSkillId() != 21611
						&& effect.getSkillTemplate().getSkillId() != 21612) {
						iter.remove();
						effect.endEffect();
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private List<Effect> getAuraEffects() {
		return noshowEffects.values().stream().filter(e -> e.getSkillSubType() == SkillSubType.CHANT).collect(Collectors.toList());
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private List<Effect> getNoShowToggleEffectsExceptAuras() {
		return noshowEffects.values().stream().filter(e -> e.getSkillSubType() != SkillSubType.CHANT).collect(Collectors.toList());
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private boolean shouldAddBeforeLastEffect(Effect effect) {
		return effect.getSkill() != null && System.currentTimeMillis() - abnormalMapUpdate < effect.getSkill().getHitTime();
	}

	/**
	 * DON'T call this method anywhere else than addEffect() since it's not no synchronized
	 */
	private void addBeforeLastElement(Map<String, Effect> mapToUpdate, Effect newEffect) {
		Effect lastEffect = null;
		for (Iterator<Effect> iter = mapToUpdate.values().iterator(); iter.hasNext();) {
			lastEffect = iter.next();
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

	private Map<String, Effect> getMapForEffect(int skillId) {
		return getMapForEffect(DataManager.SKILL_DATA.getSkillTemplate(skillId));
	}

	private Map<String, Effect> getMapForEffect(SkillTemplate template) {
		if (template.isPassive())
			return passiveEffectMap;

		if (template.getActivationAttribute() == ActivationAttribute.TOGGLE && template.getTargetSlot() == SkillTargetSlot.NOSHOW)
			return noshowEffects;

		return abnormalEffectMap;
	}

	/**
	 * @param stack
	 * @return abnormalEffectMap
	 */
	public Effect getAbnormalEffect(String stack) {
		return abnormalEffectMap.get(stack);
	}

	/**
	 * @param skillId
	 * @return
	 */
	public boolean hasAbnormalEffect(int skillId) {
		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				if (effect.getSkillId() == skillId)
					return true;
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
	}

	/**
	 * Check if NoDeathPenalty is active
	 * 
	 * @return boolean
	 */
	public boolean isNoDeathPenaltyInEffect() {
		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				if (effect.isNoDeathPenalty())
					return true;
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
	}

	/**
	 * Check if NoResurrectPenalty is active
	 * 
	 * @return boolean
	 */
	public boolean isNoResurrectPenaltyInEffect() {
		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				if (effect.isNoResurrectPenalty())
					return true;
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
	}

	/**
	 * Check if HiPass is active
	 * 
	 * @return boolean
	 */
	public boolean isHiPassInEffect() {
		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				if (effect.isHiPass())
					return true;
			}
			return false;
		} finally {
			lock.readLock().unlock();
		}
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

	/**
	 * Removes the effect by skillid.
	 * 
	 * @param skillId
	 */
	public void removeEffect(int skillId) {
		Map<String, Effect> mapForEffect = getMapForEffect(skillId);
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = mapForEffect.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (effect.getSkillId() == skillId) {
					iter.remove();
					effect.endEffect();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void removeHideEffects() {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (effect.isHideEffect() && owner.getVisualState() < 10) {
					iter.remove();
					effect.endEffect();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes Petorderunsummon effects from owner.
	 */
	public void removePetOrderUnSummonEffects() {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (effect.isPetOrderUnSummonEffect()) {
					iter.remove();
					effect.endEffect();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes Paralyze effects from owner.
	 */
	public void removeParalyzeEffects() {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (effect.isParalyzeEffect()) {
					iter.remove();
					effect.endEffect();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes Stun effects from owner.
	 */
	public void removeStunEffects() {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (effect.isStunEffect()) {
					iter.remove();
					effect.endEffect();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes Transform effects from owner. for more info see TransformEffect it doesnt remove Avatar transforms (TODO find out on retail)
	 */
	public void removeTransformEffects() {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				for (EffectTemplate et : effect.getEffectTemplates()) {
					if (et instanceof TransformEffect && ((TransformEffect) et).getTransformType() != TransformType.AVATAR) {
						iter.remove();
						effect.endEffect();
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes all effects by given dispel slot type
	 */
	public void removeByDispelSlotType(DispelSlotType dispelSlotType) {
		removeByDispelEffect(null, dispelSlotType, 255, 100, 100);
	}

	public boolean removeByEffectId(int effectId, int dispelLevel, int power) {
		lock.writeLock().lock();
		try {
			if (removeByEffectId(abnormalEffectMap, effectId, dispelLevel, power))
				return true;
			return removeByEffectId(noshowEffects, effectId, dispelLevel, power);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * internal helper method, NOT synchronized
	 */
	private boolean removeByEffectId(Map<String, Effect> effectMap, int effectId, int dispelLevel, int power) {
		for (Iterator<Effect> iter = effectMap.values().iterator(); iter.hasNext();) {
			Effect effect = iter.next();
			if (!effect.containsEffectId(effectId))
				continue;
			// check dispel level
			if (effect.getReqDispelLevel() > dispelLevel)
				continue;

			if (removePower(effect, power)) {
				iter.remove();
				effect.endEffect();
				return true;
			}
		}
		return false;
	}

	public void removeByDispelEffect(EffectType effectType, DispelSlotType dispelSlotType, int count, int dispelLevel, int power) {
		lock.writeLock().lock();
		try {
			count = removeByDispelEffect(abnormalEffectMap, effectType, dispelSlotType, count, dispelLevel, power);
			if (count > 0)
				removeByDispelEffect(noshowEffects, effectType, dispelSlotType, count, dispelLevel, power);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * internal helper method, NOT synchronized
	 */
	private int removeByDispelEffect(Map<String, Effect> effectMap, EffectType effectType, DispelSlotType dispelSlotType, int count, int dispelLevel,
		int power) {
		for (Iterator<Effect> iter = effectMap.values().iterator(); iter.hasNext();) {
			// check count
			if (count == 0)
				break;

			Effect effect = iter.next();
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

			if (removePower(effect, power)) {
				iter.remove();
				effect.endEffect();
			}
			// decrease count
			count--;
		}
		return count;
	}

	/**
	 * Method used to calculate number of effects of given dispelcategory, targetslot and dispelLevel used only in DispelBuffCounterAtk, therefore rest
	 * of cases are skipped
	 * 
	 * @param dispelLevel
	 * @return
	 */
	public int calculateNumberOfEffects(int dispelLevel) {
		int number = 0;

		lock.readLock().lock();
		try {
			for (Effect effect : abnormalEffectMap.values()) {
				DispelCategoryType dispelCat = effect.getDispelCategory();
				SkillTargetSlot tragetSlot = effect.getSkillTemplate().getTargetSlot();
				// effects with duration 86400000 cant be dispelled
				// TODO recheck
				if (effect.getDuration() >= 86400000 && !isRemovableEffect(effect))
					continue;

				if (effect.isSanctuaryEffect())
					continue;

				// check for targetslot, effects with target slot higher or equal to 2 cant be removed (ex. skillId: 11885)
				if (tragetSlot != SkillTargetSlot.BUFF && (tragetSlot != SkillTargetSlot.DEBUFF && dispelCat != DispelCategoryType.ALL)
					|| effect.getTargetSlotLevel() >= 2)
					continue;

				switch (dispelCat) {
					case ALL:
					case BUFF:// DispelBuffCounterAtkEffect
						if (effect.getReqDispelLevel() <= dispelLevel)
							number++;
						break;
				}
			}
		} finally {
			lock.readLock().unlock();
		}

		return number;
	}

	public void removeEffectByDispelCat(DispelCategoryType dispelCat, SkillTargetSlot targetSlot, int count, int dispelLevel, int power,
		boolean itemTriggered) {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (count == 0)
					break;
				// effects with duration 86400000 cant be dispelled
				// TODO recheck
				if (effect.getDuration() >= 86400000 && !isRemovableEffect(effect)) {
					continue;
				}

				if (effect.isSanctuaryEffect())
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
						iter.remove();
						effect.endEffect();
						abnormalEffectMap.remove(effect.getStack());
					} else if (owner instanceof Player) {
						PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELCOUNT());
						count++;
						power += 10;
					}
					count--;
				} else if (owner instanceof Player)
					PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELLEVEL());
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private int removeEffectByTargetSlot(int count, SkillTargetSlot targetSlot, int dispelLevel, int power) {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				SkillTargetSlot ts = effect.getSkillTemplate().getTargetSlot();
				DispelCategoryType dispelCat = effect.getDispelCategory();
				if (ts == targetSlot) {
					if (count == 0)
						break;
					if (effect.getDuration() >= 86400000 && !isRemovableEffect(effect))
						continue;
					if (effect.isSanctuaryEffect())
						continue;
					if (effect.getTargetSlotLevel() >= 2)
						continue;

					boolean remove = false;
					switch (dispelCat) {
						case ALL:
						case BUFF:
							if (effect.getReqDispelLevel() <= dispelLevel)
								remove = true;
							break;
					}
					if (remove) {
						if (removePower(effect, power)) {
							iter.remove();
							effect.endEffect();
							abnormalEffectMap.remove(effect.getStack());
						} else if (owner instanceof Player)
							PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELCOUNT());
						count--;
					} else if (owner instanceof Player)
						PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELLEVEL());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		return count;
	}

	public void dispelBuffCounterAtkEffect(int count, int dispelLevel, int power) {
		count = removeEffectByTargetSlot(count, SkillTargetSlot.BUFF, dispelLevel, power);
		if (count > 0)
			removeEffectByTargetSlot(count, SkillTargetSlot.DEBUFF, dispelLevel, power);
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
	 * Removes the effect by skillid.
	 * 
	 * @param skillid
	 */
	public void removePassiveEffect(int skillid) {
		lock.writeLock().lock();
		try {
			for (Iterator<Effect> iter = passiveEffectMap.values().iterator(); iter.hasNext();) {
				Effect effect = iter.next();
				if (effect.getSkillId() == skillid) {
					iter.remove();
					effect.endEffect();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes all effects from controllers and ends them appropriately Passive effect will not be removed
	 */
	public void removeAllEffects() {
		this.removeAllEffects(false);
	}

	public void removeAllEffects(boolean logout) {
		if (logout) {
			// remove all effects on logout
			lock.writeLock().lock();
			try {
				for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
					Effect effect = iter.next();
					iter.remove();
					effect.endEffect(false);
				}
				for (Iterator<Effect> iter = noshowEffects.values().iterator(); iter.hasNext();) {
					Effect effect = iter.next();
					iter.remove();
					effect.endEffect(false);
				}
				for (Iterator<Effect> iter = passiveEffectMap.values().iterator(); iter.hasNext();) {
					Effect effect = iter.next();
					iter.remove();
					effect.endEffect(false);
				}
			} finally {
				lock.writeLock().unlock();
			}
		} else {
			lock.writeLock().lock();
			try {
				for (Iterator<Effect> iter = abnormalEffectMap.values().iterator(); iter.hasNext();) {
					Effect effect = iter.next();
					if (!effect.getSkillTemplate().isNoRemoveAtDie() && !effect.isXpBoost()) {
						iter.remove();
						effect.endEffect(false);
					}
				}
				for (Iterator<Effect> iter = noshowEffects.values().iterator(); iter.hasNext();) {
					Effect effect = iter.next();
					iter.remove();
					effect.endEffect(false);
				}
			} finally {
				lock.writeLock().unlock();
			}
			broadCastEffects(null);
		}
	}

	/**
	 * Return true if skillId is present among creature's effects
	 */
	public boolean isPresentBySkillId(int skillId) {
		Map<String, Effect> effects = getMapForEffect(skillId);
		lock.readLock().lock();
		try {
			for (Effect effect : effects.values()) {
				if (effect.getSkillId() == skillId)
					return true;
			}
		} finally {
			lock.readLock().unlock();
		}
		return false;
	}

	/**
	 * return true if creature is under Fear effect
	 */
	public boolean isUnderFear() {
		return isAbnormalSet(AbnormalState.FEAR);
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
			return abnormalEffectMap.values().stream().filter(e -> e.getSkillTemplate().getTargetSlot() != SkillTargetSlot.NOSHOW)
				.collect(Collectors.toList());
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
		if (state == AbnormalState.BUFF)
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
		if (state == AbnormalState.BUFF)
			return abnormals == 0;
		return (abnormals & state.getId()) != 0;
	}

	public int getAbnormals() {
		return abnormals;
	}

	public boolean isEmpty() {
		return abnormalEffectMap.isEmpty();
	}

	public void checkEffectCooldownId(Effect effect) {
		Collection<Effect> effects = getAbnormalEffectsToShow();
		int cdId = effect.getSkillTemplate().getCooldownId();
		int copiedId = 0;
		int size = 0;
		if (cdId == 1)
			return;
		switch (cdId) {
			case 273: // Erosion & Flamecage
			case 353: // Lockdown & Dazing Severe Blow
				size = 2;
				break;
			case 6:
				size = 10;
				break;
		}
		copiedId = cdId;
		if (effects.size() >= size) {
			int i = 0;
			Effect toRemove = null;
			for (Effect eff : effects) {
				if (eff.getSkillTemplate().getCooldownId() == copiedId && eff.getTargetSlot() == effect.getTargetSlot()) {
					i++;
					if (toRemove == null)
						toRemove = eff;
				}
			}
			if (i >= size && toRemove != null)
				toRemove.endEffect();
		}
		// archer buffs
		int count = 0;
		Effect toRemove = null;
		for (Effect eff : effects) {
			switch (eff.getSkillTemplate().getCooldownId()) {
				case 2020: // Dodging
				case 2022: // Focused Shots
				case 2024: // Aiming
				case 2026: // Bestial Fury
				case 2028: // Hunter's Eye
				case 2030: // Strong Shots
					count++;
					if (toRemove == null) {
						toRemove = eff;
						continue;
					}
					if (count >= 2 && cdId >= 2020 && cdId <= 2030)
						toRemove.endEffect();
					break;
			}
		}
	}

}

package com.aionemu.gameserver.controllers.effect;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastMap;
import javolution.util.FastTable;

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
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.DispelType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectResult;
import com.aionemu.gameserver.skillengine.model.SkillSubType;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author ATracer modified by Wakizashi, Sippolo, Cheatkiller
 */
public class EffectController {

	private Creature owner;

	protected Map<String, Effect> passiveEffectMap = new FastMap<String, Effect>().shared();
	protected Map<String, Effect> noshowEffects = new FastMap<String, Effect>().shared();
	protected Map<String, Effect> abnormalEffectMap = new FastMap<String, Effect>().shared();
	private long abnormalMapUpdate;

	private final Lock lock = new ReentrantLock();

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

		lock.lock();
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
					/**
					 * idea here is that effects with same effectId shouldnt stack effect with higher basiclvl takes priority
					 */
					for (Effect effect : mapToUpdate.values()) {
						if (effect.getTargetSlot() == nextEffect.getTargetSlot()) {
							for (EffectTemplate et : effect.getEffectTemplates()) {
								if (et.getEffectid() == 0)
									continue;
								for (EffectTemplate et2 : nextEffect.getEffectTemplates()) {
									if (et2.getEffectid() == 0)
										continue;
									if (et.getEffectid() == et2.getEffectid()) {
										if (et.getBasicLvl() > et2.getBasicLvl())
											return;
										else
											effect.endEffect();
									}
								}
							}
						}
					}
				}
			}

			Effect conflictedEffect = findConflictedEffect(mapToUpdate, nextEffect);
			if (conflictedEffect != null) {
				conflictedEffect.endEffect();
			}

			if (!nextEffect.isPassive()) {
				if (searchConflict(nextEffect))
					return;

				checkEffectCooldownId(nextEffect);
			}

			// max 3 aura effects or 1 toggle skill in noshoweffects
			if (nextEffect.isToggle() && nextEffect.getTargetSlotEnum() == SkillTargetSlot.NOSHOW) {
				int mts = nextEffect.getSkillSubType() == SkillSubType.CHANT ? 3 : 1;
				//Rangers & Riders are allowed to use 2 Toggle skills. There might be a pattern.
				if (nextEffect.getEffector() instanceof Player && (((Player) nextEffect.getEffector()).getPlayerClass() == PlayerClass.RANGER ||
						((Player) nextEffect.getEffector()).getPlayerClass() == PlayerClass.RIDER)) {
					mts = 2;
				}
				Collection<Effect> filteredMap = nextEffect.getSkillSubType() == SkillSubType.CHANT ? getAuraEffects() : getNoShowToggleEffects();
				if (filteredMap.size() >= mts) {
					Iterator<Effect> iter = filteredMap.iterator();
					iter.next().endEffect();
				}
			}

			// max 4 chants
			if (nextEffect.isChant()) {
				Collection<Effect> chants = this.getChantEffects();
				if (chants.size() >= 4) {
					Iterator<Effect> chantIter = chants.iterator();
					chantIter.next().endEffect();
				}
			}

			boolean shouldAdd = this.shouldAddBeforeLastEffect(nextEffect);
			if (shouldAdd) {
				addBeforeLastElement(mapToUpdate, nextEffect);
			} else {
				this.abnormalMapUpdate = System.currentTimeMillis();
				mapToUpdate.put(nextEffect.getStack(), nextEffect);
			}
		} finally {
			lock.unlock();
		}

		// ? move into lock area
		nextEffect.startEffect(false);

		if (!nextEffect.isPassive()) {
			broadCastEffects(nextEffect);
		}
	}

	public void addBeforeLastElement(Map<String, Effect> mapToUpdate, Effect newEffect) {
		int i = 0;
		Effect lastEffect = null;
		for (Effect e : mapToUpdate.values()) {
			if (i == mapToUpdate.size() - 1)
				lastEffect = e;
			i++;
		}
		if (lastEffect != null && !(lastEffect.getEffector() instanceof Player))
			mapToUpdate.remove(lastEffect.getStack());
		mapToUpdate.put(newEffect.getStack(), newEffect);
		if (lastEffect != null && !(lastEffect.getEffector() instanceof Player))
			mapToUpdate.put(lastEffect.getStack(), lastEffect);
	}

	/**
	 * @param mapToUpdate
	 * @param newEffect
	 * @return
	 */
	private final Effect findConflictedEffect(Map<String, Effect> mapToUpdate, Effect newEffect) {
		int conflictId = newEffect.getSkillTemplate().getConflictId();
		if (conflictId == 0) {
			return null;
		}
		for (Effect effect : mapToUpdate.values()) {
			if (effect.getSkillTemplate().getConflictId() == conflictId) {
				return effect;
			}
		}
		return null;
	}

	/**
	 * @param effect
	 * @return
	 */
	private Map<String, Effect> getMapForEffect(Effect effect) {
		if (effect.isPassive())
			return passiveEffectMap;

		if (effect.isToggle() && effect.getTargetSlotEnum() == SkillTargetSlot.NOSHOW)
			return noshowEffects;

		return abnormalEffectMap;
	}

	private boolean shouldAddBeforeLastEffect(Effect effect) {
		return effect.getSkill() != null && System.currentTimeMillis() - abnormalMapUpdate < effect.getSkill().getHitTime();
	}

	/**
	 * @param stack
	 * @return abnormalEffectMap
	 */
	public Effect getAnormalEffect(String stack) {
		return abnormalEffectMap.get(stack);
	}

	/**
	 * @param skillId
	 * @return
	 */
	public boolean hasAbnormalEffect(int skillId) {
		Iterator<Effect> localIterator = this.abnormalEffectMap.values().iterator();
		while (localIterator.hasNext()) {
			Effect localEffect = localIterator.next();
			if (localEffect.getSkillId() == skillId)
				return true;
		}
		return false;
	}

	public void broadCastEffects(Effect effect) {
		int slot = effect != null ? effect.getTargetSlotEnum().getId() : SkillTargetSlot.FULLSLOTS;
		List<Effect> effects = getAbnormalEffects();
		PacketSendUtility.broadcastPacket(getOwner(), new SM_ABNORMAL_EFFECT(getOwner(), abnormals, effects, slot));
	}

	/**
	 * @param effect
	 */
	public void clearEffect(Effect effect, boolean broadCastEffects) {
		Map<String, Effect> mapForEffect = getMapForEffect(effect);
		mapForEffect.remove(effect.getStack());
		if (broadCastEffects)
			broadCastEffects(effect);
	}

	public void clearEffect(Effect effect) {
		clearEffect(effect, true);
	}

	/**
	 * Removes the effect by skillid.
	 * 
	 * @param skillid
	 */
	public void removeEffect(int skillid) {
		for (Effect effect : abnormalEffectMap.values()) {
			if (effect.getSkillId() == skillid) {
				effect.endEffect();
			}
		}

		for (Effect effect : passiveEffectMap.values()) {
			if (effect.getSkillId() == skillid) {
				effect.endEffect();
			}
		}

		for (Effect effect : noshowEffects.values()) {
			if (effect.getSkillId() == skillid) {
				effect.endEffect();
			}
		}
	}

	public void removeHideEffects() {
		for (Effect effect : abnormalEffectMap.values()) {
			if (effect.isHideEffect() && owner.getVisualState() < 10) {
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}

	/**
	 * Removes Petorderunsummon effects from owner.
	 */
	public void removePetOrderUnSummonEffects() {
		for (Effect effect : abnormalEffectMap.values()) {
			if (effect.isPetOrderUnSummonEffect()) {
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}

	/**
	 * Removes Paralyze effects from owner.
	 */
	public void removeParalyzeEffects() {
		for (Effect effect : abnormalEffectMap.values()) {
			if (effect.isParalyzeEffect()) {
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}

	/**
	 * Removes Stun effects from owner.
	 */
	public void removeStunEffects() {
		for (Effect effect : abnormalEffectMap.values()) {
			if (effect.isStunEffect()) {
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
		}
	}

	/**
	 * Removes Transform effects from owner. for more info see TransformEffect it doesnt remove Avatar transforms (TODO find out on retail)
	 */
	public void removeTransformEffects() {
		for (Effect effect : abnormalEffectMap.values()) {
			for (EffectTemplate et : effect.getEffectTemplates()) {
				if (et instanceof TransformEffect && ((TransformEffect) et).getTransformType() != TransformType.AVATAR) {
					effect.endEffect();
					abnormalEffectMap.remove(effect.getStack());
				}
			}
		}
	}

	/**
	 * Removes all effects by given target slot
	 *
	 * @see TargetSlot
	 * @param targetSlot
	 */
	public void removeAbnormalEffectsByTargetSlot(SkillTargetSlot targetSlot) {
		this.removeByDispelEffect(DispelType.SLOTTYPE, targetSlot.toString(), 255, 100, 100);
	}

	public boolean removeByEffectId(int effectId, int dispelLevel, int power) {
		// abnormalEffectMap
		for (Effect effect : abnormalEffectMap.values()) {
			if (!effect.containsEffectId(effectId))
				continue;
			// check dispel level
			if (effect.getReqDispelLevel() > dispelLevel)
				continue;

			if (removePower(effect, power)) {
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
				return true;
			}
		}
		// noshowEffects
		for (Effect effect : noshowEffects.values()) {
			if (!effect.containsEffectId(effectId))
				continue;
			// check dispel level
			if (effect.getReqDispelLevel() > dispelLevel)
				continue;

			if (removePower(effect, power)) {
				effect.endEffect();
				noshowEffects.remove(effect.getStack());
				return true;
			}
		}
		return false;
	}

	/**
	 * @param dispelType
	 * @param value
	 * @param count
	 * @param dispelLevel
	 * @param power
	 */
	public void removeByDispelEffect(DispelType dispelType, String value, int count, int dispelLevel, int power) {
		int effectId = 0;
		EffectType effectType = null;
		SkillTargetSlot skillTargetSlot = null;
		switch (dispelType) {
			case EFFECTIDRANGE:
				effectId = Integer.parseInt(value);
				break;
			case EFFECTTYPE:
				effectType = EffectType.valueOf(value);
				break;
			case SLOTTYPE:
				skillTargetSlot = SkillTargetSlot.valueOf(value);
				break;
		}
		// abnormalEffectMap
		for (Effect effect : abnormalEffectMap.values()) {
			// check count
			if (count == 0)
				break;
			switch (dispelType) {
				case EFFECTIDRANGE:
					if (!effect.containsEffectId(effectId))
						continue;
					break;
				case EFFECTTYPE:
					if (!effect.getSkillTemplate().getEffects().isEffectTypePresent(effectType))
						continue;
					if (effectType.equals(EffectType.STUN) && effect.getSkillId() == 11904) { // avatar skill irremovable by Remove Shock TODO: logic
						continue;
					}
					break;
				case SLOTTYPE:
					if (effect.getTargetSlot() != skillTargetSlot.ordinal())
						continue;
					break;
			}
			// check dispel level
			if (effect.getReqDispelLevel() > dispelLevel)
				continue;

			if (removePower(effect, power)) {
				effect.endEffect();
				abnormalEffectMap.remove(effect.getStack());
			}
			// decrease count
			count--;
		}
		// noshowEffects
		for (Effect effect : noshowEffects.values()) {
			// check count
			if (count == 0)
				break;
			switch (dispelType) {
				case EFFECTIDRANGE:
					if (!effect.containsEffectId(effectId))
						continue;
					break;
				case EFFECTTYPE:
					if (!effect.getSkillTemplate().getEffects().isEffectTypePresent(effectType))
						continue;
					if (effectType.equals(EffectType.STUN) && effect.getSkillId() == 11904) { // avatar skill irremovable by Remove Shock TODO: logic
						continue;
					}
					break;
				case SLOTTYPE:
					if (effect.getTargetSlot() != skillTargetSlot.ordinal())
						continue;
					break;
			}
			// check dispel level
			if (effect.getReqDispelLevel() > dispelLevel)
				continue;

			if (removePower(effect, power)) {
				effect.endEffect();
				noshowEffects.remove(effect.getStack());
			}
			// decrease count
			count--;
		}
	}

	/**
	 * Method used to calculate number of effects of given dispelcategory, targetslot and dispelLevel used only in DispelBuffCounterAtk, therefore rest
	 * of cases are skipped
	 * @param dispelLevel
	 * @return
	 */
	public int calculateNumberOfEffects(int dispelLevel) {
		int number = 0;

		for (Effect effect : abnormalEffectMap.values()) {
			DispelCategoryType dispelCat = effect.getDispelCategory();
			SkillTargetSlot tragetSlot = effect.getSkillTemplate().getTargetSlot();
			// effects with duration 86400000 cant be dispelled
			// TODO recheck
			if (effect.getDuration() >= 86400000 && !removebleEffect(effect))
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

		return number;
	}

	public void removeEffectByDispelCat(DispelCategoryType dispelCat, SkillTargetSlot targetSlot, int count, int dispelLevel, int power,
		boolean itemTriggered) {
		for (Effect effect : abnormalEffectMap.values()) {
			if (count == 0)
				break;
			// effects with duration 86400000 cant be dispelled
			// TODO recheck
			if (effect.getDuration() >= 86400000 && !removebleEffect(effect)) {
				continue;
			}

			if (effect.isSanctuaryEffect())
				continue;

			// check for targetslot, effects with target slot level higher or equal to 2 cant be removed (ex. skillId: 11885)
			if (effect.getTargetSlot() != targetSlot.ordinal() || effect.getTargetSlotLevel() >= 2)
				continue;

			boolean remove = false;
			switch (dispelCat) {
				case ALL:// DispelDebuffEffect
					if ((effect.getDispelCategory() == DispelCategoryType.ALL || effect.getDispelCategory() == DispelCategoryType.DEBUFF_MENTAL || effect
						.getDispelCategory() == DispelCategoryType.DEBUFF_PHYSICAL) && effect.getReqDispelLevel() <= dispelLevel)
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
	}

	private int removeEffectByTargetSlot(int count, SkillTargetSlot targetSlot, int dispelLevel, int power) {
		for (Effect effect : abnormalEffectMap.values()) {
			SkillTargetSlot ts = effect.getSkillTemplate().getTargetSlot();
			DispelCategoryType dispelCat = effect.getDispelCategory();
			if (ts == targetSlot) {
				if (count == 0)
					break;
				if (effect.getDuration() >= 86400000 && !removebleEffect(effect))
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
						effect.endEffect();
						abnormalEffectMap.remove(effect.getStack());
					} else if (owner instanceof Player)
						PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELCOUNT());
					count--;
				} else if (owner instanceof Player)
					PacketSendUtility.sendPacket((Player) owner, SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELLEVEL());
			}
		}
		return count;
	}

	public void dispelBuffCounterAtkEffect(int count, int dispelLevel, int power) {
		if (removeEffectByTargetSlot(count, SkillTargetSlot.BUFF, dispelLevel, power) > 0)
			removeEffectByTargetSlot(count, SkillTargetSlot.DEBUFF, dispelLevel, power);
	}

	// TODO this should be removed
	private boolean removebleEffect(Effect effect) {
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
		int effectPower = effect.removePower(power);

		return effectPower <= 0;
	}

	/**
	 * Removes the effect by skillid.
	 * 
	 * @param skillid
	 */
	public void removePassiveEffect(int skillid) {
		for (Effect effect : passiveEffectMap.values()) {
			if (effect.getSkillId() == skillid) {
				effect.endEffect();
			}
		}
	}

	/**
	 * @param skillid
	 */
	public void removeNoshowEffect(int skillid) {
		for (Effect effect : noshowEffects.values()) {
			if (effect.getSkillId() == skillid) {
				effect.endEffect();
			}
		}
	}

	/**
	 * Removes all effects from controllers and ends them appropriately Passive effect will not be removed
	 */
	public void removeAllEffects() {
		this.removeAllEffects(false);
	}

	public void removeAllEffects(boolean logout) {
		if (!logout) {
			Iterator<Map.Entry<String, Effect>> it = abnormalEffectMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Effect> entry = it.next();
				// TODO recheck - kecimis
				if (!entry.getValue().getSkillTemplate().isNoRemoveAtDie() && !entry.getValue().isXpBoost()) {
					entry.getValue().endEffect();
					it.remove();
				}
			}

			for (Effect effect : noshowEffects.values()) {
				effect.endEffect();
			}
			noshowEffects.clear();
		} else {
			// remove all effects on logout
			for (Effect effect : abnormalEffectMap.values()) {
				effect.endEffect();
			}
			abnormalEffectMap.clear();
			for (Effect effect : noshowEffects.values()) {
				effect.endEffect();
			}
			noshowEffects.clear();
			for (Effect effect : passiveEffectMap.values()) {
				effect.endEffect();
			}
			passiveEffectMap.clear();
		}
	}

	/**
	 * Return true if skillId is present among creature's abnormals
	 */
	public boolean isAbnormalPresentBySkillId(int skillId) {
		for (Effect effect : abnormalEffectMap.values()) {
			if (effect.getSkillId() == skillId)
				return true;
		}
		return false;
	}

	public boolean isNoshowPresentBySkillId(int skillId) {
		for (Effect effect : noshowEffects.values()) {
			if (effect.getSkillId() == skillId)
				return true;
		}
		return false;
	}

	public boolean isPassivePresentBySkillId(int skillId) {
		for (Effect effect : passiveEffectMap.values()) {
			if (effect.getSkillId() == skillId)
				return true;
		}
		return false;
	}

	/**
	 * return true if creature is under Fear effect
	 */
	public boolean isUnderFear() {
		return isAbnormalSet(AbnormalState.FEAR);
	}

	public void updatePlayerEffectIcons(Effect effect) {
	}

	public void updatePlayerEffectIconsImpl() {
	}

	/**
	 * @return copy of anbornals list
	 */
	public List<Effect> getAbnormalEffects() {
		List<Effect> effects = new FastTable<Effect>();
		Iterator<Effect> iterator = iterator();
		while (iterator.hasNext()) {
			Effect effect = iterator.next();
			if (effect != null)
				effects.add(effect);
		}
		return effects;
	}

	/**
	 * @return list of effects to display as top icons
	 */
	public Collection<Effect> getAbnormalEffectsToTargetSlot(final int slot) {
		return Collections2.filter(abnormalEffectMap.values(), new Predicate<Effect>() {

			@Override
			public boolean apply(Effect effect) {
				return effect.getTargetSlotEnum().getId() == slot;
			}
		});
	}

	/**
	 * @return list of effects to display as top icons
	 */
	public Collection<Effect> getAbnormalEffectsToShow() {
		return Collections2.filter(abnormalEffectMap.values(), new Predicate<Effect>() {

			@Override
			public boolean apply(Effect effect) {
				return effect.getSkillTemplate().getTargetSlot() != SkillTargetSlot.NOSHOW;
			}
		});
	}

	public Collection<Effect> getChantEffects() {
		return Collections2.filter(abnormalEffectMap.values(), new Predicate<Effect>() {

			@Override
			public boolean apply(Effect effect) {
				return effect.isChant();
			}
		});
	}

	public Collection<Effect> getAuraEffects() {
		return Collections2.filter(this.noshowEffects.values(), new Predicate<Effect>() {

			@Override
			public boolean apply(Effect effect) {
				return effect.getSkillSubType() == SkillSubType.CHANT;
			}
		});
	}

	/**
	 * returns toggle noshow effects that are not auras
	 * 
	 * @return
	 */
	public Collection<Effect> getNoShowToggleEffects() {
		return Collections2.filter(this.noshowEffects.values(), new Predicate<Effect>() {

			@Override
			public boolean apply(Effect effect) {
				return effect.getSkillSubType() != SkillSubType.CHANT;
			}
		});
	}

	/**
	 * ABNORMAL EFFECTS
	 */

	public void setAbnormal(int mask) {
		owner.getObserveController().notifyAbnormalSettedObservers(AbnormalState.getStateById(mask));
		abnormals |= mask;

		// TODO move to observer?
		// if player is sitting when setting certain abnormal states, he is forcefully made to stand up
		if (owner instanceof Player && owner.isInState(CreatureState.RESTING)) {
			if (this.isInAnyAbnormalState(AbnormalState.AUTOMATICALLY_STANDUP)) {
				owner.unsetState(CreatureState.RESTING);
				PacketSendUtility.broadcastPacket((Player) owner, new SM_EMOTION(owner, EmotionType.STAND), true);
			}
		}
	}

	public void unsetAbnormal(int mask) {
		int count = 0;
		for (Effect effect : abnormalEffectMap.values()) {
			if ((effect.getAbnormals() & mask) == mask)
				count++;
		}
		if (count <= 1)
			abnormals &= ~mask;
	}

	/**
	 * Used for checking unique abnormal states
	 * 
	 * @param id
	 * @return
	 */
	public boolean isAbnormalSet(AbnormalState id) {
		return (abnormals & id.getId()) == id.getId();
	}

	/**
	 * Used for compound abnormal state checks
	 * 
	 * @param id
	 * @return
	 */
	public boolean isAbnormalState(AbnormalState id) {
		if (id == AbnormalState.BUFF)
			return abnormals == 0;
		return (abnormals & id.getId()) == id.getId();
	}

	public boolean isInAnyAbnormalState(AbnormalState id) {
		if (id == AbnormalState.BUFF)
			return abnormals == 0;
		return (abnormals & id.getId()) != 0;
	}

	public int getAbnormals() {
		return abnormals;
	}

	/**
	 * @return
	 */
	public Iterator<Effect> iterator() {
		return abnormalEffectMap.values().iterator();
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
		for (Iterator<Effect> iter = effects.iterator(); iter.hasNext();) {
			Effect eff = iter.next();
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

	private boolean checkExtraEffect(Effect nextEffect) {
		Map<String, Effect> mapToUpdate = getMapForEffect(nextEffect);
		for (Effect effect : mapToUpdate.values()) {
			if (nextEffect.getDispelCategory() == DispelCategoryType.EXTRA) {
				if (effect.getDispelCategory() == DispelCategoryType.EXTRA) {
					if (effect.getSkillTemplate().getSkillId() != 21610 && effect.getSkillTemplate().getSkillId() != 21611
						&& effect.getSkillTemplate().getSkillId() != 21612) {
						effect.endEffect();
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean searchConflict(Effect nextEffect) {
		Map<String, Effect> mapToUpdate = getMapForEffect(nextEffect);
		if (checkExtraEffect(nextEffect))
			return false;
		for (Effect effect : mapToUpdate.values()) {
			if (effect.getSkillSubType().equals(nextEffect.getSkillSubType()) || effect.getTargetSlotEnum().equals(nextEffect.getTargetSlotEnum())) {
				effectCheck:
				for (EffectTemplate et : effect.getEffectTemplates()) {
					if (et.getEffectid() == 0)
						continue;
					for (EffectTemplate et2 : nextEffect.getEffectTemplates()) {
						if (et2.getEffectid() == 0)
							continue;
						if (et.getEffectid() == et2.getEffectid()) {
							if (et.getBasicLvl() > et2.getBasicLvl()) {
								if (nextEffect.getTargetSlotEnum() != SkillTargetSlot.DEBUFF)
									nextEffect.setEffectResult(EffectResult.CONFLICT);
								return true;
							} else {
								effect.endEffect(false, false);
								break effectCheck;
							}
						}
					}
				}
			}
		}
		return false;
	}
}

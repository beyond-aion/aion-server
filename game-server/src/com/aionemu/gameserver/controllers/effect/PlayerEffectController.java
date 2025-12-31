package com.aionemu.gameserver.controllers.effect;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_STATE;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerEffectController extends EffectController {

	private final Map<CumulativeResistType, CumulativeResist> cumulativeResistInfo = new EnumMap<>(CumulativeResistType.class);
	private boolean keepBuffsOnDie;

	public PlayerEffectController(Creature owner) {
		super(owner);
	}

	public void setKeepBuffsOnDie(boolean keepBuffsOnDie) {
		this.keepBuffsOnDie = keepBuffsOnDie;
	}

	@Override
	public void addEffect(Effect effect) {
		if (checkDuelCondition(effect) && !effect.isForcedEffect())
			return;
		super.addEffect(effect);
		updatePlayerIconsAndGroup(effect);
	}

	@Override
	public void clearEffect(Effect effect, boolean broadcast) {
		super.clearEffect(effect, broadcast);
		if (broadcast)
			updatePlayerIconsAndGroup(effect);
	}

	@Override
	public Player getOwner() {
		return (Player) super.getOwner();
	}

	@Override
	public void removeAllEffects(boolean logout) {
		super.removeAllEffects(logout);
		if (!logout)
			updatePlayerIconsAndGroup(null);
	}

	/**
	 * Removes non-storable effects and their conditional effects (like Aethertech buffs)
	 */
	public void removeNonStorableEffectsForLogout() {
		getAllEffects().stream().filter(e -> !e.canSaveOnLogout()).forEach(e -> e.endEffect(false));
	}

	private void updatePlayerIconsAndGroup(Effect effect) {
		if (effect == null || !effect.isPassive()) {
			updatePlayerEffectIcons(effect);
			int slot = effect == null ? SkillTargetSlot.FULLSLOTS : effect.getTargetSlot().getId();
			if (getOwner().isInGroup()) {
				PlayerGroupService.updateGroup(getOwner(), GroupEvent.MOVEMENT);
				PlayerGroupService.updateGroupEffects(getOwner(), slot);
			} else if (getOwner().isInAlliance()) {
				PlayerAllianceService.updateAlliance(getOwner(), PlayerAllianceEvent.MOVEMENT);
				PlayerAllianceService.updateAllianceEffects(getOwner(), slot);
			}
		}
	}

	public void updatePlayerEffectIcons(Effect effect) {
		int slot = effect != null ? effect.getTargetSlot().getId() : SkillTargetSlot.FULLSLOTS;
		Collection<Effect> effects = getAbnormalEffectsToShow();
		PacketSendUtility.sendPacket(getOwner(), new SM_ABNORMAL_STATE(effects, getAbnormals(), slot));
	}

	/**
	 * Effect of DEBUFF should not be added if duel ended (friendly unit)
	 */
	private boolean checkDuelCondition(Effect effect) {
		return effect.getTargetSlot() == SkillTargetSlot.DEBUFF && effect.getEffector() instanceof Player player && !getOwner().equals(player) && !getOwner().isEnemy(player);
	}

	public void addSavedEffect(int skillId, int skillLvl, int remainingTime, long endTime, ForceType forceType) {
		if (EventService.getInstance().isInactiveEventForceType(forceType))
			return;
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (remainingTime <= 0)
			return;
		if (CustomConfig.ABYSSXFORM_LOGOUT && template.isDeityAvatar()) {

			if (System.currentTimeMillis() >= endTime)
				return;
			else
				remainingTime = (int) (endTime - System.currentTimeMillis());
		}

		Effect effect = new Effect(getOwner(), getOwner(), template, skillLvl, remainingTime, forceType);
		put(effect);
		effect.addAllEffectToSucess();
		effect.startEffect();

		if (effect.getSkillTemplate().getTargetSlot() != SkillTargetSlot.NOSHOW)
			PacketSendUtility.sendPacket(getOwner(), new SM_ABNORMAL_STATE(Collections.singletonList(effect), getAbnormals(), SkillTargetSlot.FULLSLOTS));
	}

	@Override
	public void removeAllEffects() {
		super.removeAllEffects();
		synchronized (cumulativeResistInfo) {
			cumulativeResistInfo.clear();
		}
	}

	@Override
	protected boolean canRemoveOnDie(Effect effect) {
		if (!super.canRemoveOnDie(effect))
			return false;
		if (keepBuffsOnDie)
			return effect.getTargetSlot() == SkillTargetSlot.DEBUFF;
		return true;
	}

	public long calculateAndApplyCumulativeResistDuration(CumulativeResistType type, long duration) {
		synchronized (cumulativeResistInfo) {
			CumulativeResist cumulativeResist = cumulativeResistInfo.computeIfAbsent(type, _ -> new CumulativeResist());
			// retail uses a dynamic duration after the last successful fear/para/sleep, which we approximate by multiplying the base duration * 2 for now
			cumulativeResist.tryIncrementLevel(duration * 2);
			return (long) (duration * cumulativeResist.getDurationMultiplier());
		}
	}

	public int getCumulativeResistance(CumulativeResistType type) {
		synchronized (cumulativeResistInfo) {
			CumulativeResist cumulativeResist = cumulativeResistInfo.get(type);
			return cumulativeResist == null ? 0 : cumulativeResist.getResistance();
		}
	}
}

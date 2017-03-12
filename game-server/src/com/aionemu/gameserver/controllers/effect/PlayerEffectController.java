package com.aionemu.gameserver.controllers.effect;

import java.util.Collection;
import java.util.Collections;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.common.legacy.GroupEvent;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABNORMAL_STATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STANCE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PlayerEffectController extends EffectController {

	public PlayerEffectController(Creature owner) {
		super(owner);
	}

	@Override
	public void addEffect(Effect effect) {
		if (checkDuelCondition(effect) && !effect.getIsForcedEffect())
			return;
		super.addEffect(effect);
		updatePlayerIconsAndGroup(effect, true);
	}

	@Override
	public void clearEffect(Effect effect, boolean broadcast) {
		super.clearEffect(effect, broadcast);
		updatePlayerIconsAndGroup(effect, broadcast);
	}

	@Override
	public Player getOwner() {
		return (Player) super.getOwner();
	}

	/**
	 * @param effect
	 */
	private void updatePlayerIconsAndGroup(Effect effect, boolean broadcast) {
		if (!effect.isPassive() && broadcast) {
			updatePlayerEffectIcons(effect);
			if (getOwner().isInGroup()) {
				PlayerGroupService.updateGroup(getOwner(), GroupEvent.MOVEMENT);
				PlayerGroupService.updateGroupEffects(getOwner(), effect.getTargetSlot().getId());
				PlayerGroupService.updateGroup(getOwner(), GroupEvent.MOVEMENT);
			} else if (getOwner().isInAlliance()) {
				PlayerAllianceService.updateAlliance(getOwner(), PlayerAllianceEvent.MOVEMENT);
				PlayerAllianceService.updateAllianceEffects(getOwner(), effect.getTargetSlot().getId());
				PlayerAllianceService.updateAlliance(getOwner(), PlayerAllianceEvent.MOVEMENT);
			}
		}
	}

	@Override
	public void updatePlayerEffectIcons(Effect effect) {
		int slot = effect != null ? effect.getTargetSlot().getId() : SkillTargetSlot.FULLSLOTS;
		Collection<Effect> effects = getAbnormalEffectsToShow();
		PacketSendUtility.sendPacket(getOwner(), new SM_ABNORMAL_STATE(effects, abnormals, slot));
	}

	/**
	 * Effect of DEBUFF should not be added if duel ended (friendly unit)
	 * 
	 * @param effect
	 * @return
	 */
	private boolean checkDuelCondition(Effect effect) {
		Creature creature = effect.getEffector();
		if (creature instanceof Player) {
			if (!getOwner().isEnemy(creature) && effect.getTargetSlot() == SkillTargetSlot.DEBUFF) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param skillId
	 * @param skillLvl
	 * @param currentTime
	 * @param reuseDelay
	 */
	public void addSavedEffect(int skillId, int skillLvl, int remainingTime, long endTime) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

		if (remainingTime <= 0)
			return;
		if (CustomConfig.ABYSSXFORM_LOGOUT && template.isDeityAvatar()) {

			if (System.currentTimeMillis() >= endTime)
				return;
			else
				remainingTime = (int) (endTime - System.currentTimeMillis());
		}

		Effect effect = new Effect(getOwner(), getOwner(), template, skillLvl, remainingTime);
		getMapForEffect(effect).put(effect.getStack(), effect);
		effect.addAllEffectToSucess();
		effect.startEffect(true);

		if (effect.getSkillTemplate().getTargetSlot() != SkillTargetSlot.NOSHOW)
			PacketSendUtility.sendPacket(getOwner(), new SM_ABNORMAL_STATE(Collections.singletonList(effect), abnormals, SkillTargetSlot.FULLSLOTS));

	}

	@Override
	public void broadCastEffects(Effect effect) {
		super.broadCastEffects(effect);
		Player player = getOwner();
		if (player.getController().isUnderStance()) {
			PacketSendUtility.sendPacket(player, new SM_PLAYER_STANCE(player, 1));
		}
	}
}

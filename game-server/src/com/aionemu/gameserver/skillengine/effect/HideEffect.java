package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_STATE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Sweetkr, Cura
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HideEffect")
public class HideEffect extends BufEffect {

	@XmlAttribute
	protected CreatureVisualState state;
	@XmlAttribute(name = "bufcount")
	protected int buffCount;
	@XmlAttribute
	protected int type = 0;

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect);

		final Creature effected = effect.getEffected();
		effected.unsetVisualState(state);
		effected.getEffectController().unsetAbnormal(AbnormalState.HIDE);
		effected.getController().onHideEnd();
		PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected)); // update visibility
	}

	@Override
	public void startEffect(final Effect effect) {
		super.startEffect(effect);

		final Creature effected = effect.getEffected();
		effected.getEffectController().setAbnormal(AbnormalState.HIDE);
		effect.setAbnormal(AbnormalState.HIDE);

		effected.setVisualState(state);

		// Cancel targeted enemy cast
		AttackUtil.cancelCastOn(effected);

		// send all to set new 'effected' visual state (remove all visual targetting from 'effected')
		PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected));

		ThreadPoolManager.getInstance().schedule(() -> {
			// do on all who targetting on 'effected' (set target null, cancel attack skill, cancel npc pursuit)
			AttackUtil.removeTargetFrom(effected, true);
		}, 500);

		effected.getController().onHide();
		// for player adding: Remove Hide when using any item action . when requesting dialog to any npc . when being attacked . when attacking
		if (effected instanceof Player) {

			// Remove Hide when use skill / item skill
			effect.addObserver(effected, new ActionObserver(ObserverType.STARTSKILLCAST) {

				private int buffNumber = 0;

				@Override
				public void startSkillCast(Skill skill) {
					// TODO find better way
					if (skill.getSkillMethod() == SkillMethod.ITEM) {
						if (skill.getItemTemplate().isPotion() || skill.getSkillTemplate().getDuration() > 0)
							effect.endEffect();
						return;
					}
					boolean isShapeChange = skill.getSkillTemplate().getEffects().hasAnyEffectType(EffectType.SHAPECHANGE);
					if (isShapeChange || !skill.isSelfBuff() || ++buffNumber >= buffCount)
						effect.endEffect();
				}
			});
			effect.addObserver(effected, new ActionObserver(ObserverType.ATTACK) {

				@Override
				public void attack(Creature creature, int skillId) {
					effect.endEffect();
				}
			});
			effect.addObserver(effected, new ActionObserver(ObserverType.ITEMUSE) {

				@Override
				public void itemused(Item item) {
					// [4.5] Buff items do not affect Hide II. Hide I is cancelled.
					ItemActions actions = item.getItemTemplate().getActions();
					if (actions != null) {
						if (buffCount == 0 || actions.getSkillUseAction() == null)
							effect.endEffect();
					}
				}
			});

			// type >= 1, hide is maintained even after damage
			if (type == 0)
				effect.setCancelOnDmg(true);
		} else { // effected is npc
			if (type == 0) { // type >= 1, hide is maintained even after damage
				effect.setCancelOnDmg(true);

				// Remove Hide when attacking
				effect.addObserver(effected, new ActionObserver(ObserverType.ATTACK) {

					@Override
					public void attack(Creature creature, int skillId) {
						effect.endEffect();
					}

				});

				// Remove Hide when use skill
				effect.addObserver(effected, new ActionObserver(ObserverType.STARTSKILLCAST) {

					@Override
					public void startSkillCast(Skill skill) {
						effect.endEffect();
					}

				});
			}
		}
	}
}

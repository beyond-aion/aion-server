package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

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

		// send all to set new 'effected' visual state (remove all visual targetting from 'effected')
		PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected));

		effected.getController().onHide();
		// for player adding: Remove Hide when using any item action . when requesting dialog to any npc . when being attacked . when attacking
		if (effected instanceof Player) {

			HideCounter counter = new HideCounter(buffCount);
			// Remove Hide when use skill / item skill
			effect.addObserver(effected, new ActionObserver(ObserverType.STARTSKILLCAST) {

				@Override
				public void startSkillCast(Skill skill) {
					if (skill.getSkillMethod() == SkillMethod.ITEM)
						return; // items are counted once, on use (see ITEMUSE below)
					boolean isShapeChange = skill.getSkillTemplate().getEffects().hasAnyEffectType(EffectType.SHAPECHANGE);
					// TODO logic: retail spends the skill's own hide_decrease_count here (0 = breaks), like items do; until skill templates
					// carry it, every self buff spends 1
					if (isShapeChange || !skill.isSelfBuff() || !counter.spend(1))
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
					ItemActions actions = item.getItemTemplate().getActions();
					if (actions != null && !counter.spend(item.getItemTemplate().getHideDecreaseCount()))
						effect.endEffect();
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

	/**
	 * Hide counter shared by skills and items: a counter of 0 breaks on anything, a
	 * decrease of 255 never spends, a decrease of 0 breaks, otherwise the counter goes down and breaks once it reaches 0.
	 */
	static final class HideCounter {

		static final int NEVER_SPENDS = 255;
		private int remaining;

		HideCounter(int remaining) {
			this.remaining = remaining;
		}

		/**
		 * @return true if hide holds
		 */
		synchronized boolean spend(int decrease) {
			if (remaining <= 0 || decrease == 0)
				return false;
			if (decrease == NEVER_SPENDS)
				return true;
			remaining -= decrease;
			return remaining > 0;
		}
	}
}

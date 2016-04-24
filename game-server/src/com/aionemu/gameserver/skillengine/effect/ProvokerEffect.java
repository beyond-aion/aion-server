package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.ProvokeTarget;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ATracer modified by kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProvokerEffect")
public class ProvokerEffect extends ShieldEffect {

	@XmlAttribute(name = "provoke_target")
	protected ProvokeTarget provokeTarget;
	@XmlAttribute(name = "skill_id")
	protected int skillId;

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		ActionObserver observer = null;
		final Creature effector = effect.getEffector();
		final int prob2 = hitTypeProb;
		final int radius = this.radius;
		switch (hitType) {
			case NMLATK:// ATTACK
				observer = new ActionObserver(ObserverType.ATTACK) {

					@Override
					public void attack(Creature creature) {
						if (Rnd.get(1, 100) <= prob2) {
							Creature target = getProvokeTarget(provokeTarget, effector, creature);
							createProvokedEffect(effector, target);
						}
					}

				};
				break;
			case PHHIT:
				observer = new ActionObserver(ObserverType.ATTACKED) {

					@Override
					public void attacked(Creature creature, int id) {
						if (radius > 0) {
							if (!MathUtil.isIn3dRange(effector, creature, radius))
								return;
						}
						if (Rnd.get(1, 100) <= prob2) {
							if (id == 0 || DataManager.SKILL_DATA.getSkillTemplate(id).getType() == SkillType.PHYSICAL) {
								Creature target = getProvokeTarget(provokeTarget, effector, creature);
								createProvokedEffect(effector, target);
							}
						}
					}
				};
				break;
			case MAHIT:
				observer = new ActionObserver(ObserverType.ATTACKED) {

					@Override
					public void attacked(Creature creature, int id) {
						if (radius > 0) {
							if (!MathUtil.isIn3dRange(effector, creature, radius))
								return;
						}
						if (Rnd.get(1, 100) <= prob2) {
							if (id != 0 && DataManager.SKILL_DATA.getSkillTemplate(id).getType() == SkillType.MAGICAL) {
								Creature target = getProvokeTarget(provokeTarget, effector, creature);
								createProvokedEffect(effector, target);
							}
						}
					}
				};
				break;
			case EVERYHIT:// ATTACKED
				observer = new ActionObserver(ObserverType.ATTACKED) {

					@Override
					public void attacked(Creature creature, int id) {
						if (radius > 0) {
							if (!MathUtil.isIn3dRange(effector, creature, radius))
								return;
						}
						if (Rnd.get(1, 100) <= prob2) {
							Creature target = getProvokeTarget(provokeTarget, effector, creature);
							createProvokedEffect(effector, target);
						}
					}
				};
				break;
			case BACKATK:// ATTACK opponents from back
				observer = new ActionObserver(ObserverType.ATTACK) {

					@Override
					public void attack(Creature creature) {
						if (PositionUtil.isBehind(creature, effector)) {
							if (Rnd.get(1, 100) <= prob2) {
								Creature target = getProvokeTarget(provokeTarget, effector, creature);
								createProvokedEffect(effector, target);
							}
						}
					}

				};
				break;
		}

		if (observer == null)
			return;

		effect.setActionObserver(observer, position);
		effect.getEffected().getObserveController().addObserver(observer);
	}

	/**
	 * @param effector
	 * @param target
	 */
	private void createProvokedEffect(final Creature effector, Creature target) {
		if (provokeTarget == ProvokeTarget.OPPONENT && target == effector) {
			return;
		}

		if (effector instanceof Player) {
			int nameId = DataManager.SKILL_DATA.getSkillTemplate(skillId).getNameId();
			PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_SKILL_PROC_EFFECT_OCCURRED(nameId));
		}
		SkillEngine.getInstance().applyEffectDirectly(skillId, effector, target, 0);
	}

	/**
	 * @param provokeTarget
	 * @param effector
	 * @param target
	 * @return
	 */
	private Creature getProvokeTarget(ProvokeTarget provokeTarget, Creature effector, Creature target) {
		switch (provokeTarget) {
			case ME:
				return effector;
			case OPPONENT:
				return target;
		}
		throw new IllegalArgumentException("Provoker target is invalid " + provokeTarget);
	}

	@Override
	public void endEffect(Effect effect) {
		ActionObserver observer = effect.getActionObserver(position);
		if (observer != null)
			effect.getEffected().getObserveController().removeObserver(observer);
	}
}

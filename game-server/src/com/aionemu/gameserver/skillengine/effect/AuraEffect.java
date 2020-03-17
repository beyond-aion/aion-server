package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MANTRA_EFFECT;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, kecimis, xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuraEffect")
public class AuraEffect extends EffectTemplate {

	@XmlAttribute
	protected int distance;
	@XmlAttribute(name = "distance_z")
	protected int distanceZ;
	@XmlAttribute(name = "skill_id")
	protected int skillId;

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		if (effector instanceof Player && effector.getEffectController().findBySkillId(effect.getSkillId()) != null) {
			AuditLogger.log((Player) effector, "might be abusing CM_CASTSPELL mantra effect, skill id: " + effect.getSkillId());
			return;
		}
		effect.addToEffectedController();
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		Creature effector = effect.getEffector();
		if (effector instanceof Npc) {
			applyAuraTo(effector);
		} else {
			Player p = (Player) effector;
			if (!p.isOnline()) { // task check
				return;
			}
			if (p.isInTeam()) {
				int rangeBoost = effector.getGameStats().getStat(StatEnum.BOOST_MANTRA_RANGE, 100).getCurrent();
				float rangeZ = distanceZ * rangeBoost / 100f;
				float range = distance * rangeBoost / 100f;
				for (Player player : p.getCurrentGroup().getOnlineMembers()) {
					if (p.equals(player) || Math.abs(p.getZ() - player.getZ()) <= rangeZ && PositionUtil.isInRange(p, player, range, false)) {
						applyAuraTo(player);
					}
				}
			} else {
				applyAuraTo(effector);
			}
		}
		PacketSendUtility.broadcastPacket(effector, new SM_MANTRA_EFFECT(effector, skillId));
	}

	private void applyAuraTo(Creature effected) {
		SkillEngine.getInstance().applyEffect(skillId, effected, effected);
	}

	@Override
	public void startEffect(Effect effect) {
		effect.setPeriodicTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new AuraTask(effect), 0, 6500), position);
	}

	private class AuraTask implements Runnable {

		private Effect effect;

		public AuraTask(Effect effect) {
			this.effect = effect;
		}

		@Override
		public void run() {
			onPeriodicAction(effect);
			/**
			 * This has the special effect of clearing the current thread's quantum and putting it to the end of the queue for its priority level. Will just
			 * give-up the thread's turn, and gain it in the next round.
			 */
			Thread.yield();
		}
	}

	@Override
	public void endEffect(Effect effect) {

	}
}

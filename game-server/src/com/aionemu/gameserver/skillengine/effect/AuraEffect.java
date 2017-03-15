package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MANTRA_EFFECT;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
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
		final Player effector = (Player) effect.getEffector();
		if (effector.getEffectController().isPresentBySkillId(effect.getSkillId())) {
			AuditLogger.info(effector, "might be abusing CM_CASTSPELL mantra effect Player kicked skill id: " + effect.getSkillId());
			effector.getClientConnection().close();
			return;
		}
		effect.addToEffectedController();
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		Player effector = (Player) effect.getEffector();
		if (!effector.isOnline()) { // task check
			return;
		}
		if (effector.isInTeam()) {
			int rangeBoost = effector.getGameStats().getStat(StatEnum.BOOST_MANTRA_RANGE, 100).getCurrent();
			float rangeZ = distanceZ * rangeBoost / 100f;
			float range = distance * rangeBoost / 100f;
			for (Player player : effector.getCurrentGroup().getOnlineMembers()) {
				if (effector.equals(player) || Math.abs(effector.getZ() - player.getZ()) <= rangeZ && PositionUtil.isInRange(effector, player, range)) {
					applyAuraTo(player);
				}
			}
		} else {
			applyAuraTo(effector);
		}
		PacketSendUtility.broadcastPacket(effector, new SM_MANTRA_EFFECT(effector, skillId));
	}

	/**
	 * @param effector
	 */
	private void applyAuraTo(Player effected) {
		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
		Effect e = new Effect(effected, effected, template, template.getLvl(), 0);
		e.initialize();
		e.applyEffect();
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

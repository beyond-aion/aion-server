package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_USESKILL;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetOrderUseUltraSkillEffect")
public class PetOrderUseUltraSkillEffect extends EffectTemplate {

	@XmlAttribute
	protected boolean release;

	@Override
	public void applyEffect(Effect effect) {
		Player effector = (Player) effect.getEffector();

		if (effector.getSummon() == null) {
			return;
		}

		int effectorId = effector.getSummon().getObjectId();

		int npcId = effector.getSummon().getNpcId();
		int orderSkillId = effect.getSkillId();

		int petUseSkillId = DataManager.PET_SKILL_DATA.getPetOrderSkill(orderSkillId, npcId);
		SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(petUseSkillId);
		if (skillTemplate == null) {
			LoggerFactory.getLogger(PetOrderUseUltraSkillEffect.class)
				.warn("Couldn't find summon skill template for ID {} (summon order skill ID {})", petUseSkillId, orderSkillId);
			return;
		}

		int skillLvl = skillTemplate.getLvl();
		int targetId = effect.getEffected().getObjectId();
		int hate = effect.getEffectHate() > 1 ? effect.getEffectHate() : 0;
		effector.getSummon().addSkillOrder(petUseSkillId, skillLvl, effect.getEffected(), hate, release);
		PacketSendUtility.sendPacket(effector, new SM_SUMMON_USESKILL(effectorId, petUseSkillId, skillLvl, targetId));
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffector() instanceof Player && effect.getEffected() != null)
			super.calculate(effect, null, null);
	}
}

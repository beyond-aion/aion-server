package com.aionemu.gameserver.skillengine.condition;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.ChainSkills;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, kecimis, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChainCondition")
public class ChainCondition extends Condition {

	@XmlAttribute(name = "selfcount")
	private int selfCount = 1;
	@XmlAttribute(name = "precount")
	private int preCount = 1;
	@XmlAttribute(name = "category")
	private String category;
	@XmlAttribute(name = "precategory")
	private String preCategory;
	@XmlAttribute(name = "time")
	private int time;

	/**
	 * Checks the chain state without changing it. A cast that passes and actually starts resets the chain if it was used as a follow-up skill or
	 * restarts its own exhausted repetition, see {@link Skill#isChainResetOnCastStart()}.
	 */
	@Override
	public boolean validate(Skill env) {
		if (env.getEffector() instanceof Player player) {
			ChainSkills chain = player.getChainSkills();
			if (isRepeatable() && chain.canRepeat(category, selfCount, getActivationWindow()))
				return true;
			if (preCategory != null) {
				if (!chain.canContinue(preCategory, preCount, getActivationWindow())) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_IN_NONE_CHAINSKILL_STATUS());
					return false;
				}
				env.setChainResetOnCastStart(true);
			} else if (isRepeatable() && chain.isCurrentCategory(category)) {
				env.setChainResetOnCastStart(true);
			}
		}
		return true;
	}

	private boolean isRepeatable() {
		return selfCount > 1;
	}

	/**
	 * @return Number of allowed skill activations of this chain skill.
	 */
	public int getAllowedActivations() {
		return selfCount;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return Time in milliseconds after the hit of the predecessor (or of the own previous use for repetitions) within which this skill can be
	 *         activated: the template time plus 1500, or 3000 if the template has none.<br>
	 *         Differs from retail on purpose: retail tells a missing time (3000) from an explicit time of 0 (0 + 1500 = 1500), here 0 means
	 *         missing and gives 3000. No template has an explicit 0, so the result is the same; to support one, make the field an Integer and
	 *         check for null instead of 0.
	 */
	public int getActivationWindow() {
		return time == 0 ? 3000 : time + 1500;
	}
}
